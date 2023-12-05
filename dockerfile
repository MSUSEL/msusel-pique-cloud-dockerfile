FROM alpine:3.18.5

## dependency and library versions
ARG GRYPE_VERSION=0.72.0
ARG MAVEN_VERSION=3.9.6
ARG PIQUE_CORE_VERSION=0.9.3
ARG PIQUE_DOCKERFILE_VERSION=0.9
ARG TRIVY_VERSION=0.44.1

RUN apk update && apk upgrade && apk add \
    # system level packages
    curl dpkg git openjdk8 python3 py3-pip
    # pique specific installs

# move to home for a fresh start
WORKDIR "/home"

## grype installs
RUN curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin v$GRYPE_VERSION

## trivy installs
RUN wget "https://github.com/aquasecurity/trivy/releases/download/v"$TRIVY_VERSION"/trivy_"$TRIVY_VERSION"_Linux-64bit.deb"
RUN dpkg --add-architecture amd64
RUN dpkg -i "trivy_"$TRIVY_VERSION"_Linux-64bit.deb"
RUN rm "trivy_"$TRIVY_VERSION"_Linux-64bit.deb"

##################################################
############### pique core install ###############
##################################################
# maven install - install in opt
WORKDIR "/opt"
RUN wget "https://dlcdn.apache.org/maven/maven-3/"$MAVEN_VERSION"/binaries/apache-maven-"$MAVEN_VERSION"-bin.tar.gz"
RUN tar xzvf "apache-maven-"$MAVEN_VERSION"-bin.tar.gz"
RUN rm "apache-maven-"$MAVEN_VERSION"-bin.tar.gz"
ENV PATH "/opt/apache-maven-"$MAVEN_VERSION"/bin:$PATH"

# pique core install
WORKDIR "/home"
RUN git clone https://github.com/MSUSEL/msusel-pique.git
WORKDIR "/home/msusel-pique"
RUN git checkout "tags/v"$PIQUE_CORE_VERSION
RUN mvn install -Dmaven.test.skip

##################################################
######### pique dockerfile install ###############
##################################################

# python dependency installs
RUN pip install argparse requests #json

WORKDIR "/home/msusel-pique-cloud-dockerfile"
# local copy until we publicize it on github
COPY . "/home/msusel-pique-cloud-dockerfile/"
#RUN git clone https://github.com/MSUSEL/msusel-pique-cloud-dockerfile
RUN mvn package -Dmaven.test.skipgit

#create input directory
#RUN mkdir "/input"

# input for project files
#VOLUME ["/input"]

# create output directory
#RUN mkdir "/output"
# output for model
#VOLUME ["/output"]

#RUN chmod -R +x /input
#RUN chmod -R +x /output

##### secret sauce
#ENTRYPOINT ["java", "-jar", "/home/msusel-pique-cloud-dockerfile/target/msusel-pique-cloud-dockerfile-0.0.1-jar-with-dependencies.jar"]
