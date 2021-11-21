FROM openjdk:13-alpine

RUN apk add --update \
    bash \
  && rm -rf /var/cache/apk/*

WORKDIR /app
COPY . /app

COPY caroline-server/caroline-server-bundled/build/install/caroline-server-bundled-shadow ./app

CMD ["./app/bin/caroline-server-bundled"]
