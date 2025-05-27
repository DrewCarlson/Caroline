package cloud.caroline

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.Services
import cloud.caroline.data.ProjectUserSession
import cloud.caroline.internal.carolinePropertyInt
import cloud.caroline.internal.checkServicesPermission
import cloud.caroline.logging.LogRecord
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.none
import kotlinx.coroutines.flow.toList
import org.bson.BsonDocument
import org.bson.types.ObjectId
import org.drewcarlson.ktor.permissions.withPermission

private const val DEFAULT_RECORD_RETURN_LIMIT = 100

internal fun Route.addLoggingRoutes(kmongo: MongoClient, mongoDb: MongoDatabase) {
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
        withPermission<Permission>({
            checkServicesPermission(Services.LOGGING)
        }) {
            post("/create") {
                val projectId = call.principal<ProjectUserSession>()!!.projectId
                val projectDb = kmongo.getDatabase(projectId)
                val logKey = ObjectId.get().toString()
                projectDb.createCollection("logs-$logKey")
                call.respond(OK, logKey)
            }/* describeLogging {
                summary = "Create a new logging channel."
                security("JWT")
                security("Session")
                OK.value response {
                    description = "The new logging channel key."
                    plainText {
                        schema(ObjectId.get().toString())
                    }
                }
            }*/
        }
        withPermission<Permission>({
            checkServicesPermission(Services.LOGGING, requireId = false)
            add<Permission.Logging> {
                stub(Permission.Logging("", "", read = true))
                verify(Permission.Logging::read)
            }
        }) {
            get("/channels") {
            }/* describeLogging {
                summary = "List available log channels"
                security("JWT")
                security("Session")
            }*/
        }
        route("/{logKey}") {
            route("/record") {
                withPermission<Permission>({
                    add<Permission.Logging> {
                        stub(Permission.Logging("", "", read = true))
                        verify(Permission.Logging::read)
                        select { permissions ->
                            val logKey: String by parameters
                            permissions.filter { it.logKey == logKey }.toSet()
                        }
                    }
                }) {
                    get {
                        val projectId = call.principal<ProjectUserSession>()!!.projectId
                        val logKey: String by call.parameters
                        val limit: Int? by call.parameters
                        val offset: Long? by call.parameters
                        val filter = offset?.let { Filters.gte(LogRecord::timestamp.name, it) }

                        val projectDb = kmongo.getDatabase(projectId)
                        if (projectDb.listCollectionNames().none { it == "logs-$logKey" }) {
                            return@get call.respond(NotFound)
                        }
                        val logRecordDb = projectDb.getCollection<LogRecord>("logs-$logKey")

                        val slice = logRecordDb
                            .find(filter ?: BsonDocument())
                            .limit((limit ?: defaultRecordLimit).coerceIn(1, defaultRecordLimit))
                            .toList()

                        call.respond(slice)
                    }/* describeLogging {
                        summary = "Get log items for the given log key."
                        security("Session")
                        security("JWT")
                        "logKey" pathParameter {
                            description = "The log key to load results for."
                            schema<String>()
                        }
                        "limit" queryParameter {
                            description = "The amount of log lines to return."
                            schema<Int>()
                        }
                        "offset" queryParameter {
                            description = "Return items created after this unix timestamp"
                            schema(System.currentTimeMillis())
                        }
                        OK.value response {
                            json {
                                schema<List<LogRecord>>()
                            }
                        }
                        NotFound.value response {
                            description = "The provided log key does not exist."
                        }
                    }*/
                }

                withPermission<Permission>({
                    add<Permission.Project> {
                        stub(Permission.Project("", modify = true))
                        verify(Permission.Project::modify)
                        select { permissions ->
                            val projectId = principal<ProjectUserSession>()?.projectId
                                ?: return@select emptySet()
                            permissions.filter { it.projectId == projectId }.toSet()
                        }
                    }
                    add<Permission.Logging> {
                        stub(Permission.Logging("", "", modify = true))
                        verify(Permission.Logging::modify)
                        select { permissions ->
                            val logKey: String by parameters
                            permissions.filter { it.logKey == logKey }.toSet()
                        }
                    }
                }) {
                    post {
                        val projectId = call.principal<ProjectUserSession>()!!.projectId
                        val logKey: String? by call.parameters
                        val records = call.receiveNullable<List<LogRecord>>()

                        call.respond(OK)

                        if (records?.isNotEmpty() == true) {
                            val projectDb = kmongo.getDatabase(projectId)
                            val logRecordDb = projectDb.getCollection<LogRecord>("logs-$logKey")

                            records.chunked(100) { chunk ->
                                chunk.map { it.copy(id = ObjectId.get().toString()) }
                            }.forEach { chunk ->
                                logRecordDb.insertMany(chunk)
                            }
                        }
                    } /*describeLogging {
                        summary = "Add new records to a log."
                        security("JWT")
                        "logKey" pathParameter {
                            description = "The log key to load results for."
                            schema<String>()
                        }

                        body {
                            json {
                                schema<List<LogRecord>>()
                            }
                        }

                        OK.value response {
                            description = "The log messages have been received and will be stored in the log channel."
                        }
                    }*/
                }
            }

            withPermission<Permission.Logging>({
                add<Permission.Logging> {
                    stub(Permission.Logging("", "", delete = true))
                    verify(Permission.Logging::delete)
                    select { permissions ->
                        val logKey: String by parameters
                        permissions.filter { it.logKey == logKey }.toSet()
                    }
                }
            }) {
                delete {
                    val projectId = call.principal<ProjectUserSession>()!!.projectId
                    val logKey: String by call.parameters

                    val projectDb = kmongo.getDatabase(projectId)
                    projectDb.getCollection<LogRecord>("logs-$logKey").drop()

                    call.respond(OK)
                } /*describeLogging {
                    summary = "Delete a log channel."
                    "logKey" pathParameter {
                        description = "The log key to delete."
                        schema<String>()
                    }

                    OK.value response {
                        description = "The log channel has been deleted."
                    }
                }*/
            }
        }
    }
}
/*
@KtorDsl
private infix fun Route.describeLogging(block: OperationDsl.() -> Unit) = describe {
    block()
    tags += "Logging"
    security("JWT")
    security("Session")
}
*/