package cloud.caroline

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.routing.*

internal fun Route.addAnalyticsRoutes(mongodb: MongoDatabase) {
    route("/analytics") {
    }
}
