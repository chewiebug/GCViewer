# cicd
This directory contains a settings.xml + script file that can be used to 
deploy to the Central Publisher Portal from a Github actions build.

# secrets
For local tests, add them to the .env file.
For github actions running on github, configure them here: https://github.com/chewiebug/GCViewer/settings/secrets/actions

- GITHUB_TOKEN
  - automatically injected by github actions runners
  - needs manual creation for local runs
  - https://github.com/settings/personal-access-tokens
- CI_DEPLOY_USERNAME / CI_DEPLOY_PASSWORD
  - needed to deploy to Sonatype maven central repository
  - https://central.sonatype.com/usertoken
- SCP_USERNAME / SCP_PASSWORD 
  - needed to upload to frs.sourceforge.net
- CODECOV_TOKEN
  - needed to upload code coverage results
  - https://app.codecov.io/gh/chewiebug/GCViewer/config/general
- ENCRYPTION_PASSWORD
  - the password used for openssl encryption (must not contain "$", otherwise usage in the bash script won't work)
- GPG_KEYNAME / GPG_PASSPHRASE
  - keyname (fingerprint or email) + passphrase to use the private key for jar signing with the maven-gpg-plugin
# gpg

## steps to create / renew private-key.asc.enc + public-key.asc.enc

expiration date of current keys: 2029-05-24

### generate a new keypair (RSA-4096 recommended)
- gpg --full-generate-key
  - algorithm: ECC (sign only)
  - elliptic curve: Curve 25519
  - expiry: 3y
  - real name: Joerg Wuethrich
  - email: jwu@gmx.ch (must match your Central Portal account)
  - comment: sign for maven central
  - store the revocation certificate safely

### get the key ID
- gpg --list-keys --keyid-format LONG jwu@gmx.ch
  -> use the full fingerprint (e.g. CA925CD6C9E8D064FF05B4728190C4130ABA0F98) as GPG_KEYNAME

### publish public key to a keyserver
- gpg --keyserver keyserver.ubuntu.com --send-keys <KEYID>

### export the keys (armored format)
- gpg --armor --export-secret-keys <KEYID> > private-key.asc
- gpg --armor --export <KEYID> > public-key.asc

### encrypt keys for CI
- openssl enc -e -a -v -aes-256-cbc -salt -pbkdf2 -iter 500000 -in ./private-key.asc -out ./private-key.asc.enc
- openssl enc -e -a -v -aes-256-cbc -salt -pbkdf2 -iter 500000 -in ./public-key.asc -out ./public-key.asc.enc

### clean up unencrypted files
- rm private-key.asc public-key.asc
