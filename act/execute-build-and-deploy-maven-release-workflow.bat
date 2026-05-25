rem requirement for act builds to work: use the catthehacker/ubuntu:full-latest image (contains maven and Xvfb)
rem all necessary secrets are set in the .env file
rem HINT: Set  GITHUB_REF_NAME to the current branch for this build to work
rem CAUTION: will do real deployments!
pushd ..
act push -W .github/workflows/build-and-deploy.yaml --matrix java:8 --var DRY_RUN=false --env GITHUB_REF_NAME=feature/fix-maven-releases --input RELEASE_BUILD=true  --secret-file .env
popd