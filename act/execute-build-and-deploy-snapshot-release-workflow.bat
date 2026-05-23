rem requirement for act builds to work: use the catthehacker/ubuntu:full-latest image (contains maven and Xvfb)
rem all necessary secrets are set in the .env file
rem CAUTION: will do real deployments!
pushd ..
act push -W .github/workflows/build-and-deploy.yaml --matrix java:8 --var DRY_RUN=false --env GITHUB_REF_NAME=develop --secret-file .env
popd