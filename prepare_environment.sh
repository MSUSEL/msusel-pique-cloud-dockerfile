#!/bin/sh

########################################
# Prepares Environment For PIQUE-Cloud
########################################

#   TODO Set up directory structure
directory_setup() {
    echo "Setting up directories..."
    if [ ! -d "$PWD/input" ]; then
        mkdir -p "$PWD/input/keys";
    fi
}
#   TODO Instruct user to create various access keys
    #   TODO advanced - Automate key creation

#   TODO Validate keys/dependencies and access to docker
    #   NVD API keys
    #   Github API token
    #   Copy key files to input directory

#   TODO Generate target file
