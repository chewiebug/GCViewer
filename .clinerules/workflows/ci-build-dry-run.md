# CI Build Dry Run

Simulate GCViewer CI behavior for PR, develop (snapshot), or master (release) without deploying.

## Step 1: Confirm repository and prerequisites
Verify we are in the GCViewer repository root and that `bash` and `mvn` are available.
If prerequisites are missing, stop and report the issue.

## Step 2: Ask which CI dry run to simulate
<ask_followup_question>
  <question>Which CI dry run do you want to simulate?</question>
  <options>["pull request", "develop snapshot", "master release", "cancel"]</options>
</ask_followup_question>

If user selects "cancel", stop.

## Step 3: Run selected dry run
Use `DRY_RUN=true` and execute `cicd/gcviewer-script.sh` via Git Bash with CI-like env vars. JAVA_HOME is pinned to `E:/swdevelopment/java/openjdk-21` (Git Bash path form).

### Option A: pull request
<execute_command>
  <command>bash -lc "cd /d/Users/joerg2/Daten/java/git/GCViewer && JAVA_HOME='E:/swdevelopment/java/openjdk-21' DRY_RUN=true GITHUB_EVENT_NAME=pull_request GITHUB_HEAD_REF=feature/local-test GITHUB_REF_NAME=develop CI_JDK_VERSION=openjdk21 CI_COMMIT_MESSAGE='PR dry run' ./cicd/gcviewer-script.sh"</command>
  <requires_approval>false</requires_approval>
</execute_command>

### Option B: develop snapshot
<execute_command>
  <command>bash -lc "cd /d/Users/joerg2/Daten/java/git/GCViewer && JAVA_HOME='E:/swdevelopment/java/openjdk-21' DRY_RUN=true GITHUB_EVENT_NAME=push GITHUB_REF_NAME=develop CI_JDK_VERSION=openjdk8 CI_COMMIT_MESSAGE='develop dry run' ./cicd/gcviewer-script.sh"</command>
  <requires_approval>false</requires_approval>
</execute_command>

### Option C: master release
<execute_command>
  <command>bash -lc "cd /d/Users/joerg2/Daten/java/git/GCViewer && JAVA_HOME='E:/swdevelopment/java/openjdk-21' DRY_RUN=true GITHUB_EVENT_NAME=push GITHUB_REF_NAME=master CI_JDK_VERSION=openjdk8 CI_COMMIT_MESSAGE='master dry run' GITHUB_TOKEN=dummy ENCRYPTION_PASSWORD=dummy GPG_DIR=/d/Users/joerg2/Daten/java/git/GCViewer/cicd/gpg ./cicd/gcviewer-script.sh"</command>
  <requires_approval>false</requires_approval>
</execute_command>

## Step 4: Summarize route taken
Report which branch path was exercised:
- PR → verify path
- develop → snapshot path (dry-run deploy)
- master → release path (dry-run release/push)

Also report key output lines proving `DRY_RUN=true` behavior.