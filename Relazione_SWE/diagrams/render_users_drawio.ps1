param(
    [string]$Source = (Join-Path $PSScriptRoot '..\..\docs\Users SWE.drawio'),
    [string]$Output = (Join-Path $PSScriptRoot '..\images\users-swe-current.png')
)

# Render the existing draw.io graph without changing its cells, geometry or links.
Add-Type -AssemblyName System.Drawing
[xml]$document = Get-Content -LiteralPath $Source -Raw -Encoding UTF8
$cells = @{}
foreach ($cell in $document.mxfile.diagram.mxGraphModel.root.mxCell) {
    $cells[$cell.id] = $cell
}
$vertices = @($cells.Values | Where-Object {
    $_.vertex -eq '1' -and $_.mxGeometry -and $_.style -notlike 'edgeLabel*'
})
$left = [float](($vertices | ForEach-Object { [float]$_.mxGeometry.x } | Measure-Object -Minimum).Minimum)
$top = [float](($vertices | ForEach-Object { [float]$_.mxGeometry.y } | Measure-Object -Minimum).Minimum)
$right = [float](($vertices | ForEach-Object { [float]$_.mxGeometry.x + [float]$_.mxGeometry.width } | Measure-Object -Maximum).Maximum)
$bottom = [float](($vertices | ForEach-Object { [float]$_.mxGeometry.y + [float]$_.mxGeometry.height } | Measure-Object -Maximum).Maximum)
$padding = 48
$scale = 2
$bitmap = [System.Drawing.Bitmap]::new([int](($right - $left + 2 * $padding) * $scale), [int](($bottom - $top + 2 * $padding) * $scale))
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$graphics.ScaleTransform($scale, $scale)
$graphics.Clear([System.Drawing.Color]::White)
$ink = [System.Drawing.Color]::Black
$pen = [System.Drawing.Pen]::new($ink, 1.3)
$dashed = [System.Drawing.Pen]::new($ink, 1.3)
$dashed.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
$brush = [System.Drawing.SolidBrush]::new($ink)
$labelBackground = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
$caseFont = [System.Drawing.Font]::new('Arial', 10)
$actorFont = [System.Drawing.Font]::new('Arial', 14)
$edgeFont = [System.Drawing.Font]::new('Arial', 9)
$center = [System.Drawing.StringFormat]::new()
$center.Alignment = [System.Drawing.StringAlignment]::Center
$center.LineAlignment = [System.Drawing.StringAlignment]::Center

function Box($cell) {
    $g = $cell.mxGeometry
    return [System.Drawing.RectangleF]::new(
        ([float]$g.x - $left + $padding),
        ([float]$g.y - $top + $padding),
        [float]$g.width,
        [float]$g.height
    )
}

function Label($cell) {
    $value = [System.Net.WebUtility]::HtmlDecode([string]$cell.value)
    $value = $value -replace '(?i)</div>', "`n"
    $value = $value -replace '(?i)<br\s*/?>', "`n"
    $value = $value -replace '<[^>]+>', ''
    $parts = @($value -split "`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    return ($parts -join "`n")
}

# Edges first, then the actors and use-case ellipses.
foreach ($edge in $cells.Values) {
    if ($edge.edge -ne '1' -or -not $cells.ContainsKey($edge.source) -or -not $cells.ContainsKey($edge.target)) { continue }
    $sourceBox = Box ($cells[$edge.source])
    $target = Box ($cells[$edge.target])
    $sourceActor = $cells[$edge.source].style -like 'shape=actor*'
    $targetActor = $cells[$edge.target].style -like 'shape=actor*'
    $x1 = if ($target.X -ge $sourceBox.X) { $sourceBox.Right } else { $sourceBox.Left }
    $y1 = $sourceBox.Y + $sourceBox.Height / 2
    $x2 = if ($target.X -ge $sourceBox.X) { $target.Left } else { $target.Right }
    $y2 = $target.Y + $target.Height / 2
    if ($sourceActor) { $y1 = $sourceBox.Y + 30 }
    if ($targetActor) { $y2 = $target.Y + 30 }
    $edgePen = if ($edge.style -like '*dashed=1*') { $dashed } else { $pen }
    $graphics.DrawLine($edgePen, $x1, $y1, $x2, $y2)
    if ($edge.style -like '*endArrow=block*' -or $edge.style -like '*endArrow=classic*') {
        $direction = if ($x2 -gt $x1) { 1 } else { -1 }
        $graphics.DrawLine($pen, $x2, $y2, $x2 - 9 * $direction, $y2 - 5)
        $graphics.DrawLine($pen, $x2, $y2, $x2 - 9 * $direction, $y2 + 5)
    }
    if ($edge.style -like '*dashed=1*') {
        $label = '<<include>>'
        $labelX = ($x1 + $x2) / 2
        $labelY = ($y1 + $y2) / 2 - 15
        $labelBox = [System.Drawing.RectangleF]::new($labelX - 45, $labelY, 90, 16)
        $graphics.FillRectangle($labelBackground, $labelBox)
        $graphics.DrawString($label, $edgeFont, $brush, $labelBox, $center)
    }
}

foreach ($cell in $vertices) {
    $box = Box $cell
    $label = Label $cell
    if ($cell.style -like 'shape=actor*') {
        $cx = $box.X + $box.Width / 2
        $graphics.DrawEllipse($pen, $cx - 7, $box.Y + 1, 14, 14)
        $graphics.DrawLine($pen, $cx, $box.Y + 15, $cx, $box.Y + 39)
        $graphics.DrawLine($pen, $cx - 14, $box.Y + 25, $cx + 14, $box.Y + 25)
        $graphics.DrawLine($pen, $cx, $box.Y + 39, $cx - 15, $box.Y + 56)
        $graphics.DrawLine($pen, $cx, $box.Y + 39, $cx + 15, $box.Y + 56)
        $graphics.DrawString($label, $actorFont, $brush,
            [System.Drawing.RectangleF]::new($box.X - 36, $box.Bottom + 2, $box.Width + 72, 22), $center)
    } elseif ($cell.style -like 'ellipse*') {
        $graphics.DrawEllipse($pen, $box)
        $graphics.DrawString($label, $caseFont, $brush, $box, $center)
    }
}

$directory = Split-Path -Parent $Output
if (-not (Test-Path -LiteralPath $directory)) { New-Item -ItemType Directory -Path $directory | Out-Null }
$bitmap.Save($Output, [System.Drawing.Imaging.ImageFormat]::Png)
$center.Dispose()
$edgeFont.Dispose()
$actorFont.Dispose()
$caseFont.Dispose()
$labelBackground.Dispose()
$brush.Dispose()
$dashed.Dispose()
$pen.Dispose()
$graphics.Dispose()
$bitmap.Dispose()
