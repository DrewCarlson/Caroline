package cloud.caroline

import io.ktor.server.routing.*
import org.litote.kmongo.coroutine.CoroutineDatabase


internal fun Route.addCrashRoutes(mongodb: CoroutineDatabase) {
    route("/crash") {

    }
}
