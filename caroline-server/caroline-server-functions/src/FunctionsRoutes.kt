package cloud.caroline

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.Services
import cloud.caroline.internal.checkServicesPermission
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.server.util.*
import org.drewcarlson.ktor.permissions.withPermission

internal fun Route.addFunctionsRoutes(mongodb: MongoDatabase) {
    route("/functions") {
        withPermission<Permission>({
            checkServicesPermission(Services.FUNCTIONS)
            add<Permission.Functions> {
                stub(Permission.Functions("", "", read = true))
                verify(Permission.Functions::read)
                select { permissions ->
                    val projectId: String by parameters
                    permissions.filter { it.projectId == projectId }.toSet()
                }
            }
        }) {
            get {
            }.describe {
                summary = "List existing functions."
                tag("Functions")
                security {
                    requirement("JWT")
                    requirement("Session")
                }
                responses {
                    response(OK.value) {
                        description = "A list of functions."
                    }
                }
            }
        }

        withPermission<Permission>({
            checkServicesPermission(Services.FUNCTIONS)
        }) {
            post {
            }.describe {
                summary = "Create a new function."
                tag("Functions")
                security {
                    requirement("JWT")
                    requirement("Session")
                }
                responses {
                    response(OK.value) {
                        description = "The created function."
                    }
                }
            }
        }

        route("/{functionId}") {
            withPermission<Permission>({
                checkServicesPermission(Services.FUNCTIONS)
                add<Permission.Functions> {
                    stub(Permission.Functions("", "", read = true))
                    verify(Permission.Functions::read)
                    select { permissions ->
                        val projectId: String by parameters
                        val functionId: String by parameters
                        permissions
                            .filter { it.projectId == projectId && it.functionId == functionId }
                            .toSet()
                    }
                }
            }) {
                get {
                }.describe {
                    summary = "Get function by id."
                    tag("Functions")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    responses {
                        response(OK.value) {
                            description = "The requested function."
                        }
                    }
                }
            }

            withPermission<Permission>({
                checkServicesPermission(Services.FUNCTIONS)
                add<Permission.Functions> {
                    stub(Permission.Functions("", "", modify = true))
                    verify(Permission.Functions::modify)
                    select { permissions ->
                        val projectId: String by parameters
                        val functionId: String by parameters
                        permissions
                            .filter { it.projectId == projectId && it.functionId == functionId }
                            .toSet()
                    }
                }
            }) {
                put {
                }.describe {
                    summary = "Update function details."
                    tag("Functions")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    responses {
                        response(OK.value) {
                            description = "The updated function."
                        }
                    }
                }
            }

            withPermission<Permission>({
                checkServicesPermission(Services.FUNCTIONS)
                add<Permission.Functions> {
                    stub(Permission.Functions("", "", delete = true))
                    verify(Permission.Functions::delete)
                    select { permissions ->
                        val projectId: String by parameters
                        val functionId: String by parameters
                        permissions
                            .filter { it.projectId == projectId && it.functionId == functionId }
                            .toSet()
                    }
                }
            }) {
                delete {
                }.describe {
                    summary = "Delete function."
                    tag("Functions")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    responses {
                        response(OK.value) {
                            description = "The function has been deleted."
                        }
                    }
                }
            }

            withPermission<Permission>({
                checkServicesPermission(Services.FUNCTIONS)
                add<Permission.Functions> {
                    stub(Permission.Functions("", "", invoke = true))
                    verify(Permission.Functions::invoke)
                    select { permissions ->
                        val projectId: String by parameters
                        val functionId: String by parameters
                        permissions
                            .filter { it.projectId == projectId && it.functionId == functionId }
                            .toSet()
                    }
                }
            }) {
                get("/invoke") {
                }.describe {
                    summary = "Invoke function."
                    tag("Functions")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    responses {
                        response(OK.value) {
                            description = "The function result."
                        }
                    }
                }

                post("/invoke") {
                }.describe {
                    summary = "Invoke function."
                    tag("Functions")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    responses {
                        response(OK.value) {
                            description = "The function result."
                        }
                    }
                }
            }
        }
    }
}
