pushd ..
act --workflows .\.github\workflows\pullrequest.yaml pull_request -P ubuntu-latest=-self-hosted
popd