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
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.server.util.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.drewcarlson.ktor.permissions.withPermission

internal fun Route.addProjectRoutes(mongodb: MongoDatabase) {
    val projectDb = mongodb.getCollection<Project>("project")
    val projectDetailsDb = mongodb.getCollection<ProjectDetails>("project-details")
    val apiKeyCredentialsDb = mongodb.getCollection<ApiKeyCredentials>("api-key-credentials")
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
                    projectDb.find(Filters.`in`(Project::id.name, projectViewIds))
                        .toList()
                } else {
                    projectDb.find().toList()
                }
                call.respond(projects)
            }.describe {
                summary = "List projects accessible to the authentication token."
                tag("Projects")
                security {
                    requirement("JWT")
                    requirement("Session")
                }
                responses {
                    response(OK.value) {
                        description = "A list of project details."
                        schema = jsonSchema<List<Project>>()
                    }
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
            }.describe {
                summary = "Create a new project."
                tag("Projects")
                security {
                    requirement("JWT")
                    requirement("Session")
                }
                requestBody {
                    schema = jsonSchema<CreateProjectBody>()
                    required = true
                }
                responses {
                    response(OK.value) {
                        description = "The new project and associated details."
                        schema = jsonSchema<CreateProjectResponse>()
                    }
                    response(UnprocessableEntity.value) {
                        description = "The body was invalid and cannot be processed."
                    }
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
                }.describe {
                    summary = "Get project details."
                    tag("Projects")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    parameters {
                        path("projectId") {
                            description = "The project id to query."
                            schema = jsonSchema<String>()
                        }
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
                }.describe {
                    summary = "Update a project."
                    tag("Projects")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    parameters {
                        path("projectId") {
                            description = "The project id to update."
                        }
                    }
                    responses {
                        response(OK.value) {
                            description = "The project has been updated."
                        }
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
                    projectDb.deleteOne(Filters.eq("_id", projectId))
                    projectDetailsDb.deleteOne(Filters.eq("_id", projectId))
                    apiKeyCredentialsDb.deleteMany(Filters.eq(ApiKeyCredentials::projectId.name, projectId))
                    call.respond(OK)
                }.describe {
                    summary = "Delete a project by id, this operation cannot be reverted."
                    tag("Projects")
                    security {
                        requirement("JWT")
                        requirement("Session")
                    }
                    parameters {
                        path("projectId") {
                            description = "The project id to delete."
                        }
                    }
                    responses {
                        response(OK.value) {
                            description =
                                "The project has been deleted, expect all operations related to the project id to fail."
                        }
                        response(NotFound.value) {
                            description = "The project id is malformed or does not exist."
                        }
                        response(Forbidden.value) {
                            description = "The associated authentication details do not have project deletion permissions."
                        }
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
                        val projectDetails = projectDetailsDb.find(Filters.eq("_id", projectId))
                            .firstOrNull()
                            ?: return@get call.respond(NotFound)
                        val credentials = apiKeyCredentialsDb
                            .find(Filters.eq(ApiKeyCredentials::projectId.name, projectDetails.id))
                            .toList()
                        call.respond(credentials)
                    }.describe {
                        summary = "List all the API keys for a project."
                        tag("Projects")
                        security {
                            requirement("JWT")
                            requirement("Session")
                        }
                        parameters {
                            path("projectId") {
                                description = "The project id."
                            }
                        }
                        responses {
                            response(OK.value) {
                                description = "A list of API key credentials."
                                schema = jsonSchema<List<ApiKeyCredentials>>()
                            }
                            response(NotFound.value) {
                                description = "The project id does not exist."
                            }
                        }
                    }

                    post {
                        val projectId: String by call.parameters
                        val projectDetails = projectDetailsDb.find(Filters.eq("_id", projectId))
                            .firstOrNull()
                            ?: return@post call.respond(NotFound)
                        val permissions = call.receiveNullable<Set<Permission>>()
                            ?: return@post call.respond(UnprocessableEntity)
                        val session = call.principal<UserSession>()!!
                        if (projectDetails.ownerId == session.userId) {
                            val apiKey = projectService.generateProjectApiKey()
                            val apiKeyCredentials = ApiKeyCredentials(
                                apiKey = apiKey,
                                projectId = projectDetails.id,
                                permissions = permissions,
                            )

                            projectDetailsDb.insertOne(projectDetails)
                            apiKeyCredentialsDb.insertOne(apiKeyCredentials)
                            call.respond(OK, apiKeyCredentialsDb)
                        } else {
                            call.respond(Forbidden)
                        }
                    }.describe {
                        summary = "Create a new project API key."
                        tag("Projects")
                        security {
                            requirement("JWT")
                            requirement("Session")
                        }
                        parameters {
                            path("projectId") {
                                description = "The project id."
                            }
                        }
                        requestBody {
                            description = "The list of permissions for the new API key."
                            schema = jsonSchema<List<Permission>>()
                            required = true
                        }
                        responses {
                            response(OK.value) {
                                description = "The API key was created."
                                schema = jsonSchema<ApiKeyCredentials>()
                            }
                            response(NotFound.value) {
                                description = "The API key does not exist."
                            }
                            response(UnprocessableEntity.value) {
                                description = "The request body was malformed or empty."
                            }
                        }
                    }

                    route("/{apiKey}") {
                        get {
                            val apiKey: String by call.parameters
                            val projectId: String by call.parameters

                            val result = apiKeyCredentialsDb.find(
                                Filters.and(
                                    Filters.eq("_id", apiKey),
                                    Filters.eq(ApiKeyCredentials::projectId.name, projectId),
                                )
                            ).firstOrNull() ?: return@get call.respond(NotFound)

                            call.respond(OK, result.permissions)
                        }.describe {
                            summary = "Get the permissions for an API key."
                            tag("Projects")
                            security {
                                requirement("JWT")
                                requirement("Session")
                            }
                            parameters {
                                path("projectId") {
                                    description = "The project id."
                                }
                                path("apiKey") {
                                    description = "The API key to view."
                                }
                            }
                            responses {
                                response(OK.value) {
                                    description = "A list of permissions for the API key."
                                    schema = jsonSchema<List<Permission>>()
                                }
                            }
                        }
                        delete {
                            val apiKey: String by call.parameters
                            val projectId: String by call.parameters
                            val result = apiKeyCredentialsDb.deleteOne(
                                Filters.and(
                                    Filters.eq("_id", apiKey),
                                    Filters.eq(ApiKeyCredentials::projectId.name, projectId),
                                )
                            )
                            projectDetailsDb.updateOne(
                                Filters.eq("_id", projectId),
                                Updates.pull(ProjectDetails::apiKeys.name, apiKey),
                            )
                            if (result.deletedCount == 1L) {
                                call.respond(OK)
                            } else {
                                call.respond(NotFound)
                            }
                        }.describe {
                            summary = "Delete an API key."
                            tag("Projects")
                            security {
                                requirement("JWT")
                                requirement("Session")
                            }
                            parameters {
                                path("projectId") {
                                    description = "The project id."
                                }
                                path("apiKey") {
                                    description = "The API key to delete."
                                }
                            }
                            responses {
                                response(OK.value) {
                                    description = "The API key has been deleted."
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
