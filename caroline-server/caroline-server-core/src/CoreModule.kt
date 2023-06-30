@file:JvmName("CoreModule")

package cloud.caroline

import cloud.caroline.admin.api.ApiKeyCredentials
import cloud.caroline.core.models.Permission
import cloud.caroline.data.ProjectUserSession
import cloud.caroline.data.RestrictedSession
import cloud.caroline.data.UserSession
import cloud.caroline.internal.carolineProperty
import com.mongodb.ConnectionString
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.util.encoders.Hex
import org.drewcarlson.ktor.permissions.PermissionAuthorization
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.random.Random

private const val SESSION_KEY_BYTES = 32
public const val X_CAROLINE_API_KEY: String = "X-Caroline-Api-Key"

public val json: Json = Json {
    isLenient = true
    prettyPrint = false
    encodeDefaults = true
    ignoreUnknownKeys = true
    useArrayPolymorphism = false
    classDiscriminator = "__type"
}

@Suppress("unused")
public fun Application.coreModule() {
    val mongoUrl = carolineProperty("mongoUrl")
    val databaseName = carolineProperty("databaseName")
    val apiPath = carolineProperty("apiBasePath", CAROLINE_API_PATH)

    val kmongo = KMongo.createClient(ConnectionString(mongoUrl))
    val mongodb = kmongo.getDatabase(databaseName).coroutine
    val apiKeyDb = mongodb.getCollection<ApiKeyCredentials>()

    val jwtIssuer = carolineProperty("jwtIssuer")
    val jwtRealm = carolineProperty("jwtRealm")
    val jwtSecret = carolineProperty("jwtSecret")
    JwtManager.configure(jwtIssuer, jwtRealm, jwtSecret)

    install(Authentication) {
        jwt(PROVIDER_API_JWT) {
            realm = jwtRealm
            verifier(JwtManager.verifier())
            validate { credential ->
                val audience = credential.payload.audience.firstOrNull() ?: return@validate null
                val credentials = apiKeyDb.findOneById(audience) ?: return@validate null
                ProjectUserSession(
                    projectId = credentials.projectId,
                    apiKey = audience,
                    permissions = credentials.permissions,
                    payload = credential.payload,
                )
            }
        }
        session<UserSession>(PROVIDER_USER_SESSION) {
            challenge { call.respond(HttpStatusCode.Unauthorized) }
            validate { it }
        }
    }

    install(Sessions) {
        header<UserSession>(UserSession.KEY, MongoSessionStorage(mongodb)) {
            identity { Hex.toHexString(Random.nextBytes(SESSION_KEY_BYTES)) }
            serializer = object : SessionSerializer<UserSession> {
                override fun deserialize(text: String): UserSession = json.decodeFromString(text)
                override fun serialize(session: UserSession): String = json.encodeToString(session)
            }
        }
    }

    install(PermissionAuthorization) {
        global(Permission.Global)
        extract { (it as RestrictedSession).permissions }
    }

    install(ContentNegotiation) {
        json(json)
    }

    routing {
        route(apiPath) {
            addCoreRoutes(mongodb)
        }
    }
}
