FROM mcr.microsoft.com/java/jre:17-zulu-alpine

RUN apk add --update --no-cache bash

WORKDIR /app
COPY . /app

COPY caroline-server/caroline-server-bundled/build/install/caroline-server-bundled-shadow ./app

ENV PORT=8080
ENV MONGO_URL=mongodb://mongo

CMD ["./app/bin/caroline-server"]
