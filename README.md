# MSUSEL-PIQUE-cloud-dockerfile

## Introduction
This project is an operationalized PIQUE model for the assessment of quality in docker images.

Because of the various development environment challenges when dealing with numerous 3rd party applications,
this project is also provided as a packaged standalone docker image. That image is available
[here](https://hub.docker.com/repository/docker/msusel/pique-cloud-dockerfile/general).

## Tools and 3rd party libraries
These tools and 3rd party libraries will be automatically pulled with the docker image
* [Grype](https://github.com/anchore/grype) version 0.72.0
* [Trivy](https://github.com/aquasecurity/trivy) version 0.44.1
* [Maven](https://github.com/apache/maven) version 3.9.6
* [PIQUE-core](https://github.com/MSUSEL/msusel-pique) version 0.9.4

The dockerfile has been designed to easily adjust version information as new versions are released.

## Run environment
#### Docker
docker engine 20.10.24 (not tested with versions 21+)

The image for this project is hosted on dockerhub
[here](https://hub.docker.com/repository/docker/msusel/pique-cloud-dockerfile/general). Instructions to download
and run are supplied [below](https://github.com/MSUSEL/msusel-pique-cloud-dockerfile/tree/master#running)


#### not Docker
It is not suggested to run PIQUE-cloud-dockerfile without the pre-built docker image, but all files and configs
are supplied on this repository.


## Running
### Prerequisites
1. Download and install [Docker engine](https://docs.docker.com/engine/install/)
2. With Docker engine installed, pull the latest version of this project:
`docker pull msusel/pique-cloud-dockerfile:latest`
3. Navigate to a working directory for this project. (Your working directory should not be in the same directory as this repository)
#### Shell Script Setup
In a unix-like environment, running prepare_environment will automatically check dependencies, guide the user through
setting up necssary keys, pull the appropriate docker image and run PIQUE against a sample target.

1.

#### Manual Setup
1. Create two directories, "input" and "output". Inside the "input directory", create another directory "keys"
2. Generate an NVD API key [here](https://nvd.nist.gov/developers/request-an-api-key) and save the text of the key to a file 'nvd-api-key.txt'
3. Generate a [Github API token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) and save the text of the key to a file 'github-token.txt'
4. Move the files 'nvd-api-key.txt' and 'github-token.txt' to the 'keys' directory.
5. Create a file named 'docker-image-target.json' and place it in the 'input' directory.
6. Copy and paste the contents of the [targets file](input/docker-image-target.json) to 'docker-image-target.json'
   1. Modify 'docker-image-target.json' to target the docker images to be analyzed.
7. The resulting directory structure should look like this:
```
├── $WORKDIR
│   ├── input
│   │   ├── keys
│   │   │   ├── github-token.txt
│   │   │   ├── nvd-api-key.txt
│   │   ├── docker-image-target.json
│   ├── output
```
11. Run the command `docker run -it --rm -v "/var/run/docker.sock:/var/run/docker.sock:rw" -v /path/to/working/directory/input:/input -v /path/to/working/directory/output:/output pique-cloud-dockerfile:latest`
12. Results will be generated in the 'output' directory

Funding Agency:

[<img src="https://www.cisa.gov/profiles/cisad8_gov/themes/custom/gesso/dist/images/backgrounds/6fdaa25709d28dfb5cca.svg" width="20%" height="20%">](https://www.cisa.gov/)
