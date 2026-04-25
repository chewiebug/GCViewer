---
name: ci-act-run
description: Run the full build-and-deploy.yaml workflow locally via act + Docker (pull_request path, DRY_RUN=true, dummy secrets).
compatibility: opencode
---

## What I do
- Run the complete `.github/workflows/build-and-deploy.yaml` workflow inside Docker using `act`.
- Covers every step: Checkout, `actions/setup-java@v5` (Temurin JDK 8), Xvfb headless display, `cicd/gcviewer-script.sh` (verify path), Codecov upload (best-effort).
- Always uses `DRY_RUN=true` and dummy secrets — safe for local testing, no actual deploys or pushes.
- The archive step is automatically skipped because the workflow guards it with `!env.ACT`.
- Uses the Docker image already configured in `.actrc`: `catthehacker/ubuntu:full-latest`.

## Critical Constraints

- **NEVER pass `--secret GITHUB_TOKEN=...`** (or any value for `GITHUB_TOKEN`) when `DRY_RUN=true`.
  Providing this secret causes the run to fail. Leave it completely unset — do not include the flag at all.

## When to use me
- When you want to validate the full workflow pipeline (all Actions steps, not just the shell script).
- Use the `ci-build-dry-run` skill instead if you only want to test `cicd/gcviewer-script.sh` paths (snapshot/release) without Docker.

## Workflow

### 1) Verify prerequisites
Run the following checks. If any fail, stop and report the issue.

- Docker daemon is running:
  - `docker info`
- `act` is installed:
  - `act --version`
- Current directory is the GCViewer repo root:
  - `git rev-parse --show-toplevel`
- Required files exist:
  - `.github/workflows/build-and-deploy.yaml`
  - `cicd/gcviewer-script.sh`
  - `.actrc` (configures the Docker image — must contain `-P ubuntu-latest=catthehacker/ubuntu:full-latest`)

### 2) Warn about first-run image pull
If the Docker image `catthehacker/ubuntu:full-latest` has not been pulled before, inform the user:
- The image is large (several GB) and the first pull can take several minutes.
- Subsequent runs use the cached image and start much faster.

### 3) Ask whether to override the Java matrix version
The workflow matrix defaults to multiple java versions. Ask:
- `all` (no `--matrix` flag added)
- `Java 8`
- `Java 17`
- `Java 21`
- `Java 25`
- `cancel`

If user selects `cancel`, stop.

If the user selects anything other than the default, append `--matrix java:<version>` to the act command.

### 4) Execute `act pull_request`
Run the appropriate command from the repo root.

> **WARNING:** Do NOT include `--secret GITHUB_TOKEN=<anything>` in the command.
> When `DRY_RUN=true`, passing GITHUB_TOKEN will cause the run to fail.
> It must be absent from the command entirely.

#### Default (all java versions, no matrix override)
```bash
act pull_request \
  -W .github/workflows/build-and-deploy.yaml \
  --env DRY_RUN=true \
  --secret ENCRYPTION_PASSWORD=dummy \
  --secret CI_DEPLOY_USERNAME=dummy \
  --secret CI_DEPLOY_PASSWORD=dummy \
  --secret CODECOV_TOKEN=dummy
```

#### With Java matrix override (example: Java 17)
```bash
act pull_request \
  -W .github/workflows/build-and-deploy.yaml \
  --matrix java:17 \
  --env DRY_RUN=true \
  --secret ENCRYPTION_PASSWORD=dummy \
  --secret CI_DEPLOY_USERNAME=dummy \
  --secret CI_DEPLOY_PASSWORD=dummy \
  --secret CODECOV_TOKEN=dummy
```

### 5) Summarize results
Report:
- Steps executed in order:
  1. Set up job
  2. Checkout (full history for releases)
  3. Set up JDK (Temurin)
  4. Prepare GUI display for tests (Xvfb)
  5. Show environment
  6. Make build script executable
  7. Build/Verify/Release per branch logic
  8. Upload coverage to Codecov (best-effort, non-fatal)
  9. Archive build artifacts — **skipped** (guarded by `!env.ACT`)
- Confirmation that `cicd/gcviewer-script.sh` took the `perform_verify` path:
  - Expected because `act pull_request` sets `GITHUB_EVENT_NAME=pull_request`, which makes `CI_IS_PR=true` in the script.
- Key proof lines from the output, e.g.:
  - `CI_IS_PR = true`
  - `only verify`
  - `BUILD SUCCESS`
- Any step warnings or failures observed.
