package cloud.caroline

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.Services
import cloud.caroline.internal.checkServicesPermission
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.drewcarlson.ktor.permissions.withPermission
import org.litote.kmongo.coroutine.CoroutineDatabase

internal fun Route.addFunctionsRoutes(mongodb: CoroutineDatabase) {
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
            }/* describeFunctions {
                summary = "List existing functions."
                body {
                    json {
                        schema<CreateFunctionBody>()
                    }
                }

                OK.value response {
                    json {
                        schema<CreateFunctionResponse>()
                    }
                }
            }*/
        }

        withPermission<Permission>({
            checkServicesPermission(Services.FUNCTIONS)
        }) {
            post {
            }/* describeFunctions {
                summary = "Create a new function."
                body {
                    json {
                        schema<CreateFunctionBody>()
                    }
                }

                OK.value response {
                    json {
                        schema<CreateFunctionResponse>()
                    }
                }
            }*/
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
                }/* describeFunctions {
                    summary = "Get function by id."
                }*/
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
                }/* describeFunctions {
                    summary = "Update function details."
                    body {
                        json {
                            schema<CreateFunctionBody>()
                        }
                    }

                    OK.value response {
                        json {
                            schema<CreateFunctionResponse>()
                        }
                    }
                }*/
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
                }/* describeFunctions {
                    summary = "Delete function."

                    OK.value response {
                        json {}
                    }
                }*/
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
                }/* describeFunctions {
                    summary = "Invoke function."

                    OK.value response {
                        json {}
                    }
                }*/

                post("/invoke") {
                }/* describeFunctions {
                    summary = "Invoke function."

                    OK.value response {
                        json {}
                    }
                }*/
            }
        }
    }
}
/*
@KtorDsl
private infix fun Route.describeFunctions(block: OperationDsl.() -> Unit) = describe {
    block()
    tags += "Functions"
    security("JWT")
    security("Session")
}
*/