rem requirement for act builds to work: use the catthehacker/ubuntu:full-latest image (contains maven and Xvfb)
pushd ..
act --workflows .\.github\workflows\build-and-deploy.yaml pull_request
popd