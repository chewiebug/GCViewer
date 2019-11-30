# cicd
This directory contains a settings.xml + script file that can be used to 
deploy to the SonaType OSS repository from a Travis CI build.

The full instructions are here:
http://knowm.org/configure-travis-ci-to-deploy-snapshots/

# gpg
## documentation 
- https://stackoverflow.com/questions/38276762/travis-gpg-signing-failed-secret-key-not-available
- https://github.com/making/travis-ci-maven-deploy-skelton
- https://www.gnupg.org/gph/en/manual.html

expiration date of current keys: 2021-11-28

## issues with outdated openssl version in travis-ci (30.11.2019)
-> docker ruby image (ruby 2.6.5p114) uses openssl 1.1.1d; travis-ci uses openssl 1.0.2g
-> preferred command would be "openssl enc -e -v -iter 3 -aes-256-cbc -pass pass:$ENCRYPTION_PASSWORD -in ./xxx.gpg -out xxx.gpg.enc"
two issues:
- "-iter 3" is not known -> drop option
- key derivation mechanism was changed between openssl 1.1.x and 1.0.x
-- https://stackoverflow.com/questions/39637388/encryption-decryption-doesnt-work-well-between-two-different-openssl-versions/39641378#39641378
-- -> use -md sha1 to enable decryption by openssl 1.0.x 

## steps to create / renew pubring.gpg.enc + secring.gpg.enc
### run docker image
- docker run --rm -it --mount type=bind,src=D:\Users\joerg2\Daten\java\git\GCViewer\cicd\gpg,dst=/usr/gpg ruby/travis /bin/bash
- cd /usr/gpg

### create private + public key
- gpg --generate-key
-- jwu@gmx.ch
-- gpg zert für maven signierung
- gpg --output pubring.gpg --export jwu@gmx.ch
- gpg --armor --export jwu@gmx.ch > pubring.gpg.txt
-- upload to public key server (http://keyserver.ubuntu.com:11371)
- gpg --export-secret-keys > secring.gpg

### encrypt keys
- export ENCRYPTION_PASSWORD=<encryption password>
- openssl enc -e -v -aes-256-cbc -md sha1 -pass pass:$ENCRYPTION_PASSWORD -in ./pubring.gpg -out ./pubring.gpg.enc
- openssl enc -e -v -aes-256-cbc -md sha1 -pass pass:$ENCRYPTION_PASSWORD -in ./secring.gpg -out ./secring.gpg.enc
