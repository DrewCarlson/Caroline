# Caroline

**_[WIP] This project is unfinished and not ready for use, most features described are not complete._**

[![](https://img.shields.io/badge/-sponsor-ff69b4)](https://github.com/sponsors/DrewCarlson)
[![Maven Central](https://img.shields.io/maven-central/v/tools.caroline/caroline-sdk-core?label=maven&color=blue)](https://search.maven.org/search?q=g:tools.caroline)
[![](https://github.com/DrewCarlson/Caroline/workflows/Server/badge.svg)](https://github.com/DrewCarlson/Caroline/actions?query=workflow%3Aserver)
[![](https://github.com/DrewCarlson/Caroline/workflows/SDK/badge.svg)](https://github.com/DrewCarlson/Caroline/actions?query=workflow%3Asdk)
[![](https://img.shields.io/github/workflow/status/drewcarlson/caroline/Publish%20Docker%20image?label=Docker)](https://github.com/DrewCarlson/Caroline/pkgs/container/caroline)

Caroline provides privacy respecting backend services with multiplatform Kotlin SDKs.

## Services

The SDK provides the following features depending on your server deployment.


### Monitoring

**Logging** - Stream your application logs in realtime or schedule uploads

**Crash Reporting** - Collect caught and fatal errors with custom data

**Analytics** - Track events with custom attributes


### Application

**Remote configuration** - Distribute runtime configuration based on device attributes, percentage, etc.

**Shared data store** - Store and query data with custom access rules

**Encrypted data store** - Store and query data locally and sync it with e2e encryption

**Authentication** - Managed users with passwords or OAuth Providers  (Github, Google, etc.)

**Functions** - Manipulate shared data based on cron or webhook triggers


### SDK

```kotlin
// Configure SDK
val sdk = CarolineSDK {
    serverUrl = "https://caroline-server"
    projectId = "..."
    apiKey = "..."
}


// Logging
val logger = CarolineLogger.create(sdk, LogSchedule.WhenBufferFull)

logger.logInfo("An event to be logged.", mapOf("uid" to "..."))
```

## Server

`caroline-server` supports various deployment modes depending on your backend requirements.
By default, an all-in-one mode is used exposing all services from a single central instance.

If only some services are required, the server deployment can disable any unnecessary SDK backend components.
Backend components can be deployed as individual services to scaling individual services.


#### Database

[MongoDB](https://www.mongodb.com/) provides the primary data storage.
[KMongo](https://litote.org/kmongo/) is used to interact with the database from server code.

### Deploy

Deployment is straightforward with and without Docker, but for an all-in-one development instance, docker-compose is recommended.
See the [Dockerfile](Dockerfile) to understand running the server without Docker.

For critical deployments, managed database hosting is recommended.
[MongoDB Atlas](https://cloud.mongodb.com/) is a good option and provides a free development database.

#### Docker

TODO: Finish this section

If required, start a Mongo instance:
```shell

```

Then start Caroline, pointing to the local Mongo instance:
```shell
docker run -d --name caroline \
    --env MONGO_URL=mongodb://mongodb \
    ghcr.io/drewcarlson/caroline:main
```


#### Docker Compose

See [docker-compose.yml](docker-compose.yml) for a complete example.

```yaml
version: '3.1'
services:

  mongo:
    container_name: mongo
    image: mongo
    restart: unless-stopped

  caroline:
    container_name: caroline
    image: ghcr.io/drewcarlson/caroline:main
    environment:
      PORT: 8080
      MONGO_URL: mongodb://mongo
    restart: unless-stopped
    ports:
      - 8080:8080
    links:
      - mongo
```


## Download


[![Maven Central](https://img.shields.io/maven-central/v/tools.caroline/caroline-sdk-core?label=maven&color=blue)](https://search.maven.org/search?q=g:tools.caroline)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/tools.caroline/caroline-sdk-core?server=https%3A%2F%2Fs01.oss.sonatype.org)

```kotlin
repositories {
    mavenCentral()
    // Or snapshots
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    // SDK Modules
    implementation("tools.caroline:caroline-sdk-admin:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-analytics:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-auth:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-config:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-core:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-crash:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-functions:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-logging:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-store:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-sdk-store-encrypted:$CAROLINE_VERSION")

    // Server Modules
    implementation("tools.caroline:caroline-server-core:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-server-projects:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-server-users:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-server-crash:$CAROLINE_VERSION")
    implementation("tools.caroline:caroline-server-logging:$CAROLINE_VERSION")
}
```
