FROM azul/zulu-openjdk:21-jre-latest

WORKDIR /app

COPY caroline-server/caroline-server-bundled/build/install/caroline-server-shadow ./app

ENV PORT=8080
ENV MONGO_URL=mongodb://mongo

ARG PUID=1000
ARG PGID=1000
ARG user=caroline

RUN addgroup --gid "$PGID" "$user" \
    && adduser  --gecos '' --uid "$PUID" --gid "$PGID" --disabled-password --shell /bin/bash "$user"
USER $user


CMD ["./app/bin/caroline-server"]
