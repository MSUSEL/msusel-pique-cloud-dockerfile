#!/bin/sh

########################################################################
# Prepares Environment For PIQUE-Cloud and runs PIQUE on sample targets
########################################################################

WORKDIR="$PWD"
INPUT_MESSAGE="##############################################\n#   INPUT NEEDED    \n##############################################"

# Dependency Checks
check_deps() {
    DOCKER_CONFIG="$HOME/.docker/config.json"

    # Check that wget is installed
    printf "Checking dependencies...\n"
    [ -x /usr/bin/wget ] || { printf "\u001b[31mERROR: wget is not installed. Please install wget on your system or perform manual installation of PIQUE-Cloud\nexit 1\n\u001b[0m"; exit 1; }
    printf "\twget is installed\n"

    # Check that docker is installed and user is logged in
    ([ -f "$DOCKER_CONFIG" ] && [ $(grep -c '"auths" {}:' "$DOCKER_CONFIG") -eq 0 ]) \
        || { printf "\u001b[31mPlease ensure that docker is installed, running, and configured with your username and login.\u001b[0m\nexit 1\n"; exit 1; }
    printf "\tdocker is installed\n"

    # Check git is installed
    git --version >/dev/null 2>&1 || { printf "\u001b[31mgit is not installed. Please install git on your system to continue PIQUE-Cloud setup\u001b[0m\nexit 1\n"; exit 1; }
    printf "\tgit is installed\n"
    printf "\u001b[32mDependency check completed successfully\n\n\u001b[0m"
}

# Check for/create input, output, and subdirectories
set_up_directories() {
    printf "Checking directory structure...\n"
    [ -d "$WORKDIR/input/keys" ] || { mkdir -p "$WORKDIR/input/keys"; }
    [ -d "$WORKDIR/output/" ] || { mkdir -p "$WORKDIR/output"; }
    printf "\u001b[32mDirectory check/setup complete\n\n\u001b[0m"
}

# Checks for existing keys. If they don't exist, prompt user to create them and paste into prompts
get_authentication_keys() {
    printf "Checking authentication keys...\n"

    NVD_KEY_PATH="$WORKDIR/input/keys/nvd-api-key.txt"
    GITHUB_TOKEN_PATH="$WORKDIR/input/keys/github-token.txt"

    # Check if keys already exist and provide option to paste in keys
    # Future Work: Key validation
    [ -f "$NVD_KEY_PATH" ] || { printf "$INPUT_MESSAGE\n"; \
        printf "\u001b[33mThis step asks you to paste an api key into the terminal. If you are concened about having a secure key in your clipboard or shell history,\n"; \
        printf "type the letter 'n' in the prompt. The script will exit and you can manually generate WORKDIR/input/keys/nvd-api-key.txt. Then, rerun this script.\u001b[0m\n"; \
        printf "Please generate an API key with the National Vulnerability Database here:\u001b[36m https://nvd.nist.gov/developers/request-an-api-key\u001b[0m\n"; \
        printf "Be sure to save it somewhere secure. If you lose the key, you will need to generate a new one.\n"; \
        printf "Once you have retrieved your key, enter it here: "; \
        read nvd_key; \
        [ "$nvd_key" = "n" ] && exit 1 || echo "$nvd_key" > $NVD_KEY_PATH; }
    printf "\tNVD Key setup complete\n"

    # Future Work: Key validation
    [ -f "$GITHUB_TOKEN_PATH" ] || { printf "$INPUT_MESSAGE\n"; \
        printf "\u001b[33mThis step asks you to paste a personal access token into the terminal. If you are concened about having a secure key in your clipboard or shell \n"; \
        printf "history, type the letter 'n' in the prompt. The script will exit and you can manually generate WORKDIR/input/keys/github-token.txt Then, rerun this script.\u001b[0m\n"; \
        printf "Please generate a Github personal access token with ......permissions. More info:\u001b[36m https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens\u001b[0m\n"; \
        printf "Once you have generated or retrieved your token, paste it here: "; \
        read github_key; \
        [ "$github_key" = "n" ] && exit 1 || echo "$github_key" > "$GITHUB_TOKEN_PATH"; }
    printf "\tGithub token setup complete\n"
    printf "\u001b[32mAuthentication setup complete\u001b[0m\n\n"
}

# Grabs the sample target file from msusel-pique-cloud-dockerfile github repo
generate_sample_target() {
    printf "Checking if target file exists...\n"
    WGET_OPTS="-O $WORKDIR/input/docker-image-target.json"
    TARGET="https://raw.githubusercontent.com/MSUSEL/msusel-pique-cloud-dockerfile/master/input/docker-image-target.json"

    [ -f "$WORKDIR/input/docker-image-target.json" ] || { printf "Getting a sample target file from PIQUE-Cloud\n"; \
        wget $WGET_OPTS $TARGET; \
        printf "\u001b[32mSample target file created\u001b[0m\n"; }
    printf "\u001b[32mTarget file configured\u001b[0m\n\n"
}

run() {
    check_deps
    set_up_directories
    get_authentication_keys
    generate_sample_target

    docker run -it --rm -v "/var/run/docker.sock:/var/run/docker.sock:rw" -v $WORKDIR/input:/input -v $WORKDIR/output:/output msusel/pique-cloud-dockerfile:latest
}
run
