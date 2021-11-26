package cloud.caroline

import io.ktor.routing.Route
import io.ktor.routing.route
import org.litote.kmongo.coroutine.CoroutineDatabase


internal fun Route.addCrashRoutes(mongodb: CoroutineDatabase) {
    route("/crash") {

    }
}
