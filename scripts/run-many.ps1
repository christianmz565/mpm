param(
    [int]$Count = 1
)

$CoreDirs = @("core/src", "assets")
$ChecksumFile = ".core_checksum"
$JarPath = "lwjgl3/build/libs/MicroPatosMania-1.0.0.jar"

try {
    $Files = Get-ChildItem -Path $CoreDirs -Recurse -File -ErrorAction Stop
    $FileHashes = $Files | Get-FileHash -Algorithm SHA1 | Sort-Object Hash
    $Signature = ($FileHashes | ForEach-Object { $_.Hash }) -join ""
    
    $Utf8Obj = [System.Text.Encoding]::UTF8.GetBytes($Signature)
    $Stream = [System.IO.MemoryStream]::new($Utf8Obj)
    $CurrentSum = (Get-FileHash -InputStream $Stream -Algorithm SHA1).Hash
} catch {
    $CurrentSum = "ERROR_CALCULATING_SUM"
}

$StoredSum = if (Test-Path $ChecksumFile) { (Get-Content $ChecksumFile).Trim() } else { "" }

if (-not (Test-Path $ChecksumFile) -or $StoredSum -ne $CurrentSum) {
    Write-Host "Source changed - rebuilding..."
    .\gradlew.bat lwjgl3:build
    $CurrentSum | Out-File $ChecksumFile -Encoding ascii -NoNewline
} else {
    Write-Host "No changes in $CoreDirs - skipping rebuild."
}

1..$Count | ForEach-Object {
    $LogFile = "lastrun-$_.log"
    Start-Process -FilePath "cmd" -ArgumentList "/c java -jar ""$JarPath"" > ""$LogFile"" 2>&1" -WindowStyle Hidden
}