#!/usr/bin/env bash

BASE=$(pwd)

# Build project
cd ../
mvn clean install -DskipTests

# Build image
cd $BASE
mvn  spring-boot:build-image

# Run Docker composer
docker-compose up
