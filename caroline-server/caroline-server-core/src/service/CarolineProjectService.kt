package cloud.caroline.service

import cloud.caroline.admin.api.ApiKeyCredentials
import cloud.caroline.admin.api.CreateProjectBody
import cloud.caroline.admin.api.CreateProjectResponse
import cloud.caroline.admin.api.ProjectDetails
import cloud.caroline.core.models.*
import org.bouncycastle.util.encoders.Hex
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import kotlin.random.Random

private const val API_KEY_BYTES = 48

public class CarolineProjectService(
    private val projectDb: CoroutineCollection<Project>,
    private val projectDetailsDb: CoroutineCollection<ProjectDetails>,
    private val apiKeyCredentialsDb: CoroutineCollection<ApiKeyCredentials>,
) {

    public suspend fun getProjectsCount(): Long? {
        return runCatching { projectDb.countDocuments() }.getOrNull()
    }

    public suspend fun createProject(ownerUserId: String, body: CreateProjectBody): CreateProjectResponse {
        val queryProjectName = body.name.lowercase().replace(' ', '-')
        val existingByName = projectDb.findOne(Project::name eq queryProjectName)
        if (existingByName != null) {
            return CreateProjectResponse.Failed.ProjectNameExists
        }

        val rootApiKey = generateProjectApiKey()
        val project = Project(
            id = ObjectId.get().toString(),
            name = queryProjectName,
            displayName = body.name,
            description = body.description,
        )
        val projectDetails = ProjectDetails(
            id = project.id,
            ownerId = ownerUserId,
            apiKeys = listOf(rootApiKey),
        )
        val apiKeyCredentials = ApiKeyCredentials(
            apiKey = rootApiKey,
            projectId = project.id,
            permissions = setOf(
                Permission.Project(
                    projectId = project.id,
                    read = true,
                    modify = true,
                    delete = true,
                ),
                Permission.UseServices(
                    projectId = project.id,
                    services = setOf(Services.ALL),
                ),
            ),
        )

        projectDb.insertOne(project)
        projectDetailsDb.insertOne(projectDetails)
        apiKeyCredentialsDb.insertOne(apiKeyCredentials)
        return CreateProjectResponse.Success(project, apiKeyCredentials)
    }

    public fun generateProjectApiKey(): String {
        return Hex.toHexString(Random.nextBytes(API_KEY_BYTES))
    }
}
