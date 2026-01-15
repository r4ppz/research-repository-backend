# Research Repository Backend

## Collaboration Guide

To maintain code quality and avoid configuration conflicts, all developers must follow this workflow.

### Branch Strategy

We use a two-branch system. **Direct pushes to `main` and `dev` are blocked.**

```text
  main (Production)  <-- [Owner Only Merge]
    ↑
   dev  (Development)  <-- [Shared Staging]
    ↑
 [feature-branch]      <-- [Developer Work]

```

- **`main`**: Only for stable, production-ready releases.
- **`dev`**: The primary integration branch. All work meets here first.

---

### Development Flow

1. **Issue First (if possible)**: Before starting any work, create/assign a **GitHub Issue**. This prevents us from working on the same feature or bug simultaneously.
2. **Branch Out**: Create a local branch from `dev`.

```bash
git checkout dev
git pull origin dev
git checkout -b feat/issue-number-description

```

3. **Implementation**: Follow the technical specifications documented in the [Docs/Specs Repository](https://github.com/r4ppz/research-repo-docs).
4. **Local Config**: Use the provided `.env-example` to create your local `.env`. Do **not** commit your `.env` file.
5. **Pull Request (PR)**:

- Push your branch to GitHub.
- Open a PR targeting the **`dev`** branch.
- CI must pass (tests and Docker build).
- Optional: **1 approval** from the other dev before merging.

6. **Release**: Once `dev` is stable and verified, the **Repo Owner** will merge `dev` into `main` for the final release.
