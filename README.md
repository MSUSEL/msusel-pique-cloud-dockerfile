# MSUSEL-PIQUE-cloud-dockerfile

## Introduction
This project is an operationalized PIQUE model for the assessment of quality in docker images.

Because of the various development environment challenges when dealing with numerous 3rd party applications, 
this project is also provided as a packaged standalone docker image. That image is available
[here](https://hub.docker.com/repository/docker/msusel/pique-cloud-dockerfile/general).

## Tools and 3rd party libraries
These tools and 3rd party libraries will be automatically pulled with the docker image
* [Grype](https://github.com/anchore/grype) version 0.72.0
* [Trivy](https://github.com/aquasecurity/trivy) version 0.59.1
* [Dive](ttps://github.com/wagoodman/dive) version 0.12.0
* [Maven](https://github.com/apache/maven) version 3.9.6
* [PIQUE-core](https://github.com/MSUSEL/msusel-pique) version 1.0.1

The dockerfile has been designed to easily adjust version information as new versions are released.

## Run environment
#### Docker
docker engine 20.10.24 (not tested with versions 21+)

The image for this project is hosted on dockerhub 
[here](https://hub.docker.com/repository/docker/msusel/pique-cloud-dockerfile/general). Instructions to download
and run are supplied [below](https://github.com/MSUSEL/msusel-pique-cloud-dockerfile/tree/master#running)

It is important to note, that the docker image cannot be run without the [msusel/nvd-mirror](https://hub.docker.com/repository/docker/msusel/nvd-mirror/general) image. A docker-compose file is provided that handles this, see [Running](#running) below.

#### not Docker
It is not suggested to run PIQUE-cloud-dockerfile without the pre-built docker image, but all files and configs are supplied on this repository.

## Running
1. If not already installed on your system, download and install [Docker engine](https://docs.docker.com/engine/install/)
    * Configure docker group (no sudo required) [Instructions here](https://docs.docker.com/engine/install/linux-postinstall/)
2. Navigate to a working directory for this project
3. Run the following command to download the docker-compose file:`curl -o docker-compose.yml https://raw.githubusercontent.com/MSUSEL/msusel-pique-cloud-dockerfile/refs/heads/master/docker-compose.yml`
4. Create two directories, `input` and `output` inside the working directory.
5. Create a file named 'docker-image-target.json' and place it in the 'input' directory.
6. Copy and paste the contents of the [targets file](input/docker-image-target.json) to 'docker-image-target.json'
   1. Modify 'docker-image-target.json' to target the docker images to be analyzed.
7. The resulting directory structure should look like this:
```
├── $WORKDIR
│   ├── input
│   │   ├── docker-image-target.json
│   ├── output
```
8. Run the command `docker compose up`
9. Results will be generated in the 'output' directory

Funding Agency:
[<img src="https://www.cisa.gov/profiles/cisad8_gov/themes/custom/gesso/dist/images/backgrounds/6fdaa25709d28dfb5cca.svg" width="20%" height="20%">](https://www.cisa.gov/)
