@file:JvmName("LoggingModule")

package cloud.caroline

import cloud.caroline.internal.carolineProperty
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

@Suppress("unused")
public fun Application.loggingModule() {
    val mongoUrl = carolineProperty("mongoUrl")
    val databaseName = carolineProperty("databaseName")
    val apiPath = carolineProperty("apiBasePath", CAROLINE_API_PATH)

    val kmongo = MongoClient.create(mongoUrl)
    val mongodb = kmongo.getDatabase(databaseName)
    routing {
        route(apiPath) {
            route("/{projectId}") {
                authenticate(PROVIDER_API_JWT, PROVIDER_USER_SESSION) {
                    addLoggingRoutes(kmongo, mongodb)
                }
            }
        }
    }
}
