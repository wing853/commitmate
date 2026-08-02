$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mobileRoot = Join-Path $projectRoot 'mobile'
$flutterCommand = Get-Command flutter -ErrorAction SilentlyContinue
$userFlutter = Join-Path $env:USERPROFILE 'develop\flutter\bin\flutter.bat'

if ($flutterCommand) {
    $flutterExecutable = $flutterCommand.Source
}
elseif (Test-Path -LiteralPath $userFlutter) {
    $flutterExecutable = $userFlutter
}
else {
    throw 'Flutter SDK를 찾을 수 없습니다. Flutter를 PATH에 추가해 주세요.'
}

Push-Location $mobileRoot
try {
    & $flutterExecutable pub get
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    & $flutterExecutable run
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
finally {
    Pop-Location
}
