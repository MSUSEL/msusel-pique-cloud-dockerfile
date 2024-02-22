#!/bin/sh

########################################################################
# Prepares Environment For PIQUE-Cloud and runs PIQUE on sample targets
########################################################################

WORKDIR="$PWD"
INPUT_MESSAGE="########################################################\n#   INPUT NEEDED    \n########################################################\n"

# Dependency Checks
function check_deps() {
    DOCKER_CONFIG="$HOME/.docker/config.json"

    # Check that wget is installed
    printf "Checking dependencies...\n"
    [ -x /usr/bin/wget ] || { printf "ERROR: wget is not installed. Please install wget on your system or perform manual installation of PIQUE-Cloud\nexit 1\n"; exit 1; }
    printf "\twget is installed\n"

    # Check that docker is installed and user is logged in
    ([ -f "$DOCKER_CONFIG" ] && [ $(grep -c '"auths" {}:' "$DOCKER_CONFIG") -eq 0 ]) \
        || { printf "Please ensure that docker is installed, running, and configured with your username and login.\nexit 1\n"; exit 1; }
    printf "\tdocker is installed and you are logged in\n"

    # Check git is installed
    git --version >/dev/null 2>&1 || { printf "git is not installed. Please install git on your system to continue PIQUE-Cloud setup\nexit 1\n"; exit 1; }
    printf "\tgit is installed\n"
    printf "Dependency check completed successfully\n\n"
}

# Check for/create input, output, and subdirectories
function set_up_directories() {
    printf "Checking directory structure...\n"
    [ -d "$WORKDIR/input/keys" ] || { mkdir -p "$WORKDIR/input/keys"; }
    [ -d "$WORKDIR/output/" ] || { mkdir -p "$WORKDIR/output"; }
    printf "Directory check/setup complete\n\n"
}

# Checks for existing keys. If they don't exist, prompt user to create them and paste into prompts
function get_authentication_keys() {
    printf "Checking authentication keys...\n"

    NVD_KEY_PATH="$WORKDIR/input/keys/nvd-api-key.txt"
    GITHUB_TOKEN_PATH="$WORKDIR/input/keys/github-token.txt"

    # Check if keys already exist and provide option to paste in keys
    # Future Work: Key validation
    [ -f "$NVD_KEY_PATH" ] || { printf "$INPUT_MESSAGE"; \
        printf "Please generate an API key with the National Vulnerability Database here https://nvd.nist.gov/developers/request-an-api-key\n"; \
        printf "Be sure to save it somewhere secure. If you lose the key, you will need to generate a new one\n"; \
        printf "Once you have retrieved your key, enter the it here: "; \
        read nvd_key; \
        echo "$nvd_key" > $NVD_KEY_PATH; }
    printf "\tNVD Key setup complete\n"

    # Future Work: Key validation
    [ -f "$GITHUB_TOKEN_PATH" ] || { printf "$INPUT_MESSAGE"; \
        printf "Please generate a Github personal access token with ......permissions. Then paste the public key here: "; \
        read github_key; \
        echo "$github_key" > "$GITHUB_TOKEN_PATH"; }
    printf "\tGithub token setup complete\n"
    printf "Authentication setup complete\n\n"
}

# Grabs the sample target file from msusel-pique-cloud-dockerfile github repo
function generate_sample_target() {
    printf "Checking if target file exists...\n"
    WGET_OPTS="-O $WORKDIR/input/docker-image-target.json"
    TARGET="https://raw.githubusercontent.com/MSUSEL/msusel-pique-cloud-dockerfile/master/input/docker-image-target.json"

    [ -f "$WORKDIR/input/docker-image-target.json" ] || { printf "Getting a sample target file from PIQUE-Cloud\n"; \
        wget $WGET_OPTS $TARGET; \
        printf "Sample target file created\n"; }
    printf "Target file configured\n\n"
}

function run() {
    check_deps
    set_up_directories
    get_authentication_keys
    generate_sample_target

    docker run -it --rm -v "/var/run/docker.sock:/var/run/docker.sock:rw" -v $WORKDIR/input:/input -v $WORKDIR/output:/output msusel/pique-cloud-dockerfile:latest
}
run

