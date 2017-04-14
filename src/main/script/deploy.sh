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
docker kill pocket-square-articles; docker rm pocket-square-articles

warn "Pulling latest docker image..."
docker pull pocketsquare/pocket-square-articles:latest

warn "Starting docker image..."
docker run -dit --name pocket-square-articles --link pocket-square-ingest --link pocket-square-mongo -e MONGO_PASSWORD=MONGO_PASSWORD -v /logs:/logs -p 28103:8080 pocketsquare/pocket-square-articles:latest

warn "Currently running docker images"
docker ps -a
