/bin/sh

java -jar /home/msusel-pique-cloud-dockerfile/pique-cloud-entrypoint.jar --run evaluate --file /input/docker-image-target.json

ln -s main.log /output/main.log