package cloud.caroline

import cloud.caroline.admin.api.ApiKeyCredentials
import cloud.caroline.admin.api.CreateProjectBody
import cloud.caroline.admin.api.CreateProjectResponse
import cloud.caroline.admin.api.ProjectDetails
import cloud.caroline.core.models.*
import cloud.caroline.data.ProjectUserSession
import cloud.caroline.service.CarolineProjectService
import cloud.caroline.service.CarolineUserService
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.describe
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.*
import org.litote.kmongo.coroutine.CoroutineDatabase
import java.util.concurrent.atomic.AtomicBoolean

internal fun Route.addCoreRoutes(mongodb: CoroutineDatabase) {
    val projectsDb = mongodb.getCollection<Project>()
    val projectDetailsDb = mongodb.getCollection<ProjectDetails>()
    val apiKeyCredsDb = mongodb.getCollection<ApiKeyCredentials>()
    val usersDb = mongodb.getCollection<User>()
    val userCredentialsDb = mongodb.getCollection<UserCredentials>()
    val userService = CarolineUserService(usersDb, userCredentialsDb)
    val projectService = CarolineProjectService(projectsDb, projectDetailsDb, apiKeyCredsDb)
    route("/core") {
        authenticate(PROVIDER_API_JWT, PROVIDER_USER_SESSION, optional = true) {
            post("/token") {
                val apiKey = call.request.header(X_CAROLINE_API_KEY)
                val jwtApiKey = call.principal<ProjectUserSession>()?.apiKey
                val credentials = (apiKey ?: jwtApiKey)
                    ?.takeUnless(String::isNullOrBlank)
                    ?.let { apiKeyCredsDb.findOneById(it) }
                    ?: return@post call.respond(Unauthorized)

                call.respond(JwtManager.createToken(credentials.apiKey))
            } describeCore {
                summary = "Create an API token for the project."
                X_CAROLINE_API_KEY headerParameter {
                    description = "The API key to create a token for."
                    required = true
                    schema<String>()
                }
                OK.value response {
                    description = "The new session token."
                }
                Unauthorized.value response {
                    description = "The API Key is missing or invalid."
                }
            }
        }

        val requiresSetup = runBlocking { projectService.getProjectsCount() == 0L }
        if (requiresSetup) {
            addSetupRoutes(userService, projectService)
        }
    }
}

private fun Route.addSetupRoutes(
    userService: CarolineUserService,
    projectService: CarolineProjectService,
) {
    val initializeMutex = Mutex()
    val initialized = AtomicBoolean(false)
    route("/setup") {
        get {
            if (initializeMutex.withLock { initialized.get() }) {
                return@get call.respond(NotFound)
            }
            call.respondHtml(OK) {
                head {
                    title("Caroline Setup")
                    style {
                        type = "text/css"
                        unsafe {
                            +"""
                            .center {
                                margin: 0 auto 0 auto;
                                text-align: center;
                            }
                            .left {
                                text-align: start;
                                display: inline-block;
                            }
                            section {
                                padding-top: 6px;
                                padding-bottom: 6px;
                            }
                            form {
                                display: flex;
                                flex-direction: column;
                                gap: 8px;
                                width: 300px;
                            }
                            """.trimIndent()
                        }
                    }
                }
                body {
                    header { h1(classes = "center") { +"Caroline Setup" } }
                    section {
                        div(classes = "center") {
                            h3 { +"Welcome!" }
                            div(classes = "left") {
                                +"1. Create an Admin user"; br
                                +"2. Copy the project API Key and ID"; br
                            }
                        }
                    }
                    section {
                        postForm(
                            action = "setup",
                            encType = FormEncType.multipartFormData,
                            classes = "center",
                        ) {
                            textInput {
                                name = "username"
                                placeholder = "Username"
                                required = true
                            }
                            passwordInput {
                                name = "password"
                                placeholder = "Password"
                                required = true
                            }
                            passwordInput {
                                name = "password_confirm"
                                placeholder = "Confirm Password"
                                required = true
                            }
                            emailInput {
                                name = "email"
                                placeholder = "Email"
                                required = true
                            }
                            submitInput {
                                value = "Submit"
                            }
                        }
                    }
                }
            }
        }
        post {
            if (initializeMutex.withLock { initialized.get() }) {
                return@post call.respond(NotFound)
            }
            initializeMutex.lock()
            val formData = call.receiveMultipart()
                .readAllParts().filterIsInstance<PartData.FormItem>()
                .map(PartData.FormItem::value)

            if (formData[1] != formData[2]) {
                return@post call.respondHtml {
                    head { title("Caroline Setup") }
                    body { +"Passwords do not match, try again." }
                }
            }

            val userResponse = userService.createUser(
                CreateUserBody(
                    username = formData[0],
                    password = formData[1],
                    email = formData[3],
                    inviteCode = null,
                ),
                setOf(
                    Permission.Global,
                    Permission.Admin(createProjects = true),
                ),
            )
            when (userResponse) {
                is CreateUserResponse.Success -> Unit
                is CreateUserResponse.Failed -> return@post call.respond(userResponse)
            }
            val projectResponse = projectService.createProject(
                userResponse.user.id,
                CreateProjectBody(
                    name = userResponse.user.username,
                    description = "A project for ${userResponse.user.displayName}",
                ),
            )
            when (projectResponse) {
                is CreateProjectResponse.Success -> Unit
                CreateProjectResponse.Failed.InvalidRequestBody,
                CreateProjectResponse.Failed.ProjectNameExists,
                -> {
                    // TODO: Remove user and credentials
                    return@post call.respond("Failed to create project: $projectResponse")
                }
            }
            initialized.set(true)
            initializeMutex.unlock()
            call.respondHtml {
                head { title("Caroline Setup") }
                body {
                    section { h3 { +"Welcome ${userResponse.user.displayName}!" } }
                    section {
                        +"Project ID: "; b { +projectResponse.project.id }
                        br
                        +"API Key: "; b { +projectResponse.credentials.apiKey }
                    }
                }
            }
        }
    }
}

@KtorDsl
private infix fun Route.describeCore(
    block: OperationDsl.() -> Unit,
) = describe {
    block()
    tags += "Core"
    security("JWT")
}
