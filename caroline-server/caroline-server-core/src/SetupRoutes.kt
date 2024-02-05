package cloud.caroline

import cloud.caroline.admin.api.CreateProjectBody
import cloud.caroline.admin.api.CreateProjectResponse
import cloud.caroline.core.models.CreateUserBody
import cloud.caroline.core.models.CreateUserResponse
import cloud.caroline.core.models.Permission
import cloud.caroline.service.CarolineProjectService
import cloud.caroline.service.CarolineUserService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.*
import java.util.concurrent.atomic.AtomicBoolean

internal fun Route.addSetupRoutes(
    userService: CarolineUserService,
    projectService: CarolineProjectService,
) {
    val initializeMutex = Mutex()
    val initialized = AtomicBoolean(false)
    route("/setup") {
        get {
            if (initializeMutex.withLock { initialized.get() }) {
                return@get call.respond(HttpStatusCode.NotFound)
            }
            call.respondHtml(HttpStatusCode.OK) {
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
                return@post call.respond(HttpStatusCode.NotFound)
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
                is CreateUserResponse.Failed -> {
                    initializeMutex.unlock()
                    return@post call.respond(userResponse)
                }
            }
            val projectResponse = projectService.createProject(
                userResponse.user.id,
                CreateProjectBody(
                    name = "Default Project",
                    description = "A project for ${userResponse.user.displayName}",
                ),
            )
            when (projectResponse) {
                is CreateProjectResponse.Success -> Unit
                CreateProjectResponse.Failed.InvalidRequestBody,
                CreateProjectResponse.Failed.ProjectNameExists,
                -> {
                    userService.deleteUser(userResponse.user.id)
                    initializeMutex.unlock()
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
