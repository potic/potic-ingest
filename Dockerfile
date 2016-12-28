FROM openjdk:8

RUN mkdir -p /usr/src/pocket-square-articles
RUN mkdir -p /usr/app

COPY build/distributions/* /usr/src/pocket-square-articles/

RUN unzip /usr/src/pocket-square-articles/pocket-square-articles-*.zip -d /usr/app/
RUN ln -s /usr/app/pocket-square-articles-* /usr/app/pocket-square-articles

WORKDIR /usr/app/pocket-square-articles

EXPOSE 8080
ENTRYPOINT ["./bin/pocket-square-articles"]
CMD []
