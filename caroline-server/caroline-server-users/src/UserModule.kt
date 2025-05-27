@file:JvmName("UserModule")

package cloud.caroline

import cloud.caroline.internal.carolineProperty
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.application.*
import io.ktor.server.routing.*

@Suppress("unused")
public fun Application.userModule() {
    val mongoUrl = carolineProperty("mongoUrl")
    val databaseName = carolineProperty("databaseName")
    val apiPath = carolineProperty("apiBasePath", CAROLINE_API_PATH)

    val kmongo = MongoClient.create(mongoUrl)
    val mongodb = kmongo.getDatabase(databaseName)
    routing {
        route(apiPath) {
            addUserRoutes(mongodb)
        }
    }
}
