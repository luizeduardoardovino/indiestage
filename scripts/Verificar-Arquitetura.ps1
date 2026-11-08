# Verificação local de descoberta e rotas, com processos e bancos descartáveis.
# Não para aplicações abertas no Eclipse. Não usa o Docker.
$ErrorActionPreference='Stop'
$base=Split-Path -Parent $PSScriptRoot
$saida=Join-Path $base '.verificacoes'
New-Item -ItemType Directory -Path $saida -Force|Out-Null
$execucao=Join-Path $saida ('runtime-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $execucao|Out-Null
$processos=[Collections.Generic.List[Diagnostics.Process]]::new()
$interrompidos=[Collections.Generic.List[int]]::new()
$java=Join-Path $env:JAVA_HOME 'bin\java.exe'
function Iniciar([string]$nome,[int]$porta,[string[]]$extras){
    $jar=Join-Path $base "$nome\target\$nome-0.0.1-SNAPSHOT.jar"
    if(-not(Test-Path -LiteralPath $jar)){throw "JAR ausente: $jar"}
    $args=@('-Xmx256m','-jar',('"'+$jar+'"'),"--server.port=$porta",
        '--server.address=127.0.0.1','--spring.main.banner-mode=off')+$extras
    $processo=Start-Process -FilePath $java -ArgumentList $args -WorkingDirectory "$base\$nome" `
        -WindowStyle Hidden -PassThru -RedirectStandardOutput "$execucao\$nome.log" `
        -RedirectStandardError "$execucao\$nome.err.log"
    $processos.Add($processo)
    Write-Output "$nome iniciado: PID $($processo.Id), porta $porta"
}
function Aguardar([string]$url,[int]$tentativas=90){
    for($i=0;$i -lt $tentativas;$i++){
        try{return Invoke-RestMethod -Uri $url -TimeoutSec 2 -Headers @{Accept='application/json'}}catch{
            foreach($p in $processos){if($p.HasExited -and -not $interrompidos.Contains($p.Id)){throw "Processo $($p.Id) encerrou. Consulte $execucao"}}
            Start-Sleep -Seconds 1
        }
    }
    throw "Tempo esgotado aguardando $url. Consulte $execucao"
}
try{
    foreach($porta in @(18761,18080,18081,18082)){
        if(Get-NetTCPConnection -LocalPort $porta -State Listen -ErrorAction SilentlyContinue){throw "Porta $porta ocupada"}
    }
    Iniciar 'eureka-server' 18761 @('--eureka.server.wait-time-in-ms-when-sync-empty=0',
        '--eureka.server.use-read-only-response-cache=false')
    $null=Aguardar 'http://127.0.0.1:18761/eureka/apps'
    $descoberta=@('--eureka.client.enabled=true',
        '--eureka.client.service-url.defaultZone=http://127.0.0.1:18761/eureka/',
        '--eureka.instance.prefer-ip-address=true','--eureka.instance.ip-address=127.0.0.1',
        '--eureka.client.registry-fetch-interval-seconds=1',
        '--eureka.client.initial-instance-info-replication-interval-seconds=1')
    foreach($servico in @('espacos','eventos')){
        $porta=if($servico -eq 'espacos'){18080}else{18081}
        $banco=(Join-Path $execucao $servico).Replace('\','/')
        Iniciar $servico $porta ($descoberta+@("--spring.datasource.url=jdbc:h2:file:$banco;DB_CLOSE_ON_EXIT=FALSE",'--spring.h2.console.enabled=false'))
        $null=Aguardar "http://127.0.0.1:$porta/api/$servico"
    }
    Iniciar 'gateway' 18082 ($descoberta+@('--spring.cloud.loadbalancer.cache.enabled=false'))
    $espacos=Aguardar 'http://127.0.0.1:18082/api/espacos'
    $eventos=Aguardar 'http://127.0.0.1:18082/api/eventos'
    Write-Output "Contagem via Gateway: espacos=$($espacos.Count), eventos=$($eventos.Count)"
    @{espacos=$espacos;eventos=$eventos}|ConvertTo-Json -Depth 8|Set-Content -LiteralPath "$execucao\respostas.json" -Encoding UTF8
    if($espacos.Count -ne 4 -or $eventos.Count -ne 4){throw 'Carga inicial inesperada pelo Gateway'}
    $pagina=Aguardar 'http://127.0.0.1:18082/api/eventos/paginados?pagina=0&tamanho=2'
    if($pagina.conteudo.Count -ne 2 -or $pagina.totalElementos -ne 4){throw 'Paginação via Gateway inválida'}
    $registro=Aguardar 'http://127.0.0.1:18761/eureka/apps'
    $nomes=@($registro.applications.application|ForEach-Object {$_.name})
    foreach($esperado in @('ESPACOS','EVENTOS')){if($nomes -notcontains $esperado){throw "Registro ausente: $esperado"}}
    function Detalhes([string]$statusEsperado){
        for($tentativa=0;$tentativa -lt 60;$tentativa++){
            try{
                $dados=Invoke-RestMethod -Uri 'http://127.0.0.1:18082/api/eventos/1/detalhes' -TimeoutSec 5 `
                    -Headers @{'X-Correlation-Id'='demonstracao-indie-1';Accept='application/json'}
                if($dados.espaco.status -eq $statusEsperado){return $dados}
            }catch{}
            Start-Sleep -Seconds 1
        }
        throw "Não obtido status esperado: $statusEsperado"
    }
    $normal=Detalhes 'DISPONIVEL'
    if($normal.espaco.nome -ne 'Galpão 47'){throw 'Composição de dados incorreta'}
    Write-Output 'Detalhes completos obtidos: Eventos consultou Espaços por descoberta.'
    $interrompidos.Add($processos[1].Id)
    Stop-Process -Id $processos[1].Id
    $processos[1].WaitForExit()
    $fallback=Detalhes 'TEMPORARIAMENTE_INDISPONIVEL'
    for($i=0;$i -lt 4;$i++){ $null=Detalhes 'TEMPORARIAMENTE_INDISPONIVEL' }
    $metrica=Invoke-RestMethod -Uri 'http://127.0.0.1:18081/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:espacos&tag=state:open'
    if($metrica.measurements[0].value -ne 1){throw 'Circuit breaker não foi observado aberto'}
    $listaDuranteFalha=Invoke-RestMethod -Uri 'http://127.0.0.1:18082/api/eventos'
    if($listaDuranteFalha.Count -ne 4){throw 'Consulta de Eventos não permaneceu disponível'}
    Write-Output 'Fallback confirmado e circuito observado OPEN; listagem de Eventos preservada.'
    $bancoEspacos=(Join-Path $execucao 'espacos').Replace('\','/')
    Iniciar 'espacos' 18080 ($descoberta+@("--spring.datasource.url=jdbc:h2:file:$bancoEspacos;DB_CLOSE_ON_EXIT=FALSE",'--spring.h2.console.enabled=false'))
    $null=Aguardar 'http://127.0.0.1:18080/actuator/health'
    $recuperado=Detalhes 'DISPONIVEL'
    $null=Detalhes 'DISPONIVEL'
    $fechado=Invoke-RestMethod -Uri 'http://127.0.0.1:18081/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:espacos&tag=state:closed'
    if($fechado.measurements[0].value -ne 1){throw 'Circuit breaker não voltou a CLOSED'}
    Write-Output 'Recuperação confirmada sem editar código; circuito CLOSED.'
    $resultado=[ordered]@{
        data=(Get-Date -Format o);status='APROVADO';servicosRegistrados=$nomes;
        quantidadeEspacos=$espacos.Count;quantidadeEventos=$eventos.Count;
        paginacaoEventos=$pagina;processos=@($processos|ForEach-Object {$_.Id});
        composicao=$normal;fallback=$fallback;recuperacao=$recuperado;
        limite='Falha de processo verificada; demonstração em containers e logs centralizados ainda pendentes.'
    }
    $resultado|ConvertTo-Json -Depth 8|Set-Content -LiteralPath "$execucao\resultado.json" -Encoding UTF8
    Write-Output 'APROVADO: dois serviços registrados no Eureka e rotas por nome lógico no Gateway.'
    Write-Output "Evidência: $execucao\resultado.json"
} finally {
    foreach($p in $processos){if(-not $p.HasExited){Stop-Process -Id $p.Id -ErrorAction SilentlyContinue}}
    Write-Output 'Processos iniciados por este teste encerrados; aplicações do Eclipse preservadas.'
}