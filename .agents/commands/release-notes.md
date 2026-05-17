---
allowed-tools: Bash, Read
---

Create a git tag and publish a GitHub release with formatted release notes.

The tag argument is: $ARGUMENTS

**If no tag argument was provided, stop immediately and tell the user:**
> Error: tag is required. Usage: `/release-notes v1.0.0`

---

## Steps

### 1. Validate the tag

Confirm `$ARGUMENTS` is non-empty and matches the version pattern (e.g. `v1.0.0`, `v1.2.0-beta01`, `v0.4.5`). If it doesn't match `v\d+\.\d+\.\d+`, warn the user but proceed if they confirm.

Check the tag doesn't already exist:
```bash
git tag --list "$ARGUMENTS"
```
If it exists, stop and report the conflict.

### 2. Find the previous tag

```bash
git tag --sort=-version:refname | head -1
```

If no tags exist, this is an initial release — skip the comparison range and collect all merged PRs from the repository start (see Step 3).

### 3. Collect merged PRs since the previous tag

If a previous tag exists:
```bash
gh pr list \
  --state merged \
  --base main \
  --search "merged:>=$(git log <prev-tag> -1 --format=%aI)" \
  --json number,title,author,url \
  --limit 50
```

If no previous tag (initial release):
```bash
gh pr list \
  --state merged \
  --base main \
  --json number,title,author,url \
  --limit 100
```

If `gh pr list` fails, fall back to commit log:
```bash
# With previous tag:
git log <prev-tag>...HEAD --oneline --merges
# Initial release:
git log --oneline --merges
```

### 4. Determine release type

| Condition | Release type |
|-----------|-------------|
| Tag contains `alpha`, `beta`, `rc` | Pre-release (`--prerelease` flag) |
| Major version bump (e.g. `v1.0.0` → `v2.0.0`) | Breaking — use `## ✨ What's Changed (Breaking changes)` heading |
| Otherwise | Standard — use `## What's Changed` heading |

### 5. Format the release notes body

Standard release:
```markdown
## What's Changed
* <PR title> by @<author-login> in <PR url>
* <PR title> by @<author-login> in <PR url>

**Full Changelog**: https://github.com/ujizin/Camposer/compare/<prev-tag>...<new-tag>
```

For an initial release, omit the compare URL or link to the full commit history.

Breaking / major release:
```markdown
## ✨ What's Changed (Breaking changes)

* <PR title> by @<author-login> in <PR url>

- <bullet summarising the key breaking change>
- <bullet for next significant change>

**Full Changelog**: https://github.com/ujizin/Camposer/compare/<prev-tag>...<new-tag>
```

> Note: a banner image is appended manually by the maintainer after the release is published.

### 6. Create the git tag and push it

```bash
git tag "$ARGUMENTS"
git push origin "$ARGUMENTS"
```

### 7. Create the GitHub release

Standard:
```bash
gh release create "$ARGUMENTS" \
  --title "$ARGUMENTS" \
  --notes "<formatted body>" \
  --latest
```

Pre-release:
```bash
gh release create "$ARGUMENTS" \
  --title "$ARGUMENTS" \
  --notes "<formatted body>" \
  --prerelease
```

### 8. Print the release URL

Output the URL returned by `gh release create` so the maintainer can open it and attach the banner image.
