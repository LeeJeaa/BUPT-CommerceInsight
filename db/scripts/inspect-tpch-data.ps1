param(
    [string]$DataDir = "",
    [string]$LogDir = "../../report/import_logs"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
function Resolve-TpchDataDir {
    param([string]$InputDir)
    if ($InputDir) {
        return Resolve-Path (Join-Path $scriptDir $InputDir)
    }

    $projectRoot = Resolve-Path (Join-Path $scriptDir "../..")
    $candidate = Get-ChildItem -LiteralPath $projectRoot -Directory |
        ForEach-Object { Get-ChildItem -LiteralPath $_.FullName -Directory -ErrorAction SilentlyContinue } |
        Where-Object { $_.Name -like "tpc-h*(2)" } |
        Select-Object -First 1

    if ($candidate) {
        return $candidate.FullName
    }

    return Resolve-Path (Join-Path $scriptDir "../../data/tpch/SF0.1")
}

$resolvedDataDir = Resolve-TpchDataDir $DataDir
$resolvedLogDir = Join-Path $scriptDir $LogDir
New-Item -ItemType Directory -Force -Path $resolvedLogDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $resolvedLogDir "inspect_tpch_data_$timestamp.txt"

$expected = @(
    @{ File = "region.txt";   Columns = 3;  ExpectedRows = 5 },
    @{ File = "nation.txt";   Columns = 4;  ExpectedRows = 25 },
    @{ File = "supplier.txt"; Columns = 7;  ExpectedRows = 2000 },
    @{ File = "customer.txt"; Columns = 8;  ExpectedRows = 30000 },
    @{ File = "part.txt";     Columns = 9;  ExpectedRows = 40000 },
    @{ File = "partsupp.txt"; Columns = 5;  ExpectedRows = 160000 },
    @{ File = "orders.txt";   Columns = 9;  ExpectedRows = 300000 },
    @{ File = "lineitem.txt"; Columns = 16; ExpectedRows = 1199969 }
)

function Write-Log {
    param([string]$Message)
    $line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message"
    Write-Host $line
    Add-Content -Path $logFile -Value $line
}

Write-Log "Inspecting TPC-H data directory: $resolvedDataDir"

foreach ($item in $expected) {
    $path = Join-Path $resolvedDataDir $item.File
    if (-not (Test-Path $path)) {
        Write-Log "MISSING $($item.File)"
        continue
    }

    $rowCount = 0
    $badCount = 0
    $trailingPipeCount = 0

    Get-Content -LiteralPath $path -ReadCount 5000 | ForEach-Object {
        foreach ($line in $_) {
            $rowCount++
            if ($line.EndsWith("|")) {
                $trailingPipeCount++
            }
            if (($line -split "\|", -1).Count -ne $item.Columns) {
                $badCount++
            }
        }
    }

    $rowStatus = if ($rowCount -eq $item.ExpectedRows) { "OK" } else { "CHECK" }
    $columnStatus = if ($badCount -eq 0) { "OK" } else { "CHECK" }
    Write-Log "$($item.File): rows=$rowCount expected=$($item.ExpectedRows) rowStatus=$rowStatus columns=$($item.Columns) badRows=$badCount columnStatus=$columnStatus trailingPipeRows=$trailingPipeCount"
}

Write-Log "Inspection completed. Log: $logFile"
