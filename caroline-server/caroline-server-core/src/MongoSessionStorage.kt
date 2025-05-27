package cloud.caroline

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.sessions.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@Serializable
internal data class SessionData(
    @SerialName("_id")
    val id: String,
    val data: String,
)

internal class MongoSessionStorage(
    mongodb: MongoDatabase,
) : SessionStorage {

    private val sessions = ConcurrentHashMap<String, String>()
    private val sessionCollection = mongodb.getCollection<SessionData>("session-data")
    private val updateOptions = UpdateOptions().upsert(true)

    override suspend fun write(id: String, value: String) {
        sessions[id] = value
        sessionCollection.updateOne(
            Filters.eq("_id", id),
            Updates.set(SessionData::data.name, value),
            updateOptions
        )
    }

    override suspend fun read(id: String): String {
        return (sessions[id]
            ?: sessionCollection
                .find(Filters.eq("_id", id))
                .firstOrNull()
                ?.data)
            ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun invalidate(id: String) {
        sessionCollection.deleteOne(Filters.eq("_id", id))
    }
}
