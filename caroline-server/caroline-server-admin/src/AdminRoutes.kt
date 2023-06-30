package cloud.caroline

import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.ktor.describe
import io.ktor.server.routing.*
import io.ktor.util.*
import org.litote.kmongo.coroutine.CoroutineDatabase

internal fun Route.addAdminRoutes(mongodb: CoroutineDatabase) {
    route("/admin") {
    }
}

@KtorDsl
private infix fun Route.describeAdmin(
    block: OperationDsl.() -> Unit,
) = describe {
    block()
    tags += "Admin"
    security("Session")
}
