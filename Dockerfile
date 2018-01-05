FROM openjdk:8

RUN mkdir -p /usr/src/potic-ingest && mkdir -p /opt

COPY build/distributions/* /usr/src/potic-ingest/

RUN unzip /usr/src/potic-ingest/potic-ingest-*.zip -d /opt/ && ln -s /opt/potic-ingest-* /opt/potic-ingest

WORKDIR /opt/potic-ingest

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-ingest --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
