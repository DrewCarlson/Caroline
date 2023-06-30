package cloud.caroline

import com.mongodb.client.model.UpdateOptions
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import java.util.concurrent.ConcurrentHashMap

@Serializable
internal data class SessionData(
    @SerialName("_id")
    val id: String,
    val data: String,
)

internal class MongoSessionStorage(
    mongodb: CoroutineDatabase,
) : SessionStorage {

    private val sessions = ConcurrentHashMap<String, String>()
    private val sessionCollection = mongodb.getCollection<SessionData>()
    private val updateOptions = UpdateOptions().upsert(true)

    override suspend fun write(id: String, value: String) {
        sessions[id] = value
        sessionCollection.updateOne(SessionData(id, value), updateOptions)
    }

    override suspend fun read(id: String): String {
        return (sessions[id] ?: sessionCollection.findOneById(id)?.data)
            ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun invalidate(id: String) {
        sessionCollection.deleteOneById(id)
    }
}
