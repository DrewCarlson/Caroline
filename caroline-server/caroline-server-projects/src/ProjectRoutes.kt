package cloud.caroline

import cloud.caroline.admin.api.ApiKeyCredentials
import cloud.caroline.admin.api.CreateProjectBody
import cloud.caroline.admin.api.CreateProjectResponse
import cloud.caroline.admin.api.ProjectDetails
import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.Project
import cloud.caroline.core.models.Services
import cloud.caroline.data.ProjectUserSession
import cloud.caroline.data.UserSession
import cloud.caroline.internal.checkServicesPermission
import cloud.caroline.service.CarolineProjectService
import guru.zoroark.koa.dsl.DescriptionBuilder
import guru.zoroark.koa.dsl.schema
import guru.zoroark.koa.dsl.schemaArray
import guru.zoroark.koa.ktor.describe
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
import org.drewcarlson.ktor.permissions.withPermission
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.`in`
import org.litote.kmongo.pull
import java.util.Base64
import kotlin.random.Random

private const val API_KEY_BYTES = 48

internal fun Route.addProjectRoutes(mongodb: CoroutineDatabase) {
    val projectDb = mongodb.getCollection<Project>()
    val projectDetailsDb = mongodb.getCollection<ProjectDetails>()
    val apiKeyCredentialsDb = mongodb.getCollection<ApiKeyCredentials>()
    val projectService = CarolineProjectService(projectDb, projectDetailsDb, apiKeyCredentialsDb)

    route("/project") {
        withPermission<Permission>({
            checkServicesPermission(Services.PROJECTS, requireId = false)
            add<Permission.Project> { verify(Permission.Project::read) }
        }) {
            get {
                val permissions = call.principal<UserSession>()?.permissions
                    ?: call.principal<ProjectUserSession>()?.permissions.orEmpty()
                val projectViewIds = permissions.mapNotNull { permission ->
                    (permission as? Permission.Project)?.let { projectPerm ->
                        projectPerm.projectId.takeIf { projectPerm.read }
                    }
                }
                val projects = if (projectViewIds.isNotEmpty()) {
                    projectDb.find(Project::id `in` projectViewIds).toList()
                } else {
                    projectDb.find().toList()
                }
                call.respond(projects)
            } describeProject {
                summary = "List projects accessible to the authentication token."
                OK response ContentType.Application.Json {
                    description = "A list of project details."
                    schemaArray<Project>()
                }
            }
        }

        withPermission<Permission>({
            add<Permission.Admin> {
                stub(Permission.Admin(createProjects = true))
                verify(Permission.Admin::createProjects)
            }
        }) {
            post {
                val body = call.receiveNullable<CreateProjectBody>()
                    ?: return@post call.respond(UnprocessableEntity)
                val session = call.principal<UserSession>()!!
                call.respond(projectService.createProject(session.userId, body))
            } describeProject {
                summary = "Create a new project."
                security("JWT")
                security("Session")
                ContentType.Application.Json requestBody {
                    schema<CreateProjectBody>()
                }
                OK response ContentType.Application.Json {
                    description = "The new project and associated details."
                    schema<CreateProjectResponse>()
                }
            }
        }

        route("/{projectId}") {
            withPermission<Permission>({
                checkServicesPermission(Services.PROJECTS)
                add<Permission.Project> {
                    stub(Permission.Project("", read = true))
                    verify(Permission.Project::read)
                    select { permissions ->
                        val projectId = parameters["projectId"] ?: return@select emptySet()
                        permissions.filter { it.projectId == projectId }.toSet()
                    }
                }
            }) {
                get {
                    call.respond(OK)
                } describeProject {
                    summary = "Get project details."
                    security("JWT")
                    security("Session")
                    "projectId" pathParameter {
                        description = "The project id to query."
                        schema<String>()
                    }
                }
            }

            withPermission<Permission>({
                checkServicesPermission(Services.PROJECTS)
                add<Permission.Project> {
                    stub(Permission.Project("", modify = true))
                    verify(Permission.Project::modify)
                    select { permissions ->
                        val projectId = parameters["projectId"] ?: return@select emptySet()
                        permissions.filter { it.projectId == projectId }.toSet()
                    }
                }
            }) {
                put {
                } describeProject {
                    summary = "Update a project."
                    security("JWT")
                    security("Session")
                    "projectId" pathParameter {
                        description = "The project id to update."
                    }
                    OK response {
                        description = "The project has been updated."
                    }
                }
            }

            withPermission<Permission>({
                checkServicesPermission(Services.PROJECTS)
                add<Permission.Project> {
                    stub(Permission.Project("", delete = true))
                    verify(Permission.Project::delete)
                    select { permissions ->
                        val projectId = parameters["projectId"] ?: return@select emptySet()
                        permissions.filter { it.projectId == projectId }.toSet()
                    }
                }
            }) {
                delete {
                    val projectId: String by call.parameters
                    projectDb.deleteOneById(projectId)
                    projectDetailsDb.deleteOneById(projectId)
                    apiKeyCredentialsDb.deleteMany(ApiKeyCredentials::projectId eq projectId)
                    call.respond(OK)
                } describeProject {
                    summary = "Delete a project by id, this operation cannot be reverted."
                    security("JWT")
                    security("Session")
                    "projectId" pathParameter {
                        description = "The project id to delete."
                    }
                    OK response {
                        description =
                            "The project has been deleted, expect all operations related to the project id to fail."
                    }
                    NotFound response {
                        description = "The project id is malformed or does not exist."
                    }
                    Forbidden response {
                        description = "The associated authentication details do not have project deletion permissions."
                    }
                }
            }

            route("/api-key") {
                withPermission<Permission>({
                    checkServicesPermission(Services.PROJECTS)
                    add<Permission.Project> {
                        stub(Permission.Project("", modify = true))
                        verify(Permission.Project::modify)
                        select { permissions ->
                            val projectId = parameters["projectId"] ?: return@select emptySet()
                            permissions.filter { it.projectId == projectId }.toSet()
                        }
                    }
                }) {
                    get {
                        val projectId: String by call.parameters
                        val projectDetails = projectDetailsDb.findOneById(projectId)
                            ?: return@get call.respond(NotFound)
                        val credentials = apiKeyCredentialsDb
                            .find(ApiKeyCredentials::projectId eq projectDetails.id)
                            .toList()
                        call.respond(credentials)
                    } describeProject {
                        summary = "List all the API keys for a project."
                        "projectId" pathParameter {
                            description = "The project id."
                        }
                    }

                    post {
                        val projectId: String by call.parameters
                        val projectDetails = projectDetailsDb.findOneById(projectId)
                            ?: return@post call.respond(NotFound)
                        val permissions = call.receiveNullable<Set<Permission>>()
                            ?: return@post call.respond(UnprocessableEntity)
                        val session = call.principal<UserSession>()!!
                        if (projectDetails.ownerId == session.userId) {
                            val apiKey = generateProjectApiKey()
                            val apiKeyCredentials = ApiKeyCredentials(
                                apiKey = apiKey,
                                projectId = projectDetails.id,
                                permissions = permissions
                            )

                            projectDetailsDb.insertOne(projectDetails)
                            apiKeyCredentialsDb.insertOne(apiKeyCredentials)
                        } else {
                            call.respond(Forbidden)
                        }
                    } describeProject {
                        summary = "Create a new project API key."
                        "projectId" pathParameter {
                            description = "The project id."
                        }
                        ContentType.Application.Json requestBody {
                            description = "The list of permissions for the new API key."
                            schemaArray<Permission>()
                        }
                    }

                    route("/{apiKey}") {
                        get {
                            val apiKey: String by call.parameters
                            val projectId: String by call.parameters

                            val result = apiKeyCredentialsDb.findOne(
                                ApiKeyCredentials::apiKey eq apiKey,
                                ApiKeyCredentials::projectId eq projectId,
                            ) ?: return@get call.respond(NotFound)

                            call.respond(result.permissions)
                        } describeProject {
                            summary = "Get the permissions for an API key."
                            "projectId" pathParameter {
                                description = "The project id."
                            }
                            "apiKey" pathParameter {
                                description = "The API key to view."
                            }
                            OK response ContentType.Application.Json {
                                schemaArray<Permission>()
                            }
                        }
                        delete {
                            val apiKey: String by call.parameters
                            val projectId: String by call.parameters
                            val result = apiKeyCredentialsDb.deleteOne(
                                ApiKeyCredentials::apiKey eq apiKey,
                                ApiKeyCredentials::projectId eq projectId,
                            )
                            projectDetailsDb.updateOne(
                                ProjectDetails::id eq projectId,
                                pull(ProjectDetails::apiKeys, apiKey)
                            )
                            if (result.deletedCount == 1L) {
                                call.respond(OK)
                            } else {
                                call.respond(NotFound)
                            }
                        } describeProject {
                            summary = "Delete an API key."
                            "projectId" pathParameter {
                                description = "The project id."
                            }
                            "apiKey" pathParameter {
                                description = "The API key to delete."
                            }
                            OK response {
                                description = "The API key has been deleted."
                            }
                        }
                    }
                }
            }
        }
    }
}

public fun generateProjectApiKey(): String {
    return Base64.getEncoder().encodeToString(Random.nextBytes(API_KEY_BYTES))
}

@KtorDsl
private infix fun Route.describeProject(block: DescriptionBuilder.() -> Unit) = describe {
    block()
    tags += "Projects"
    security("JWT")
    security("Session")
}
