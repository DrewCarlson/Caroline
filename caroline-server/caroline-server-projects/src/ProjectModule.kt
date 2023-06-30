@file:JvmName("ProjectModule")

package cloud.caroline

import cloud.caroline.internal.carolineProperty
import com.mongodb.ConnectionString
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

@Suppress("unused")
public fun Application.projectModule() {
    val mongoUrl = carolineProperty("mongoUrl")
    val databaseName = carolineProperty("databaseName")
    val apiPath = carolineProperty("apiBasePath", CAROLINE_API_PATH)

    val kmongo = KMongo.createClient(ConnectionString(mongoUrl))
    val mongodb = kmongo.getDatabase(databaseName).coroutine
    routing {
        route(apiPath) {
            authenticate(PROVIDER_API_JWT, PROVIDER_USER_SESSION) {
                addProjectRoutes(mongodb)
            }
        }
    }
}
