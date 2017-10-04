#!/usr/bin/env sh

##############################################################################
##
##  Stop and kill currently running docker image, pull newest version and
##  run it.
##
##############################################################################

warn ( ) {
    echo "$*"
}

warn "Currently running docker images"
docker ps -a

warn "Killing currently running docker image..."
docker kill potic-ingest; docker rm potic-ingest

warn "Pulling latest docker image..."
docker pull potic/potic-ingest:$TAG_TO_DEPLOY

warn "Starting docker image..."
docker run -dit --name potic-ingest --link potic-pocket-api --link potic-mongodb --link potic-users -e LOG_PATH=/logs -v /logs:/logs -e MONGO_PASSWORD=$MONGO_PASSWORD -p 40404:8080 potic/potic-ingest:$TAG_TO_DEPLOY

warn "Currently running docker images"
docker ps -a
