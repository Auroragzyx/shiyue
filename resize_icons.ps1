Add-Type -AssemblyName System.Drawing

$srcPath = "C:/Users/Administrator/.gemini/antigravity/brain/1e7454a0-babf-4b6b-899d-c286b395d159/.user_uploaded/media_1793352280541_9ea9e05c.png"
$srcImg = [System.Drawing.Image]::FromFile($srcPath)

function Resize-Image($img, $width, $height, $destPath) {
    $bmp = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bmp)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.DrawImage($img, 0, 0, $width, $height)
    $bmp.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bmp.Dispose()
}

# Remove old vector xml icon if present
$oldXml = "C:/Users/Administrator/SwissDaysApp/android/src/main/res/drawable/ic_launcher.xml"
if (Test-Path $oldXml) { Remove-Item $oldXml -Force }

# Save 192x192 for android drawable
Resize-Image $srcImg 192 192 "C:/Users/Administrator/SwissDaysApp/android/src/main/res/drawable/ic_launcher.png"

# Save for web / favicon
Resize-Image $srcImg 192 192 "C:/Users/Administrator/SwissDaysApp/icon-192.png"
Resize-Image $srcImg 512 512 "C:/Users/Administrator/SwissDaysApp/icon-512.png"
Resize-Image $srcImg 64 64 "C:/Users/Administrator/SwissDaysApp/favicon.png"

$srcImg.Dispose()
Write-Host "Icons generated successfully!"
