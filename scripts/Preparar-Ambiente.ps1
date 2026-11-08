$ErrorActionPreference='Stop'
$base=Split-Path -Parent $PSScriptRoot
$destino=Join-Path $base '.env'
if(Test-Path -LiteralPath $destino){Write-Output '.env existente preservado.';exit 0}
function NovaSenha {
    $bytes=New-Object byte[] 24
    $gerador=[Security.Cryptography.RandomNumberGenerator]::Create()
    try{$gerador.GetBytes($bytes)}finally{$gerador.Dispose()}
    return [BitConverter]::ToString($bytes).Replace('-','')
}
$conteudo="ESPACOS_DB_PASSWORD=$(NovaSenha)`nEVENTOS_DB_PASSWORD=$(NovaSenha)`nGRAFANA_ADMIN_PASSWORD=$(NovaSenha)`n"
[IO.File]::WriteAllText($destino,$conteudo,[Text.UTF8Encoding]::new($false))
Write-Output '.env local gerado com valores aleatórios. Não versionar esse arquivo.'