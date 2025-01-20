#!/usr/bin/env bash

set -e
BASE=$(pwd)

# Build project
cd ../
mvn clean install -DskipTests -Dspotbugs.skip=true -Dpmd.skip=true

# Build image
cd $BASE
mvn  spring-boot:build-image

# Run Docker composer
docker-compose up
