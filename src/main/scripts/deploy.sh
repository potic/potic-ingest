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

warn "Pulling latest docker image..."
docker pull potic/potic-ingest:$TAG_TO_DEPLOY

warn "Killing currently running docker image..."
docker kill potic-ingest; docker rm potic-ingest

warn "Starting docker image..."
docker run -dit --name potic-ingest --restart on-failure --link potic-pocket-api --link potic-articles --link potic-users -e LOG_PATH=/mnt/logs -v /mnt/logs:/mnt/logs -e LOGZIO_TOKEN=$LOGZIO_TOKEN potic/potic-ingest:$TAG_TO_DEPLOY

warn "Wait 30sec to check status"
sleep 30

warn "Currently running docker images"
docker ps -a
