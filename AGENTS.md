### Documentation & Specs

**Scan docs before any code change.**

- **Hierarchy:** Local (`~/Project/research-repo-docs`) > Web ([r4ppz.github.io](https://r4ppz.github.io/research-repo-docs/)).
- **Stop Rule:** If docs are missing, **ask the user.** No guessing API paths or DB schema.
- **Verification:** If a task deviates from the `api_contract.md` or `specification.md`, warn the user before proceeding.

### Workflow

- **No Tests:** Do not attempt to scan, run or write unit tests yet.
- **Patterns:** Follow existing conventions used in the project. Or suggest a modern alternative if deprecated or bad code is encountered.
