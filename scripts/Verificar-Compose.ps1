$ErrorActionPreference='Stop'
$base=Split-Path -Parent $PSScriptRoot
Push-Location $base
$correlacao='demonstracao-compose-'+[guid]::NewGuid().ToString('N')
function AguardarSaudeEspacos {
    for($i=0;$i -lt 120;$i++){
        $id=docker compose ps -q espacos
        if($id){$status=docker inspect --format '{{.State.Health.Status}}' $id;if($status -eq 'healthy'){return}}
        Start-Sleep -Seconds 2
    }
    throw 'Espaços não ficou saudável após start'
}
function Detalhes([string]$status){
    for($i=0;$i -lt 90;$i++){
        try{
            $res=Invoke-RestMethod -Uri 'http://127.0.0.1:8082/api/eventos/1/detalhes' -TimeoutSec 5 `
                -Headers @{'X-Correlation-Id'=$correlacao;Accept='application/json'}
            if($res.espaco.status -eq $status){return $res}
        }catch{}
        Start-Sleep -Seconds 1
    }
    throw "Não obtido estado $status"
}
try {
    $normal=Detalhes 'DISPONIVEL'
    if($normal.espaco.nome -ne 'Galpão 47'){throw 'Dados compostos incorretos'}
    $health=Invoke-RestMethod -Uri 'http://127.0.0.1:8082/actuator/health'
    if($health.status -ne 'UP'){throw 'Health do Gateway não está UP'}
    Write-Output 'Detalhes completos e saúde do Gateway confirmados.'
    docker compose stop espacos
    if($LASTEXITCODE -ne 0){throw 'Não foi possível parar Espaços'}
    $fallback=Detalhes 'TEMPORARIAMENTE_INDISPONIVEL'
    for($i=0;$i -lt 4;$i++){$null=Detalhes 'TEMPORARIAMENTE_INDISPONIVEL'}
    $eventos=Invoke-RestMethod -Uri 'http://127.0.0.1:8082/api/eventos'
    if($eventos.Count -lt 4){throw 'Listagem de Eventos afetada'}
    $log=(docker compose logs --no-color eventos 2>&1|Out-String)
    $abriu=($log -split "`n"|Where-Object {$_.Contains($correlacao) -and $_.Contains('CallNotPermittedException')})
    if(-not $abriu){throw 'Não observada recusa por circuito aberto nos logs'}
    Write-Output 'Fallback durante interrupção de container e circuito OPEN confirmados.'
    docker compose start espacos
    if($LASTEXITCODE -ne 0){throw 'Não foi possível restaurar Espaços'}
    AguardarSaudeEspacos
    $recuperacao=Detalhes 'DISPONIVEL'
    $null=Detalhes 'DISPONIVEL'
    Write-Output 'Recuperação dos detalhes confirmada após start, sem editar código.'
    $query=[Uri]::EscapeDataString('{job="indiestage"} |= "'+$correlacao+'"')
    $consulta=$null
    for($i=0;$i -lt 90;$i++){
        try{
            $consulta=Invoke-RestMethod -Uri "http://127.0.0.1:3100/loki/api/v1/query_range?query=$query&limit=1000" -TimeoutSec 5
            $servicos=@($consulta.data.result|ForEach-Object {$_.stream.service}|Sort-Object -Unique)
            if(($servicos -contains 'gateway') -and ($servicos -contains 'eventos') -and ($servicos -contains 'espacos')){break}
        }catch{}
        Start-Sleep -Seconds 1
    }
    if(-not($servicos -contains 'gateway') -or -not($servicos -contains 'eventos') -or -not($servicos -contains 'espacos')){
        throw 'Logs correlacionados dos três serviços ainda não encontrados no Loki'
    }
    $grafana=Invoke-RestMethod -Uri 'http://127.0.0.1:3000/api/health' -TimeoutSec 10
    if($grafana.database -ne 'ok'){throw 'Grafana não está pronto'}
    $painel=Invoke-RestMethod -Uri 'http://127.0.0.1:3000/api/dashboards/uid/indiestage-logs' -TimeoutSec 10
    if($painel.dashboard.uid -ne 'indiestage-logs'){throw 'Painel não provisionado'}
    $provas=Join-Path $base '.verificacoes'
    $resultado=[ordered]@{data=(Get-Date -Format o);status='APROVADO';correlationId=$correlacao;
        normal=$normal;fallback=$fallback;recuperacao=$recuperacao;healthGateway=$health;
        servicosNosLogs=$servicos;grafana=$grafana;limite='A arguição ao vivo e a prática do estudante ainda devem ocorrer.'}
    $resultado|ConvertTo-Json -Depth 10|Set-Content -LiteralPath "$provas\resultado-compose.json" -Encoding UTF8
    $consulta|ConvertTo-Json -Depth 15|Set-Content -LiteralPath "$provas\logs-correlacionados.json" -Encoding UTF8
    docker compose ps --format json |Set-Content -LiteralPath "$provas\containers.json" -Encoding UTF8
    Write-Output "APROVADO: containers, comunicação, fallback, recuperação e logs centralizados. correlationId=$correlacao"
} finally {
    docker compose start espacos | Out-Null
    Pop-Location
}