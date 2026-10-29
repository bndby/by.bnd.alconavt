#!/usr/bin/env bash
# План выпуска по тегам vX.Y.Z и новым пунктам CHANGELOG.md.
# Тегов не создаёт и в репозиторий не коммитит.
set -euo pipefail

if [[ $# -ne 2 ]]; then
  printf 'usage: release-version.sh <repo> <notes-file>\n' >&2
  exit 2
fi

repo=$1
notes_file=$2

fail() {
  printf 'release-version: %s\n' "$1" >&2
  exit 1
}

git -C "$repo" rev-parse --is-inside-work-tree >/dev/null 2>&1 \
  || fail "не git-репозиторий: $repo"

version_gt() {
  local a1 a2 a3 b1 b2 b3
  IFS=. read -r a1 a2 a3 <<<"$1"
  IFS=. read -r b1 b2 b3 <<<"$2"
  if ((10#$a1 > 10#$b1)); then return 0; fi
  if ((10#$a1 < 10#$b1)); then return 1; fi
  if ((10#$a2 > 10#$b2)); then return 0; fi
  if ((10#$a2 < 10#$b2)); then return 1; fi
  ((10#$a3 > 10#$b3))
}

apply_bump() {
  local base=$1 bump=$2
  local major minor patch
  IFS=. read -r major minor patch <<<"$base"
  case "$bump" in
    major)
      major=$((10#$major + 1))
      minor=0
      patch=0
      ;;
    minor)
      minor=$((10#$minor + 1))
      patch=0
      ;;
    patch)
      patch=$((10#$patch + 1))
      ;;
    *) fail "внутренний уровень $bump" ;;
  esac
  printf '%s.%s.%s' "$major" "$minor" "$patch"
}

semver_tags=()
while IFS= read -r tag; do
  [[ -z "$tag" ]] && continue
  [[ "$tag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]] || continue
  semver_tags+=("$tag")
done < <(git -C "$repo" tag -l 'v*')

max=""
for tag in "${semver_tags[@]+"${semver_tags[@]}"}"; do
  ver=${tag#v}
  if [[ -z "$max" ]] || version_gt "$ver" "$max"; then
    max=$ver
  fi
done

head_tags=()
while IFS= read -r tag; do
  [[ -z "$tag" ]] && continue
  [[ "$tag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]] || continue
  head_tags+=("$tag")
done < <(git -C "$repo" tag --points-at HEAD)

if ((${#head_tags[@]} > 1)); then
  fail "на коммите больше одного тега выпуска: ${head_tags[*]}"
fi

bump_rank=1
notes_lines=()

raise_bump() {
  local rank
  case "$1" in
    patch) rank=1 ;;
    minor) rank=2 ;;
    major) rank=3 ;;
    *) fail "внутренний уровень $1" ;;
  esac
  if ((rank > bump_rank)); then
    bump_rank=$rank
  fi
}

parse_line() {
  local line=$1
  line=${line%$'\r'}
  [[ -z "$line" ]] && return 0
  if [[ "$line" =~ ^-\ (patch|minor|major):\ (.*)$ ]]; then
    local level="${BASH_REMATCH[1]}"
    local text="${BASH_REMATCH[2]}"
    if [[ -z "${text//[[:space:]]/}" ]]; then
      fail "пустая фраза в пункте: $line"
    fi
    raise_bump "$level"
    notes_lines+=("- $text")
    return 0
  fi
  if [[ "$line" =~ ^-\ ([A-Za-z]+): ]]; then
    local word="${BASH_REMATCH[1]}"
    case "$word" in
      patch | minor | major) fail "после уровня нужна фраза: $line" ;;
      *) fail "неизвестный уровень «$word»" ;;
    esac
  fi
  if [[ "$line" =~ ^-\ (.+)$ ]]; then
    raise_bump patch
    notes_lines+=("- ${BASH_REMATCH[1]}")
  fi
}

collect_diff() {
  local prev_tag=$1
  local raw line
  while IFS= read -r raw || [[ -n "$raw" ]]; do
    [[ "$raw" == +++* ]] && continue
    [[ "$raw" == +* ]] || continue
    line=${raw:1}
    parse_line "$line"
  done < <(git -C "$repo" diff -U0 "$prev_tag" HEAD -- CHANGELOG.md)
}

collect_file() {
  local line
  git -C "$repo" cat-file -e HEAD:CHANGELOG.md 2>/dev/null || return 0
  while IFS= read -r line || [[ -n "$line" ]]; do
    parse_line "$line"
  done < <(git -C "$repo" show HEAD:CHANGELOG.md)
}

if ((${#head_tags[@]} == 1)); then
  mode=upload
  version=${head_tags[0]#v}
  prev=""
  for tag in "${semver_tags[@]+"${semver_tags[@]}"}"; do
    ver=${tag#v}
    if [[ "$ver" == "$version" ]]; then
      continue
    fi
    if version_gt "$version" "$ver"; then
      if [[ -z "$prev" ]] || version_gt "$ver" "$prev"; then
        prev=$ver
      fi
    fi
  done
  if [[ -n "$prev" ]]; then
    collect_diff "v$prev"
  else
    collect_file
  fi
else
  mode=create
  if [[ -n "$max" ]]; then
    collect_diff "v$max"
    base=$max
  else
    collect_file
    base=0.0.0
  fi
  case "$bump_rank" in
    3) bump=major ;;
    2) bump=minor ;;
    *) bump=patch ;;
  esac
  version=$(apply_bump "$base" "$bump")
  if [[ -n "$max" ]] && ! version_gt "$version" "$max"; then
    fail "новая версия $version не больше $max"
  fi
fi

version_code=1
for tag in "${semver_tags[@]+"${semver_tags[@]}"}"; do
  if version_gt "$version" "${tag#v}"; then
    version_code=$((version_code + 1))
  fi
done

mkdir -p "$(dirname "$notes_file")"
if ((${#notes_lines[@]} == 0)); then
  printf '%s\n' 'В этой сборке заметки не менялись.' >"$notes_file"
else
  printf '%s\n' "${notes_lines[@]}" >"$notes_file"
fi

printf 'mode=%s\nversion=%s\nversion_code=%s\n' "$mode" "$version" "$version_code"
