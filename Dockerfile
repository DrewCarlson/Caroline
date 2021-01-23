FROM openjdk:13-alpine

RUN apk add --update \
    bash \
  && rm -rf /var/cache/apk/*

WORKDIR /server
COPY . /server

RUN ./gradlew tasks --no-daemon
RUN ./gradlew :caroline-server:caroline-server-bundled:installShadowDist --no-daemon

CMD ["bash", "-c", "/server/caroline-server/caroline-server-bundled/build/install/caroline-server-bundled-shadow/bin/caroline-server-bundled", "-watch=false"]
