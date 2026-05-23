#!/usr/bin/env bash
# ============================================================================
# rename-mod.sh  —  Shard's Compats: Voltaic-Sable Compat
#
# Renames:   modid    voltaic_sable_compat        -> shards_voltaic_sable_compat
#            package  com.trevorgolden.<modid>    -> com.sharddevs.<modid>
#            authors / URLs  trevorgolden         -> sharddevs
#
# Run from the PROJECT ROOT (the folder containing build.gradle) in Git Bash.
#   cd /c/modwork/voltaic_sable_compat
#   bash rename-mod.sh
#
# It works on a new branch, so it is fully reviewable and revertible.
# It deliberately does NOT touch the Maven publish URL / credentials
# (maven.trevorgolden.com, TrevorGoldenMaven, mavenTrevor* properties) —
# see the handoff notes; that is a separate task needing new infrastructure.
# ============================================================================
set -euo pipefail

OLD_MODID="voltaic_sable_compat"
NEW_MODID="shards_voltaic_sable_compat"
OLD_SEG="trevorgolden"
NEW_SEG="sharddevs"
OLD_PKG_DIR="com/trevorgolden/voltaic_sable_compat"
NEW_PKG_DIR="com/sharddevs/shards_voltaic_sable_compat"
BRANCH="rename/shards_voltaic_sable_compat"

echo "== sanity checks =="
[ -f build.gradle ] || { echo "ERROR: no build.gradle here — run from the project root."; exit 1; }
command -v git >/dev/null || { echo "ERROR: git not found on PATH."; exit 1; }
if [ -n "$(git status --porcelain)" ]; then
  echo "ERROR: git working tree is not clean."
  echo "       Commit or stash your changes first, so this rename is the only diff."
  exit 1
fi

echo "== creating branch: $BRANCH =="
git checkout -b "$BRANCH"

echo "== locating source roots =="
JAVA_ROOT="$(find . -type d -path '*/src/main/java' | head -1)"
RES_ROOT="$(find . -type d -path '*/src/main/resources' | head -1)"
[ -n "$JAVA_ROOT" ] || { echo "ERROR: src/main/java not found."; exit 1; }
[ -n "$RES_ROOT" ]  || { echo "ERROR: src/main/resources not found."; exit 1; }

PKG_SRC="$JAVA_ROOT/$OLD_PKG_DIR"
PKG_DST="$JAVA_ROOT/$NEW_PKG_DIR"
[ -d "$PKG_SRC" ] || { echo "ERROR: package dir not found: $PKG_SRC"; exit 1; }

echo "== moving package: $OLD_PKG_DIR -> $NEW_PKG_DIR =="
mkdir -p "$JAVA_ROOT/com/sharddevs"
git mv "$PKG_SRC" "$PKG_DST"
rmdir "$JAVA_ROOT/com/trevorgolden" 2>/dev/null || true

echo "== rewriting .java files (package, imports, @Mod id, mixin handler prefixes, vendor) =="
find "$PKG_DST" -name '*.java' -print0 | while IFS= read -r -d '' f; do
  sed -i -e "s/${OLD_MODID}/${NEW_MODID}/g" \
         -e "s/${OLD_SEG}/${NEW_SEG}/g" "$f"
  echo "   patched: $f"
done

echo "== mixin config: rename + rewrite internal package =="
MIXJSON="$(find "$RES_ROOT" -maxdepth 2 -name "${OLD_MODID}.mixins.json" | head -1)"
[ -n "$MIXJSON" ] || { echo "ERROR: ${OLD_MODID}.mixins.json not found."; exit 1; }
NEWMIX="$(dirname "$MIXJSON")/${NEW_MODID}.mixins.json"
git mv "$MIXJSON" "$NEWMIX"
sed -i -e "s/${OLD_MODID}/${NEW_MODID}/g" \
       -e "s/${OLD_SEG}/${NEW_SEG}/g" "$NEWMIX"
echo "   $MIXJSON -> $NEWMIX"

echo "== rewriting neoforge.mods.toml =="
TOML="$(find "$RES_ROOT" -name 'neoforge.mods.toml' | head -1)"
[ -n "$TOML" ] || { echo "ERROR: neoforge.mods.toml not found."; exit 1; }
sed -i -e "s/${OLD_MODID}/${NEW_MODID}/g" \
       -e "s/${OLD_SEG}/${NEW_SEG}/g" "$TOML"
sed -i "s|displayName = \"Voltaic-Sable Compat\"|displayName = \"Shard's Compats: Voltaic-Sable Compat\"|" "$TOML"
echo "   patched: $TOML"

echo "== rewriting build.gradle (targeted — Maven publish URL/creds left untouched) =="
sed -i -e "s/${OLD_MODID}/${NEW_MODID}/g" \
       -e "s/group = 'com\.trevorgolden'/group = 'com.sharddevs'/" \
       -e "s/'trevorgolden'/'sharddevs'/g" \
       build.gradle
echo "   patched: build.gradle"

echo "== rewriting settings.gradle (rootProject.name), if present =="
for s in settings.gradle settings.gradle.kts; do
  if [ -f "$s" ]; then
    sed -i -e "s/${OLD_MODID}/${NEW_MODID}/g" "$s"
    echo "   patched: $s"
  fi
done

echo "== staging all changes =="
git add -A

echo
echo "============================================================"
echo " RENAME COMPLETE — nothing committed yet."
echo
echo " Review:   git diff --staged -M"
echo " Build:    ./gradlew.bat clean build"
echo " Expect:   build/libs/${NEW_MODID}-1.0.0.jar"
echo
echo " If something looks wrong, revert everything with:"
echo "   git checkout main && git branch -D ${BRANCH}"
echo "============================================================"
