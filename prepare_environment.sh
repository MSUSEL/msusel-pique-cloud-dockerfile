#!/bin/sh

########################################
# Prepares Environment For PIQUE-Cloud
########################################

WORKDIR="$PWD"

# Dependency Check
function check_deps() {
    printf "Checking dependencies\n"
    if [ ! -x /usr/bin/wget ]; then
        echo "ERROR: wget is not installed." >&2
        exit 1
    fi
#   TODO Validate access to docker
}

function set_up_directories() {
    if [ ! -d "$WORKDIR/input/keys" ]; then
        printf "Setting up directories...\n"
        mkdir -p "$WORKDIR/input/keys";
        printf "Directory setup is complete\n"
    else
        printf "Directory structure is correct\n"
    fi;
}

#   TODO advanced - Automate key creation
#   IMPORTANT!!! This will overwrite existing key files. Double check user wants to create
#       new key and has backed up any old keys
function get_authentication_keys() {
    NVD_KEY="$WORKDIR/input/keys/nvd-api-key.txt"
    GITHUB_TOKEN="$WORKDIR/input/keys/github-token.txt"

    # Check if keys already exist and provide option to paste in keys
    if [ ! -f "$NVD_KEY" ]; then
        echo "NVD Key : $NVD_KEY"
        printf "Please generate an NVD API key and enter the key here: "
        read nvd_key > $NVD_KEY
        # TODO Validate nvd_key exists and is in the correct format
        #if [ null or misformed ] {
        # print error and retry - give option to exit
        #}
        # printf "nvd-key is %s " "$nvd_key"
        # printf "\n"
        # Write key to correct file


    fi
    if [ ! -f "$GITHUB_TOKEN" ]; then
        printf "Please generate a Github personal access token with ......permissions. Then paste the public key here: "
        read github_key > $GITHUB_TOKEN
        # TODO Validate github_key exists and is in the correct format
        #if [ null or misformed ] {
        # print error and retry - give option to exit
        #}
        # printf "github key is %s " "$github_key"
        # printf "\n"
    fi;
}

function generate_sample_target() {
    WGET_OPTS="-O $WORKDIR/input/docker-image-target.json"
    TARGET="https://github.com/MSUSEL/msusel-pique-cloud-dockerfile/blob/master/input/docker-image-target.json"

    if [ ! -a "$WORKDIR/input/docker-image-target.json" ]; then
        printf "Getting a sample target file from PIQUE-Cloud\n"
        wget $WGET_OPTS $TARGET
        printf "Sample target file created\n"
    fi
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

