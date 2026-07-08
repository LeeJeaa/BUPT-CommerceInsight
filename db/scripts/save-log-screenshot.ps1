param(
    [Parameter(Mandatory = $true)]
    [string]$InputPath,
    [Parameter(Mandatory = $true)]
    [string]$OutputPath,
    [int]$Width = 1400,
    [int]$FontSize = 18,
    [int]$MaxLines = 70
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$resolvedInput = Resolve-Path $InputPath
$outputDir = Split-Path -Parent $OutputPath
if ($outputDir) {
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
}

$lines = Get-Content -LiteralPath $resolvedInput -Encoding UTF8
if ($lines.Count -gt $MaxLines) {
    $head = $lines | Select-Object -First ([Math]::Floor($MaxLines * 0.55))
    $tail = $lines | Select-Object -Last ([Math]::Ceiling($MaxLines * 0.40))
    $lines = @($head) + @("... output truncated for screenshot; see text log for full evidence ...") + @($tail)
}

$font = [System.Drawing.Font]::new("Consolas", $FontSize)
$lineHeight = [int]($font.GetHeight() + 8)
$padding = 28
$height = [Math]::Max(220, ($lines.Count + 2) * $lineHeight + ($padding * 2))

$bitmap = [System.Drawing.Bitmap]::new($Width, $height)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.Clear([System.Drawing.Color]::FromArgb(250, 250, 250))
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit

$brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(30, 30, 30))
$mutedBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(100, 100, 100))
$titleFont = [System.Drawing.Font]::new("Consolas", $FontSize + 2, [System.Drawing.FontStyle]::Bold)

$title = "Evidence: " + (Split-Path -Leaf $resolvedInput)
$graphics.DrawString($title, $titleFont, $brush, $padding, $padding)
$graphics.DrawString((Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $font, $mutedBrush, $padding, $padding + $lineHeight)

$y = $padding + ($lineHeight * 2) + 12
foreach ($line in $lines) {
    $graphics.DrawString($line, $font, $brush, $padding, $y)
    $y += $lineHeight
}

$bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)

$graphics.Dispose()
$bitmap.Dispose()
$font.Dispose()
$titleFont.Dispose()
$brush.Dispose()
$mutedBrush.Dispose()

Write-Host "Screenshot saved to $OutputPath"
