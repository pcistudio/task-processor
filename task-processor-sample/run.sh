#!/usr/bin/env bash

set -e
#set -x

START_DIR=$(pwd)
BASE=$(dirname "$0")
cd "${BASE}"
BASE=$(pwd)
cd "${BASE}"/..

function usage() {
    echo "Usage: run.sh [-s] [-c]";
    echo "  -s: skip build";
    echo "  -c: clean database";
}

while getopts sch flag
do
    case "${flag}" in
        s) skip_build="1";;
        c) clean_db="1";;
        *) usage;
           exit 1;;
    esac
done


# Build project
if [ -z "$skip_build" ]; then
  ./mvnw clean install -DskipTests -Dspotbugs.skip=true -Dpmd.skip=true

  # Build image
  cd "$BASE"
  ./../mvnw spring-boot:build-image
fi


cd "$BASE"

if [ -n "$clean_db" ]; then
  # Clean up
  docker stop "$(docker ps -a -f "ancestor=bitnami/mariadb:11.4" -f "name=task-processor-mariadb" -q)"
  docker rm "$(docker ps -a -f "ancestor=bitnami/mariadb:11.4" -f "name=task-processor-mariadb" -q)"
#  docker volume rm task-processor-sample_mariadb_data_2
  echo "New database will be created"
else
  echo "Database will be reused"
fi

# Run Docker composer
docker-compose up

cd "$START_DIR"
