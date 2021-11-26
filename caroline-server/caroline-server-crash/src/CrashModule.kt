@file:JvmName("CrashModule")
package cloud.caroline

import com.mongodb.ConnectionString
import cloud.caroline.internal.carolineProperty
import io.ktor.application.Application
import io.ktor.auth.authenticate
import io.ktor.routing.route
import io.ktor.routing.routing
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

@Suppress("unused")
public fun Application.crashModule() {
    val mongoUrl = carolineProperty("mongoUrl")
    val databaseName = carolineProperty("databaseName")
    val apiPath = carolineProperty("apiBasePath", CAROLINE_API_PATH)

    val kmongo = KMongo.createClient(ConnectionString(mongoUrl))
    val mongodb = kmongo.getDatabase(databaseName).coroutine
    routing {
        route(apiPath) {
            authenticate(PROVIDER_API_JWT) {
                addCrashRoutes(mongodb)
            }
        }
    }
}
