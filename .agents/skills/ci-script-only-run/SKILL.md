---
name: ci-script-only-run
description: Simulate GCViewer CI paths (pull request, develop snapshot, master release) with DRY_RUN and selectable OpenJDK 8/17/21/25.
compatibility: opencode
---

## What I do
- Simulate GCViewer CI behavior locally without deploying.
- Reproduce one of three CI paths:
  - pull request verification
  - develop snapshot (dry-run deploy path)
  - master release (dry-run release/push path)
- Allow selecting Java runtime from:
  - `E:/swdevelopment/java/openjdk-8`
  - `E:/swdevelopment/java/openjdk-17`
  - `E:/swdevelopment/java/openjdk-21`
  - `E:/swdevelopment/java/openjdk-25`

## When to use me
Use this when you want a local CI-equivalent dry run of `cicd/gcviewer-script.sh` and a concise proof summary of the path taken and dry-run behavior.

## Workflow

### 1) Confirm repository and prerequisites
- Verify current workspace is GCViewer repo root:
  - `git rev-parse --show-toplevel`
  - ensure `cicd/gcviewer-script.sh` exists
- Verify required tools:
  - `bash --version`
  - `mvn -v`
- If any prerequisite fails, stop and report the issue.

### 2) Ask which CI dry run to simulate
Ask:
- `pull request`
- `develop snapshot`
- `master release`
- `cancel`

If user selects `cancel`, stop.

### 3) Ask which JDK to use
Ask:
- `openjdk-8`
- `openjdk-17`
- `openjdk-21`
- `openjdk-25`
- `cancel`

If user selects `cancel`, stop.

Set:
- `JAVA_HOME` = `E:/swdevelopment/java/<selected-jdk>`
- `DRY_RUN=true` for all runs.

### 4) Execute selected dry run via Git Bash
Run the appropriate command from the repo root. Replace `<selected-jdk>` with `openjdk-8`, `openjdk-17`, `openjdk-21`, or `openjdk-25`.

#### Option A: pull request
```bash
JAVA_HOME=E:/swdevelopment/java/<selected-jdk> \
DRY_RUN=true \
GITHUB_EVENT_NAME=pull_request \
GITHUB_HEAD_REF=feature/local-test \
GITHUB_REF_NAME=develop \
CI_JDK_VERSION=openjdk21 \
CI_COMMIT_MESSAGE='PR dry run' \
bash cicd/gcviewer-script.sh
```

#### Option B: develop snapshot
```bash
JAVA_HOME=E:/swdevelopment/java/<selected-jdk> \
DRY_RUN=true \
GITHUB_EVENT_NAME=push \
GITHUB_REF_NAME=develop \
CI_JDK_VERSION=openjdk8 \
CI_COMMIT_MESSAGE='develop dry run' \
bash cicd/gcviewer-script.sh
```

#### Option C: master release
```bash
JAVA_HOME=E:/swdevelopment/java/<selected-jdk> \
DRY_RUN=true \
GITHUB_EVENT_NAME=push \
GITHUB_REF_NAME=master \
CI_JDK_VERSION=openjdk8 \
CI_COMMIT_MESSAGE='master dry run' \
GITHUB_TOKEN=dummy \
ENCRYPTION_PASSWORD=dummy \
GPG_DIR=cicd/gpg \
bash cicd/gcviewer-script.sh
```

### 5) Summarize route and proof
Report:
- Which path ran:
  - PR verification path
  - develop snapshot path (dry-run deploy)
  - master release path (dry-run release/push)
- The selected `JAVA_HOME`
- Key output lines proving `DRY_RUN=true` behavior.
