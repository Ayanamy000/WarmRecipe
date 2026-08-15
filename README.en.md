# WarmRecipe (温馨食谱)

English | [简体中文](README.md)

A cozy, minimalist recipe-book app for Android. Pure native with **zero third-party dependencies** — the APK is only ~58 KB.

## Features

- **Add recipes**: enter **ingredients & seasonings with weight/quantity** first (e.g. `egg · 2 pcs`, `flour · 200 g`), then **step-by-step instructions** (what to do + approximate time for each step). Add/remove rows freely.
- **Recipe detail**: shows the ingredient list first, then numbered steps with durations, plus an auto-calculated total time; supports notes, category and an emoji icon.
- **Categories**: Chinese / Western / Stir-fry / Stew / Soup / Dessert / Snack / Drink / Other.
- **Search & favorites**: search by name or ingredient; ★ to favorite.
- **Edit & delete**: existing recipes can be modified or removed.
- **Custom color themes**: 6 warm palettes (Cream Apricot / Peach Pink / Matcha Green / Warm Orange / Misty Blue / Lavender), switchable anytime.
- **Import / export**: share a single recipe as text, export all recipes as a JSON backup, or import recipes from a file.
- **Local-only storage**: data is kept in the phone's internal storage (`recipes.json`) — no network, no uploads.

## Install on an Android phone

1. Copy `温馨食谱-v1.0.apk` (or `WarmRecipe-v1.0.apk`) to the phone (WeChat/QQ file transfer, USB, or cloud drive).
2. Tap the APK to install; if prompted about "unknown sources", allow "Install unknown apps" (the system guides you on first install).
3. The "温馨食谱" icon appears on the home screen — tap to open.

> Requires Android 8.0 (API 26) or later, which covers the vast majority of Android devices.

## Project structure

```
食谱/
├─ app/                        # app source
│  ├─ AndroidManifest.xml
│  ├─ res/                     # layouts / styles (6 palettes) / drawables / icons
│  └─ src/com/warmrecipes/app/ # Java sources
│     ├─ MainActivity.java     # home list + search + category filter
│     ├─ DetailActivity.java   # detail (ingredients -> steps)
│     ├─ EditActivity.java     # create / edit
│     ├─ ThemeActivity.java    # theme picker
│     ├─ Recipe.java           # data model + duration estimate
│     ├─ RecipeStore.java      # JSON local storage
│     ├─ Palette.java          # palette definitions
│     └─ ThemeManager.java     # theme read/apply
├─ sdk/                        # local Android SDK (build-tools 35.0.0 + android-35)
├─ tools/                      # Fetch (downloader) / MakeIcon (icon generator)
├─ build.ps1                   # one-shot build script (no Gradle)
├─ release.keystore            # signing key (password: recipe123 — keep safe / replace)
└─ 温馨食谱-v1.0.apk           # build output (installable)
```

## Rebuilding

Requires JDK 24; `sdk/` already contains build-tools and the platform. From the project root:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
.\build.ps1
```

Pipeline: `aapt2 compile resources → link to generate R.java → javac → d8 dex → package → zipalign → apksigner sign`.
The output `WarmRecipe-v1.0.apk` is copied back to the project root.

> Note: aapt2 and other native tools cannot open non-ASCII paths on Windows, so the script stages the build in the system temp directory (ASCII path) and copies the APK back.

## Notes

- Step durations support forms like `10分钟`, `1小时30分钟`; the detail page sums them into a total time.
- Selecting a category auto-fills its default icon (you can still pick another one manually).
