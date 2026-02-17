FROM azul/zulu-openjdk:25-jre-latest

WORKDIR /app
COPY . /app

COPY caroline-server/caroline-server-bundled/build/install/caroline-server-bundled-shadow ./app

ENV PORT=8080
ENV MONGO_URL=mongodb://mongo

CMD ["./app/bin/caroline-server"]
