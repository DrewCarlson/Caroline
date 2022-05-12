package cloud.caroline

import guru.zoroark.koa.dsl.DescriptionBuilder
import guru.zoroark.koa.ktor.describe
import io.ktor.server.routing.*
import io.ktor.util.*
import org.litote.kmongo.coroutine.CoroutineDatabase

internal fun Route.addAdminRoutes(mongodb: CoroutineDatabase) {
    route("/admin") {
    }
}

@KtorDsl
private infix fun Route.describeAdmin(
    block: DescriptionBuilder.() -> Unit,
) = describe {
    block()
    tags += "Admin"
    security("Session")
}
