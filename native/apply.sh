#!/usr/bin/env bash
# 'npx cap add android' 직후에 실행: 안드로이드 프로젝트에 네이티브 코드(키 입력 → 효과음)를 넣는다.
set -e
PKG_DIR=android/app/src/main/java/com/haepari/papyrustyping
RES=android/app/src/main/res
cp native/MainActivity.java native/PapyrusPlugin.java native/PapyrusKeyService.java "$PKG_DIR/"
mkdir -p "$RES/xml" "$RES/raw"
cp native/accessibility_service_config.xml "$RES/xml/"
cp native/papyrus_strings.xml "$RES/values/"
cp sounds/*.wav "$RES/raw/"
python3 native/patch_manifest.py
echo "native files applied"
