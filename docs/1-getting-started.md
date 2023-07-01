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
      - "8080:8080"
    links:
      - mongo
```


## Download


[![Maven Central](https://img.shields.io/maven-central/v/cloud.caroline/caroline-sdk-core?label=maven&color=blue)](https://search.maven.org/search?q=g:cloud.caroline)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/cloud.caroline/caroline-sdk-core?server=https%3A%2F%2Fs01.oss.sonatype.org)

```kotlin
repositories {
    mavenCentral()
    // Or snapshots
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    // SDK Modules
    implementation("cloud.caroline:caroline-sdk-admin:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-analytics:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-auth:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-config:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-core:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-crash:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-functions:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-logging:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-store:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-sdk-store-encrypted:$CAROLINE_VERSION")

    // Server Modules
    implementation("cloud.caroline:caroline-server-core:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-server-projects:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-server-users:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-server-crash:$CAROLINE_VERSION")
    implementation("cloud.caroline:caroline-server-logging:$CAROLINE_VERSION")
}
```

