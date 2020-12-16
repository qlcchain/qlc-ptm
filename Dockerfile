# Build
FROM maven:3.6.3-jdk-11 as builder

#do not use root as there are test cases validating file accessibility
USER nobody:nogroup
ADD --chown=nobody:nogroup . /tessera
RUN cd /tessera && mvn clean -Dmaven.repo.local=/tessera/.m2/repository -DskipTests -Denforcer.skip=true package

# Create docker image with only distribution jar

FROM adoptopenjdk/openjdk11:alpine

RUN apk add bzip2=1.0.8-r1 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted
RUN apk add musl=1.2.2_pre6-r0 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted
RUN apk add libbz2=1.0.8-r1 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted
RUN apk add libtasn1=4.16.0-r1 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted
RUN apk add libpng=1.6.37-r1 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted
RUN apk add giflib=5.2.1-r0 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted
RUN apk add libjpeg-turbo=2.0.5-r0 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted
RUN apk add java-postgresql-jdbc=42.2.10-r0 --no-cache

COPY --from=builder /tessera/tessera-dist/tessera-app/target/*-app.jar /tessera/tessera-app.jar
COPY ./ptm-start.sh /tessera/ptm-start.sh

ENTRYPOINT ["/tessera/ptm-start.sh"]
