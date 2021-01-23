package drewcarlson.caroline

import com.mongodb.WriteConcern
import drewcarlson.caroline.internal.carolineProperty
import drewcarlson.caroline.internal.carolinePropertyInt
import drewcarlson.caroline.logging.LogRecord
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.gt
import org.litote.kmongo.gte

private const val DEFAULT_RECORD_RETURN_LIMIT = 100

internal fun Route.addLoggingRoutes(mongoDb: CoroutineDatabase) {
    /*runBlocking { // TODO: Do something like this when an logging is enabled for a project
        if (!mongoDb.listCollectionNames().contains("logRecord")) {
            mongoDb.createCollection("logRecord", CreateCollectionOptions().apply {
                capped(true)
                sizeInBytes(52_428_800L) // 50MB
                maxDocuments(100_000L)
            })
        }
    }*/

    val defaultRecordLimit = carolinePropertyInt("logging.listRecordLimit", DEFAULT_RECORD_RETURN_LIMIT)

    val logRecordDb = mongoDb.getCollection<LogRecord>()
        .withWriteConcern(WriteConcern.UNACKNOWLEDGED)
    route("/logging") {
        route("/record") {
            get {
                val limit = call.parameters["limit"]?.toIntOrNull() ?: defaultRecordLimit
                val offsetTimestamp = call.parameters["offset"]?.toLongOrNull()
                val filter = if (offsetTimestamp == null) {
                    null
                } else {
                    LogRecord::timestamp gte offsetTimestamp
                }
                val slice = logRecordDb
                    .find(filter)
                    .limit(limit.coerceIn(1, defaultRecordLimit))
                call.respond(slice.toList())
            }

            post {
                val records = call.receiveOrNull<List<LogRecord>>()

                call.respond(OK)

                if (records?.isNotEmpty() == true) {
                    records.chunked(100) { chunk ->
                        chunk.map { it.copy(id = ObjectId.get().toString()) }
                    }.forEach { chunk ->
                        logRecordDb.insertMany(chunk)
                    }
                }
            }
        }
    }
}
