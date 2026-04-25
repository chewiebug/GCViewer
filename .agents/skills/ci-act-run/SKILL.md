---
name: ci-act-run
description: Run the full build-and-deploy.yaml workflow locally via act + Docker. Supports pull_request and develop snapshot build paths, with configurable DRY_RUN.
compatibility: opencode
---

## What I do
- Run the complete `.github/workflows/build-and-deploy.yaml` workflow inside Docker using `act`.
- Covers every step: Checkout, `actions/setup-java@v5` (Temurin JDK 8), Xvfb headless display, `cicd/gcviewer-script.sh` (verify path), Codecov upload (best-effort).
- Supports two build paths selectable at runtime:
  - **pull_request** — simulates a PR build: `act pull_request` sets `CI_IS_PR=true` → script runs `perform_verify()`.
  - **develop snapshot** — simulates a push to the develop branch: `act push` with `GITHUB_REF_NAME=develop`, `CI_IS_PR=false`, `CI_BRANCH=develop` → script runs `perform_snapshot_release()`.
- `DRY_RUN` is configurable (default `true`). With `DRY_RUN=true`, no actual deploys or pushes are made — safe for local testing.
- The archive step is automatically skipped because the workflow guards it with `!env.ACT`.
- Uses the Docker image already configured in `.actrc`: `catthehacker/ubuntu:full-latest`.

## Critical Constraints

- **When `DRY_RUN=true`**: Do **NOT** pass `--secret GITHUB_TOKEN=...`. Providing this secret causes the run to fail. Leave it completely unset — do not include the flag at all.
- **When `DRY_RUN=false`**: `GITHUB_TOKEN` **is required** by `push_to_github()` inside `gcviewer-script.sh`. Pass it as `--secret GITHUB_TOKEN=<real_token>`. Also ensure `CI_DEPLOY_USERNAME`, `CI_DEPLOY_PASSWORD`, and `ENCRYPTION_PASSWORD` are real values — not dummies.

## When to use me
- When you want to validate the full workflow pipeline (all Actions steps, not just the shell script).
- Use the `ci-script-only-run` skill instead if you only want to test `cicd/gcviewer-script.sh` paths (snapshot/release) without Docker.

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

### 3) Ask which build path to simulate
Ask the user which CI event to simulate:

- `pull_request` — simulates a PR build; script takes `perform_verify()` path.
- `develop snapshot` — simulates a push to the `develop` branch; script takes `perform_snapshot_release()` path.
- `cancel`

If user selects `cancel`, stop.

Record the chosen build path for use in steps 5 and 6.

### 4) Ask about DRY_RUN
Ask the user whether to run with `DRY_RUN=true` or `DRY_RUN=false`:

- `true` *(recommended)* — skips actual deploys/pushes, safe for local testing. No real credentials needed.
- `false` — performs real deploys and pushes. Requires a real `GITHUB_TOKEN` and real deploy credentials.

If the user selects `DRY_RUN=false`:
- Display a clear warning:
  > **WARNING: DRY_RUN=false will perform real Maven deploys, GitHub pushes, and tag operations. This cannot be undone. Ensure all secrets are real values.**
- Ask the user to explicitly confirm they want to continue (yes/no). If they do not confirm, stop.

Record the chosen DRY_RUN value for use in step 6.

### 5) Ask whether to override the Java matrix version
The workflow matrix defaults to multiple java versions. Ask:
- `all` (no `--matrix` flag added)
- `Java 8`
- `Java 17`
- `Java 21`
- `Java 25`
- `cancel`

If user selects `cancel`, stop.

If the user selects anything other than `all`, append `--matrix java:<version>` to the act command.

### 6) Execute act

Run the appropriate command from the repo root based on the choices made in steps 3, 4, and 5.

> **REMINDER:** When `DRY_RUN=true`, do NOT include `--secret GITHUB_TOKEN=<anything>` — it causes the run to fail.
> When `DRY_RUN=false`, include `--secret GITHUB_TOKEN=<real_token>` and use real credentials for all other secrets.

#### pull_request + DRY_RUN=true (default, safe)
```bash
act pull_request \
  -W .github/workflows/build-and-deploy.yaml \
  --env DRY_RUN=true \
  --secret ENCRYPTION_PASSWORD=dummy \
  --secret CI_DEPLOY_USERNAME=dummy \
  --secret CI_DEPLOY_PASSWORD=dummy \
  --secret CODECOV_TOKEN=dummy
```

#### pull_request + DRY_RUN=true + Java matrix override (example: Java 17)
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

#### pull_request + DRY_RUN=false
```bash
act pull_request \
  -W .github/workflows/build-and-deploy.yaml \
  --env DRY_RUN=false \
  --secret GITHUB_TOKEN=<real_token> \
  --secret ENCRYPTION_PASSWORD=<real_password> \
  --secret CI_DEPLOY_USERNAME=<real_username> \
  --secret CI_DEPLOY_PASSWORD=<real_password> \
  --secret CODECOV_TOKEN=<real_token>
```

#### develop snapshot + DRY_RUN=true (safe)
```bash
act push \
  -W .github/workflows/build-and-deploy.yaml \
  --env DRY_RUN=true \
  --env GITHUB_REF_NAME=develop \
  --secret ENCRYPTION_PASSWORD=dummy \
  --secret CI_DEPLOY_USERNAME=dummy \
  --secret CI_DEPLOY_PASSWORD=dummy \
  --secret CODECOV_TOKEN=dummy
```

#### develop snapshot + DRY_RUN=true + Java matrix override (example: Java 17)
```bash
act push \
  -W .github/workflows/build-and-deploy.yaml \
  --matrix java:17 \
  --env DRY_RUN=true \
  --env GITHUB_REF_NAME=develop \
  --secret ENCRYPTION_PASSWORD=dummy \
  --secret CI_DEPLOY_USERNAME=dummy \
  --secret CI_DEPLOY_PASSWORD=dummy \
  --secret CODECOV_TOKEN=dummy
```

#### develop snapshot + DRY_RUN=false
```bash
act push \
  -W .github/workflows/build-and-deploy.yaml \
  --env DRY_RUN=false \
  --env GITHUB_REF_NAME=develop \
  --secret GITHUB_TOKEN=<real_token> \
  --secret ENCRYPTION_PASSWORD=<real_password> \
  --secret CI_DEPLOY_USERNAME=<real_username> \
  --secret CI_DEPLOY_PASSWORD=<real_password> \
  --secret CODECOV_TOKEN=<real_token>
```

### 7) Summarize results
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
- The script path taken, based on build options chosen:
  - **pull_request**: `CI_IS_PR=true` → `perform_verify()` (regardless of branch or DRY_RUN)
  - **develop snapshot**: `CI_IS_PR=false`, `CI_BRANCH=develop` → `perform_snapshot_release()`
    - With `DRY_RUN=true`: runs `mvn clean verify javadoc:javadoc` and logs what it would deploy.
    - With `DRY_RUN=false`: runs `mvn clean deploy javadoc:javadoc -P sourceforge-release`.
- Key proof lines from the output, e.g.:
  - `CI_IS_PR = true` or `CI_IS_PR = false`
  - `CI_BRANCH = develop` (for develop snapshot)
  - `only verify` (pull_request path) or `build and deploy to sourceforge (SNAPSHOT only)` (develop snapshot path)
  - `DRY_RUN = true` or `DRY_RUN = false`
  - `BUILD SUCCESS`
- Any step warnings or failures observed.
