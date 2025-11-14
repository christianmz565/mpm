param(
  [int]$Count = 1
)

$CoreDir = "core"
$ChecksumFile = ".core_checksum"
$JarPath = "lwjgl3/build/libs/MicroPatosMania-1.0.0.jar"

$CurrentSum = Get-ChildItem -Path $CoreDir -Recurse -File |
  Sort-Object FullName |
  ForEach-Object {
    Get-FileHash -Path $_.FullName -Algorithm SHA1
  } |
  ForEach-Object { $_.Hash } |
  Out-String |
  Get-FileHash -InputStream ([IO.MemoryStream]::new([Text.Encoding]::UTF8.GetBytes($_))) -Algorithm SHA1 |
  Select-Object -ExpandProperty Hash

if (!(Test-Path $ChecksumFile) -or ((Get-Content $ChecksumFile).Trim() -ne $CurrentSum)) {
  Write-Host "Source changed - rebuilding..."
  ./gradlew lwjgl3:build
  $CurrentSum | Out-File -Encoding ASCII $ChecksumFile
} else {
  Write-Host "No changes in $CoreDir - skipping rebuild."
}

for ($i = 1; $i -le $Count; $i++) {
  Start-Process -FilePath 'java' -ArgumentList '-jar', $JarPath
}
