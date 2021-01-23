package drewcarlson.caroline

import drewcalson.caroline.admin.api.ApiKeyCredentials
import drewcalson.caroline.admin.api.CreateProjectBody
import drewcalson.caroline.admin.api.ProjectDetails
import drewcarlson.caroline.core.models.Permission
import drewcarlson.caroline.core.models.Project
import drewcarlson.caroline.data.UserSession
import drewcarlson.ktor.permissions.withPermission
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import java.util.Base64
import java.util.Locale
import kotlin.random.Random

private const val API_KEY_BYTES = 48

internal fun Route.addProjectRoutes(mongodb: CoroutineDatabase) {
    val projectDb = mongodb.getCollection<Project>()
    val projectDetailsDb = mongodb.getCollection<ProjectDetails>()
    val apiKeyCredentialsDb = mongodb.getCollection<ApiKeyCredentials>()
    route("/project") {
        withPermission(Permission.ListProjects) {
            get {
                call.respond(projectDb.find().toList())
            }
        }

        withPermission(Permission.CreateProject) {
            post {
                val body = call.receiveOrNull<CreateProjectBody>()
                    ?: return@post call.respond(UnprocessableEntity)
                val session = call.principal<UserSession>()!!

                val queryProjectName = body.name.toLowerCase(Locale.ROOT)
                val existingByName = projectDb.findOne(Project::name eq queryProjectName)
                if (existingByName != null) {
                    // TODO: Return duplicate project name error
                    return@post call.respond(Conflict)
                }

                val project = Project(
                    id = ObjectId.get().toString(),
                    name = queryProjectName,
                    displayName = body.name,
                    description = body.description
                )
                val projectDetails = ProjectDetails(
                    id = project.id,
                    ownerId = session.userId,
                    apiKeys = listOf(
                        Base64.getEncoder().encodeToString(Random.nextBytes(API_KEY_BYTES))
                    )
                )
                val apiKeyCredentials = ApiKeyCredentials(
                    apiKey = projectDetails.apiKeys.first(),
                    projectId = project.id,
                    permissions = setOf(Permission.Global)
                )

                projectDb.insertOne(project)
                projectDetailsDb.insertOne(projectDetails)
                apiKeyCredentialsDb.insertOne(apiKeyCredentials)
                call.respond(OK)
            }
        }

        withPermission(Permission.DeleteProject) {
            delete("/{project_id}") {
                val projectId = call.parameters["project_id"]
                    ?: return@delete call.respond(UnprocessableEntity)
                val projectDetails = projectDetailsDb.findOneById(projectId)
                    ?: return@delete call.respond(NotFound)
                val session = call.principal<UserSession>()!!

                if (projectDetails.ownerId == session.userId) {
                    projectDb.deleteOneById(projectId)
                    projectDetailsDb.deleteOneById(projectId)
                    apiKeyCredentialsDb.deleteMany(ApiKeyCredentials::projectId eq projectId)
                    call.respond(OK)
                } else {
                    call.respond(Forbidden)
                }
            }
        }
    }
}
