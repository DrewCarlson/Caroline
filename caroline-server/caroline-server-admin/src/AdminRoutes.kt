package cloud.caroline

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.routing.*

internal fun Route.addAdminRoutes(mongodb: MongoDatabase) {
    route("/admin") {
    }
}

/*@KtorDsl
private infix fun Route.describeAdmin(
    block: OperationDsl.() -> Unit,
) = describe {
    block()
    tags += "Admin"
    security("Session")
}*/
