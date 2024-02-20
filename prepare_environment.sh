#!/bin/sh

########################################
# Prepares Environment For PIQUE-Cloud
########################################

WORKDIR="$PWD"

# Dependency Check
function check_deps() {
    # Check that wget is installed
    printf "Checking dependencies...\n"
    [ -x /usr/bin/wget ] || { printf "ERROR: wget is not installed. Please install wget on your system or perform manual installation of PIQUE-Cloud\n"; exit 1; }
    printf "\twget is installed\n"

    #   TODO Validate access to docker
    ([ -f $HOME/.docker/config.json ] && [ $(grep -c '"auths" {}:' "$HOME/.docker/config.json") -eq 0 ]) \
        || { printf "There may be an issue with your docker configuration or docker may not be installed.\n"; exit 1; }
    printf "\tdocker is installed and you are logged in\n"

    # Check git is installed
    git --version >/dev/null 2>&1 || { printf "git is not installed. Please install git to continue PIQUE-Cloud setup\n"; exit 1; }
    printf "\tgit is installed\n"
    printf "Dependency check completed successfully\n"
}

function set_up_directories() {
    printf "Checking directory structure...\n"
    [ -d "$WORKDIR/input/keys" ] || { mkdir -p "$WORKDIR/input/keys"; }
    [ -d "$WORKDIR/output/" ] || { mkdir -p "$WORKDIR/output"; }
    printf "Directory check/setup complete\n"
}

#   TODO advanced - Automate key creation
#   IMPORTANT!!! This will overwrite existing key files. Double check user wants to create
#       new key and has backed up any old keys
function get_authentication_keys() {
    printf "Checking authentication keys...\n"

    NVD_KEY_PATH="$WORKDIR/input/keys/nvd-api-key.txt"
    GITHUB_TOKEN_PATH="$WORKDIR/input/keys/github-token.txt"

    # Check if keys already exist and provide option to paste in keys
    [ -f "$NVD_KEY_PATH" ] || { printf "Please generate an NVD API key and enter the key here: "; \
        read nvd_key; \
        echo "$nvd_key" > $NVD_KEY_PATH; }
    printf "\tNVD Key setup complete\n"
        # TODO Validate nvd_key exists and is in the correct format
        #if [ null or misformed ] {
        # print error and retry - give option to exit
        #}
        # printf "nvd-key is %s " "$nvd_key"
        # printf "\n"
        # Write key to correct file

    [ -f "$GITHUB_TOKEN_PATH" ] || { printf "Please generate a Github personal access token with ......permissions. Then paste the public key here: "; \
        read github_key; \
        echo "$github_key" > "$GITHUB_TOKEN_PATH"; }
    printf "\tGithub token setup complete\n"
        # TODO Validate github_key exists and is in the correct format
        #if [ null or misformed ] {
        # print error and retry - give option to exit
        #}
        # printf "github key is %s " "$github_key"
        # printf "\n"
    printf "Authentication setup complete\n"
}

function generate_sample_target() {
    printf "Checking if target file exists...\n"
    WGET_OPTS="-O $WORKDIR/input/docker-image-target.json"
    TARGET="https://github.com/MSUSEL/msusel-pique-cloud-dockerfile/blob/master/input/docker-image-target.json"

    [ -f "$WORKDIR/input/docker-image-target.json" ] || { printf "Getting a sample target file from PIQUE-Cloud\n"; \
        wget $WGET_OPTS $TARGET; \
        printf "Sample target file created\n"; }
    printf "Target file configured\n"
}

function run() {
    check_deps
    set_up_directories
    get_authentication_keys
    generate_sample_target

    #TODO This docker command is not working in my environment yet. There is likely much more error handling and env validation to do in this file
    docker run -it --rm -v "/var/run/docker.sock:/var/run/docker.sock:rw" -v $WORKDIR/input:/input -v $WORKDIR/output:/output pique-cloud-dockerfile:latest
}
run

