## Agent skills

Always respond in russian

### Issue tracker

Issues живут как markdown-файлы в `.scratch/<feature>/`. См. `docs/agents/issue-tracker.md`.

### Triage labels

Пять канонических меток: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. См. `docs/agents/triage-labels.md`.

### Domain docs

Single-context: один `GLOSSARY.md` и `docs/adr/` в корне репозитория. См. `docs/agents/domain.md`.

### Заметки выпуска

Меняешь поведение игры в `app/` — допиши в конец `CHANGELOG.md` одну строку и одну фразу о том, что изменилось для игрока. Уровень — первое слово строки: `- patch: `, `- minor: ` или `- major: `.

- `patch` — починка, нового поведения нет, прогресс читается.
- `minor` — новое поведение или место в игре, прогресс того же мажора читается.
- `major` — установленный прогресс этой подписи больше не читается.

Готово, когда новая строка стоит в конце, уровень — одно из этих трёх слов, а прежние строки целы. Почему так устроена тройка: `docs/adr/0003-troyka-vypuska.md`.
