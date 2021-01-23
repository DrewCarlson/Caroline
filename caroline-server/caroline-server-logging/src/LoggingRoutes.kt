package drewcarlson.caroline

import com.mongodb.reactivestreams.client.MongoClient
import drewcarlson.caroline.data.ProjectUserSession
import drewcarlson.caroline.internal.carolinePropertyInt
import drewcarlson.caroline.logging.LogRecord
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.gte

private const val DEFAULT_RECORD_RETURN_LIMIT = 100

internal fun Route.addLoggingRoutes(kmongo: MongoClient, mongoDb: CoroutineDatabase) {
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

    route("/logging") {
        route("/record/{log_key}") {
            get {
                val projectId = call.principal<ProjectUserSession>()!!.projectId
                val logKey = call.parameters["log_key"] ?: return@get call.respond(NotFound)
                val limit = call.parameters["limit"]?.toIntOrNull() ?: defaultRecordLimit
                val offsetTimestamp = call.parameters["offset"]?.toLongOrNull()
                val filter = if (offsetTimestamp == null) {
                    null
                } else {
                    LogRecord::timestamp gte offsetTimestamp
                }

                val projectDb = kmongo.getDatabase(projectId).coroutine
                val logRecordDb = projectDb.getCollection<LogRecord>("logs-$logKey")

                val slice = logRecordDb
                    .find(filter)
                    .limit(limit.coerceIn(1, defaultRecordLimit))

                call.respond(slice.toList())
            }

            post {
                val projectId = call.principal<ProjectUserSession>()!!.projectId
                val logKey = call.parameters["log_key"] ?: return@post call.respond(BadRequest)
                val records = call.receiveOrNull<List<LogRecord>>()

                call.respond(OK)

                if (records?.isNotEmpty() == true) {
                    val projectDb = kmongo.getDatabase(projectId).coroutine
                    val logRecordDb = projectDb.getCollection<LogRecord>("logs-$logKey")

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
