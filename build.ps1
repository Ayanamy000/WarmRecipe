# WarmRecipe - manual APK build script (no Gradle)
# Builds in an ASCII temp dir (aapt2/zipalign cannot open non-ASCII paths on Windows),
# then copies the signed APK back to the workspace.
$ws     = $PSScriptRoot                       # workspace (may contain non-ASCII chars)
$bt     = "$ws\sdk\build-tools\35.0.0"        # native tools (launched from here, args stay ASCII)
$ajSrc  = "$ws\sdk\platforms\android-35\android.jar"
$appSrc = "$ws\app"
$ksSrc  = "$ws\release.keystore"
$jdkbin = 'C:\Program Files\Java\jdk-24\bin'

$tmp   = Join-Path $env:TEMP 'recipe_build'
$app   = "$tmp\app"
$aj    = "$tmp\android.jar"
$ks    = "$tmp\release.keystore"
$build = "$tmp\build"
$apk   = "$tmp\WarmRecipe-v1.51.apk"

# --- stage into ASCII temp dir ---
Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "$build\gen","$build\classes","$build\dex" | Out-Null
Copy-Item $appSrc $app -Recurse
Copy-Item $ajSrc  $aj
Copy-Item $ksSrc  $ks

Write-Host "== aapt2 compile =="
& "$bt\aapt2.exe" compile --dir "$app\res" -o "$build\res.zip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed: $LASTEXITCODE" }

Write-Host "== aapt2 link =="
& "$bt\aapt2.exe" link -o "$build\app-unsigned.apk" -I $aj --manifest "$app\AndroidManifest.xml" -R "$build\res.zip" --java "$build\gen" --auto-add-overlay
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed: $LASTEXITCODE" }

Write-Host "== javac =="
$javaFiles = Get-ChildItem -Recurse "$app\src" -Filter *.java | ForEach-Object { $_.FullName }
$rFiles    = Get-ChildItem -Recurse "$build\gen" -Filter *.java | ForEach-Object { $_.FullName }
& javac --release 8 -encoding UTF-8 -nowarn -classpath $aj -d "$build\classes" @($javaFiles + $rFiles)
if ($LASTEXITCODE -ne 0) { throw "javac failed: $LASTEXITCODE" }

Write-Host "== jar classes =="
& "$jdkbin\jar.exe" cf "$build\classes.jar" -C "$build\classes" .
if ($LASTEXITCODE -ne 0) { throw "jar failed: $LASTEXITCODE" }

Write-Host "== d8 dex =="
& java -cp "$bt\lib\d8.jar" com.android.tools.r8.D8 --lib $aj --min-api 26 --output "$build\dex" "$build\classes.jar"
if ($LASTEXITCODE -ne 0) { throw "d8 failed: $LASTEXITCODE" }

Write-Host "== add classes.dex =="
Copy-Item "$build\app-unsigned.apk" "$build\app-withdex.apk" -Force
& "$jdkbin\jar.exe" uf "$build\app-withdex.apk" -C "$build\dex" classes.dex
if ($LASTEXITCODE -ne 0) { throw "jar uf failed: $LASTEXITCODE" }

Write-Host "== zipalign =="
& "$bt\zipalign.exe" -f -p 4 "$build\app-withdex.apk" "$build\app-aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "zipalign failed: $LASTEXITCODE" }

Write-Host "== apksigner sign =="
& java -jar "$bt\lib\apksigner.jar" sign --ks $ks --ks-key-alias recipe --ks-pass pass:recipe123 --key-pass pass:recipe123 --out $apk "$build\app-aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "apksigner failed: $LASTEXITCODE" }

Write-Host "== verify =="
& java -jar "$bt\lib\apksigner.jar" verify --print-certs $apk

# --- copy APK back to workspace ---
Copy-Item $apk "$ws\WarmRecipe-v1.51.apk" -Force
Write-Host ""
Write-Host "APK built: $ws\WarmRecipe-v1.51.apk"
