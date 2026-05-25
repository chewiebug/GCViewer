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
- `DRY_RUN` is configurable via the GitHub repository variable `DRY_RUN` (default `true`). With `DRY_RUN=true`, no actual deploys or pushes are made — safe for local testing.
- The archive step is automatically skipped because the workflow guards it with `!env.ACT`.
- Uses the Docker image already configured in `.actrc`: `catthehacker/ubuntu:full-latest`.

## Critical Constraints

- **When `DRY_RUN=true`**: Do **NOT** pass `--secret GITHUB_TOKEN=...`. Providing this secret causes the run to fail. Leave it completely unset — do not include the flag at all.
- **When `DRY_RUN=false`**:
  - For **snapshot builds** (`perform_snapshot_release`): `GITHUB_TOKEN` and `ENCRYPTION_PASSWORD` are **not required** (no GitHub push, no GPG decrypt). Required secrets: `CI_DEPLOY_USERNAME`, `CI_DEPLOY_PASSWORD` (Sonatype OSSRH), `SCP_USERNAME`, `SCP_PASSWORD` (SourceForge SCP), `CODECOV_TOKEN`.
  - For **release builds** (`perform_release`): all secrets are required: `GITHUB_TOKEN`, `ENCRYPTION_PASSWORD`, `CI_DEPLOY_USERNAME`, `CI_DEPLOY_PASSWORD`, `SCP_USERNAME`, `SCP_PASSWORD`, `CODECOV_TOKEN`.
  - All secrets must be real, non-dummy values stored in a `.env` file at the project root. Pass them via `--secret-file .env`. The `.env` file must never be committed to git and never be read by the agent.

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

  Required variables and their meaning:

  | Variable | Required for | Description |
  |---|---|---|
  | `GITHUB_TOKEN` | Release only | Personal Access Token with `repo` scope — used by `push_to_github()` for pushes and tags. **Not needed for snapshot builds.** |
  | `ENCRYPTION_PASSWORD` | Release only | Maven/GPG encryption password for signing artifacts. **Not needed for snapshot builds.** |
  | `CI_DEPLOY_USERNAME` | Snapshot & Release | Username for Sonatype OSSRH Maven deploy server |
  | `CI_DEPLOY_PASSWORD` | Snapshot & Release | Password for Sonatype OSSRH Maven deploy server |
  | `SCP_USERNAME` | Snapshot & Release | SCP username for SourceForge file upload (used by `sourceforge-release` Maven profile) |
  | `SCP_PASSWORD` | Snapshot & Release | SCP password for SourceForge file upload |
  | `CODECOV_TOKEN` | Snapshot & Release | Codecov upload token |

  Required `.env` file format (one `KEY=value` pair per line, no quotes needed):
  ```
  CI_DEPLOY_USERNAME=your_sonatype_username
  CI_DEPLOY_PASSWORD=your_sonatype_password
  SCP_USERNAME=your_sourceforge_username
  SCP_PASSWORD=your_sourceforge_password
  CODECOV_TOKEN=your_codecov_token
  ```

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

> **REMINDER:** Use `--var DRY_RUN=true/false` instead of `--env DRY_RUN=true/false`. The workflow reads DRY_RUN from the GitHub repository variable (`${{ vars.DRY_RUN }}`) via the `--var` flag in `act`. `--env` is overridden by the workflow's `env` block and has no effect.
> When `DRY_RUN=true`, do NOT include `--secret GITHUB_TOKEN=<anything>` — it causes the run to fail.
> When `DRY_RUN=false`, secrets are read from `.env` via `--secret-file .env`.

#### pull_request + DRY_RUN=true (default, safe)
```bash
act pull_request \
  -W .github/workflows/build-and-deploy.yaml \
  --var DRY_RUN=true \
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
  --var DRY_RUN=true \
  --secret ENCRYPTION_PASSWORD=dummy \
  --secret CI_DEPLOY_USERNAME=dummy \
  --secret CI_DEPLOY_PASSWORD=dummy \
  --secret CODECOV_TOKEN=dummy
```

#### pull_request + DRY_RUN=false
```bash
act pull_request \
  -W .github/workflows/build-and-deploy.yaml \
  --var DRY_RUN=false \
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
  --var DRY_RUN=true \
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
  --var DRY_RUN=true \
  --env GITHUB_REF_NAME=develop \
  --secret ENCRYPTION_PASSWORD=dummy \
  --secret CI_DEPLOY_USERNAME=dummy \
  --secret CI_DEPLOY_PASSWORD=dummy \
  --secret CODECOV_TOKEN=dummy
```

#### develop snapshot + DRY_RUN=false
Uses `--secret-file .env`. For snapshot builds, only `CI_DEPLOY_USERNAME`, `CI_DEPLOY_PASSWORD`, `SCP_USERNAME`, `SCP_PASSWORD`, and `CODECOV_TOKEN` are required. `GITHUB_TOKEN` and `ENCRYPTION_PASSWORD` are optional (only used by the release path).
```bash
act push \
  -W .github/workflows/build-and-deploy.yaml \
  --matrix java:8 \
  --var DRY_RUN=false \
  --env GITHUB_REF_NAME=develop \
  --secret-file .env
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
    - With `DRY_RUN=false`
      - runs `mvn clean deploy javadoc:javadoc -P sourceforge-release`. for openjdk8 only
      - runs `mvn clean verify javadoc:javadoc` for all other openjdk versions
- Key proof lines from the output, e.g.:
  - `CI_IS_PR = true` or `CI_IS_PR = false`
  - `CI_BRANCH = develop` (for develop snapshot)
  - `only verify` (pull_request path) or `build and deploy to sourceforge (SNAPSHOT only)` (develop snapshot path)
  - `DRY_RUN = true` or `DRY_RUN = false`
  - `BUILD SUCCESS`
- Any step warnings or failures observed.
