FROM msusel/pique-core:1.0.1

## dependency and library versions
ARG GRYPE_VERSION=0.72.0
ARG PIQUE_DOCKERFILE_VERSION=2.0.1
# Trivy release without the 'v' because the release of trivy does not include the v on its download page
ARG TRIVY_VERSION=0.59.1
ARG DIVE_VERSION=0.12.0

RUN apk update && apk upgrade && apk add --update --no-cache \
    # system level packages
    curl dpkg docker openrc dpkg

# add user to docker group
RUN addgroup root docker
RUN rc-update add docker boot

# move to home for a fresh start
WORKDIR "/home"

## grype install
RUN curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin v$GRYPE_VERSION
## grype db update
RUN grype db update

## trivy install
RUN wget "https://github.com/aquasecurity/trivy/releases/download/v"$TRIVY_VERSION"/trivy_"$TRIVY_VERSION"_Linux-64bit.deb"
RUN dpkg --add-architecture amd64
RUN dpkg -i "trivy_"$TRIVY_VERSION"_Linux-64bit.deb"
RUN rm "trivy_"$TRIVY_VERSION"_Linux-64bit.deb"

## trivy database update
RUN trivy image --download-db-only
RUN trivy image --download-java-db-only

## dive install
RUN curl -OL https://github.com/wagoodman/dive/releases/download/v${DIVE_VERSION}/dive_${DIVE_VERSION}_linux_amd64.deb
RUN dpkg -i ./dive_${DIVE_VERSION}_linux_amd64.deb

##################################################
######### pique dockerfile install ###############
##################################################

WORKDIR "/home"
RUN git clone https://github.com/MSUSEL/msusel-pique-cloud-dockerfile
WORKDIR "/home/msusel-pique-cloud-dockerfile"

# copy model file to resources
COPY output/PIQUECLOUDDockerfile1000squashed_trimmed.json src/main/resources/PIQUECLOUDDockerfile1000squashed_trimmed.json

# build pique cloud dockerfile
RUN mvn package -Dmaven.test.skip -Dlicense.skip

# create input directory
RUN mkdir "/input"

# input for project files
VOLUME ["/input"]

# output for model
VOLUME ["/output"]

# symlink to jar file for cleanliness
RUN ln -s /home/msusel-pique-cloud-dockerfile/target/msusel-pique-cloud-dockerfile-$PIQUE_DOCKERFILE_VERSION-jar-with-dependencies.jar \
        /home/msusel-pique-cloud-dockerfile/docker_entrypoint.jar

##### secret sauce
ENTRYPOINT ["java", "-jar", "/home/msusel-pique-cloud-dockerfile/docker_entrypoint.jar", "--run", "evaluate", "--file", "/input/docker-image-target.json"]
