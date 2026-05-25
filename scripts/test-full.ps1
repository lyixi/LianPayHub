param(
    [switch]$SkipCompile
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$env:MAVEN_OPTS = "-Xms32m -Xmx192m -XX:MaxMetaspaceSize=160m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:CICompilerCount=2 -Xss512k"

function Resolve-Maven {
    $mvn = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvn) {
        return $mvn.Source
    }

    $scoopMaven = Join-Path $env:USERPROFILE "scoop\apps\maven\current\bin\mvn.cmd"
    if (Test-Path $scoopMaven) {
        return $scoopMaven
    }

    $wrapper = Join-Path $repoRoot "mvnw.cmd"
    if (Test-Path $wrapper) {
        return $wrapper
    }

    throw "Maven was not found. Install Maven or make sure mvnw.cmd is usable."
}

$maven = Resolve-Maven
$tests = "ApiSecretAuthIntegrationTest,ApiSignatureAuthIntegrationTest,FullWorkflowIntegrationTest,AdminApiSmokeTest"

if (-not $SkipCompile) {
    & $maven -q "-DskipTests" compile
}

& $maven -q "-DforkCount=0" "-Dspring.test.context.cache.maxSize=1" "-Dtest=$tests" test
