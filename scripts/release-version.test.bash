#!/usr/bin/env bash
set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
script="$script_dir/release-version.sh"
repos=()

cleanup() {
  if ((${#repos[@]} > 0)); then
    rm -rf "${repos[@]}"
  fi
}
trap cleanup EXIT

new_repo() {
  local dir
  dir=$(mktemp -d)
  repos+=("$dir")
  git init -q -b main "$dir"
  git -C "$dir" config user.email "release-test@example.com"
  git -C "$dir" config user.name "release-test"
  printf '%s\n' "$dir"
}

add_commit() {
  local repo=$1 path=$2 msg=$3
  mkdir -p "$(dirname "$repo/$path")"
  cat >"$repo/$path"
  git -C "$repo" add -- "$path"
  git -C "$repo" commit -q -m "$msg"
}

plan() {
  local repo=$1
  notes=$(mktemp)
  out=$(bash "$script" "$repo" "$notes")
  mode=$(sed -n 's/^mode=//p' <<<"$out")
  version=$(sed -n 's/^version=//p' <<<"$out")
  version_code=$(sed -n 's/^version_code=//p' <<<"$out")
}

expect() {
  local label=$1 repo=$2 want_mode=$3 want_version=$4 want_code=$5
  shift 5
  plan "$repo"
  if [[ "$mode" != "$want_mode" || "$version" != "$want_version" || "$version_code" != "$want_code" ]]; then
    printf 'FAIL %s\n got mode=%s version=%s code=%s\n want mode=%s version=%s code=%s\n' \
      "$label" "$mode" "$version" "$version_code" "$want_mode" "$want_version" "$want_code" >&2
    exit 1
  fi
  local want notes_text
  want=$(mktemp)
  printf '%s\n' "$@" >"$want"
  notes_text=$(cat "$notes")
  if [[ "$notes_text" != "$(cat "$want")" ]]; then
    printf 'FAIL %s notes\n--- got ---\n%s\n--- want ---\n%s\n' "$label" "$notes_text" "$(cat "$want")" >&2
    exit 1
  fi
  printf 'ok %s\n' "$label"
}

expect_fail() {
  local label=$1 repo=$2 needle=$3
  local notes err status
  notes=$(mktemp)
  err=$(mktemp)
  set +e
  bash "$script" "$repo" "$notes" >"$(mktemp)" 2>"$err"
  status=$?
  set -e
  if [[ "$status" -eq 0 ]] || ! grep -q "$needle" "$err"; then
    printf 'FAIL %s status=%s\n%s\n' "$label" "$status" "$(cat "$err")" >&2
    exit 1
  fi
  printf 'ok %s\n' "$label"
}

test_empty() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" README.md "init" <<'EOF'
init
EOF
  expect empty "$repo" create 0.0.1 1 "В этой сборке заметки не менялись."
}

test_minor() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
# Заметки

- minor: Карта улицы
EOF
  expect minor "$repo" create 0.1.0 1 "- Карта улицы"
}

test_highest_of_many_minors() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- minor: Один
- minor: Два
- patch: Три
EOF
  expect many-minors "$repo" create 0.1.0 1 "- Один" "- Два" "- Три"
}

test_major() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- patch: Жест
- minor: Карта
- major: Старый заход больше не открывается
EOF
  expect major "$repo" create 1.0.0 1 \
    "- Жест" "- Карта" "- Старый заход больше не открывается"
}

test_unknown_level() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- minr: ошибка
EOF
  expect_fail unknown "$repo" "неизвестный уровень"
}

test_colon_sentence_is_patch() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- Починили жест: бутылка больше не залипает
EOF
  expect colon "$repo" create 0.0.1 1 "- Починили жест: бутылка больше не залипает"
}

test_bump_from_previous_tag() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "base" <<'EOF'
- minor: Карта
EOF
  git -C "$repo" tag v0.4.2
  add_commit "$repo" CHANGELOG.md "next" <<'EOF'
- minor: Карта
- patch: Жест переливания
EOF
  expect from-tag "$repo" create 0.4.3 2 "- Жест переливания"
}

test_minor_resets_patch() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" README.md "base" <<'EOF'
base
EOF
  git -C "$repo" tag v0.4.2
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- minor: Карточка напитка
EOF
  expect minor-reset "$repo" create 0.5.0 2 "- Карточка напитка"
}

test_major_resets() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" README.md "base" <<'EOF'
base
EOF
  git -C "$repo" tag v0.4.2
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- major: Старый заход больше не открывается
EOF
  expect major-reset "$repo" create 1.0.0 2 "- Старый заход больше не открывается"
}

test_semver_order() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" README.md "a" <<'EOF'
a
EOF
  git -C "$repo" tag v0.9.0
  add_commit "$repo" README.md "b" <<'EOF'
b
EOF
  git -C "$repo" tag v0.10.0
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- patch: Жест
EOF
  expect semver-order "$repo" create 0.10.1 3 "- Жест"
}

test_marker_from_earlier_commit() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" README.md "base" <<'EOF'
base
EOF
  git -C "$repo" tag v0.1.0
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- major: Старый заход больше не открывается
EOF
  add_commit "$repo" app/src/main/Demo.kt "code" <<'EOF'
class Demo
EOF
  expect waiting-marker "$repo" create 1.0.0 2 "- Старый заход больше не открывается"
}

test_code_without_notes_is_patch() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "base" <<'EOF'
- minor: Карта
EOF
  git -C "$repo" tag v0.3.4
  add_commit "$repo" app/src/main/Demo.kt "fix" <<'EOF'
class Demo
EOF
  expect silent-patch "$repo" create 0.3.5 2 "В этой сборке заметки не менялись."
}

test_upload_same_commit() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" CHANGELOG.md "notes" <<'EOF'
- minor: Карта
EOF
  git -C "$repo" tag v0.0.1
  add_commit "$repo" CHANGELOG.md "more" <<'EOF'
- minor: Карта
- minor: Карточка
EOF
  git -C "$repo" tag v0.1.0
  expect upload "$repo" upload 0.1.0 2 "- Карточка"
}

test_two_tags_on_head() {
  local repo
  repo=$(new_repo)
  add_commit "$repo" README.md "init" <<'EOF'
init
EOF
  git -C "$repo" tag v0.0.1
  git -C "$repo" tag v0.0.2
  expect_fail two-tags "$repo" "больше одного тега"
}

test_empty
test_minor
test_highest_of_many_minors
test_major
test_unknown_level
test_colon_sentence_is_patch
test_bump_from_previous_tag
test_minor_resets_patch
test_major_resets
test_semver_order
test_marker_from_earlier_commit
test_code_without_notes_is_patch
test_upload_same_commit
test_two_tags_on_head
