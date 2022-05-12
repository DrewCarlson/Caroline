package cloud.caroline

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.Services
import cloud.caroline.internal.checkServicesPermission
import cloud.caroline.models.CreateFunctionBody
import cloud.caroline.models.CreateFunctionResponse
import guru.zoroark.koa.dsl.DescriptionBuilder
import guru.zoroark.koa.dsl.schema
import guru.zoroark.koa.ktor.describe
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
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
            } describeFunctions {
                summary = "List existing functions."
                ContentType.Application.Json requestBody {
                    schema<CreateFunctionBody>()
                }

                OK response ContentType.Application.Json {
                    schema<CreateFunctionResponse>()
                }
            }
        }

        withPermission<Permission>({
            checkServicesPermission(Services.FUNCTIONS)
        }) {
            post {
            } describeFunctions {
                summary = "Create a new function."
                ContentType.Application.Json requestBody {
                    schema<CreateFunctionBody>()
                }

                OK response ContentType.Application.Json {
                    schema<CreateFunctionResponse>()
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
                } describeFunctions {
                    summary = "Get function by id."
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
                } describeFunctions {
                    summary = "Update function details."
                    ContentType.Application.Json requestBody {
                        schema<CreateFunctionBody>()
                    }

                    OK response ContentType.Application.Json {
                        schema<CreateFunctionResponse>()
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
                } describeFunctions {
                    summary = "Delete function."

                    OK response ContentType.Application.Json {
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
                } describeFunctions {
                    summary = "Invoke function."

                    OK response ContentType.Application.Json {
                    }
                }

                post("/invoke") {
                } describeFunctions {
                    summary = "Invoke function."

                    OK response ContentType.Application.Json {
                    }
                }
            }
        }
    }
}

@KtorDsl
private infix fun Route.describeFunctions(block: DescriptionBuilder.() -> Unit) = describe {
    block()
    tags += "Functions"
    security("JWT")
    security("Session")
}
