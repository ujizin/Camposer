---
allowed-tools: Read, Glob, Grep, Bash, Write
---

Create a GitHub pull request for the current branch using the project PR template.

The base branch argument is: $ARGUMENTS

If no argument was provided, use `main` as the base branch.

## Steps

1. **Gather context:**
   ```bash
   git rev-parse --abbrev-ref HEAD          # current branch name
   git log <base>...HEAD --oneline          # commits on this branch
   git diff <base>...HEAD --name-only       # files changed
   git diff <base>...HEAD                   # full diff for summary
   ```

2. **Read the PR template** from `.github/pull_request_template.md`.

3. **Fill in the template** based on the diff and commit history:
   - **Summary:** 2-3 sentences describing what the PR does and why.
   - **Type of change:** Check the appropriate box(es).
   - **Checklist:** Check items that are already satisfied based on the diff. Leave unchecked items that the author must verify manually (test results, ABI check, etc.).
   - **Testing notes:** Describe what changed and which platforms are affected.

4. **Check if remote branch exists:**
   ```bash
   git ls-remote --heads origin $(git rev-parse --abbrev-ref HEAD)
   ```
   If not pushed, push first:
   ```bash
   git push -u origin HEAD
   ```

5. **Create the PR:**
   ```bash
   gh pr create \
     --base <base-branch> \
     --title "<title>" \
     --body "<filled-template>"
   ```

   Title format: `<type>(<scope>): <short description>` — follow Conventional Commits.
   Examples:
   - `feat(android): add night mode capture support`
   - `fix(ios): correct AVFoundation flash mode mapping`
   - `refactor: extract PinchToZoom into shared applier`

6. **Print the PR URL** after creation.

## Rules

- Never skip the template — always use `.github/pull_request_template.md` as the body base.
- Do not check items in the checklist that require running commands (tests, ABI) — leave those for the author unless you have evidence they passed.
- If the diff touches `commonMain` files, always verify the "No CameraX / AVFoundation imports" checklist item by grepping the changed files.
- If any `expect` declaration changed, check the KMP expect/actual item only if all 3 platform files are present in the diff.
