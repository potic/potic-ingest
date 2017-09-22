FROM openjdk:8

RUN mkdir -p /usr/src/potic-ingest && mkdir -p /usr/app

COPY build/distributions/* /usr/src/potic-ingest/

RUN unzip /usr/src/potic-ingest/potic-ingest-*.zip -d /usr/app/ && ln -s /usr/app/potic-ingest-* /usr/app/potic-ingest

WORKDIR /usr/app/potic-ingest

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-ingest --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
