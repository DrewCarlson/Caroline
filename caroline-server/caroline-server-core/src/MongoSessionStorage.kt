package drewcarlson.caroline

import com.mongodb.client.model.UpdateOptions
import io.ktor.sessions.SessionStorage
import io.ktor.util.cio.toByteArray
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.updateOne
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

@Serializable
internal data class SessionData(
    @SerialName("_id")
    val id: String,
    val data: String,
)

internal class MongoSessionStorage(
    mongodb: CoroutineDatabase
) : SessionStorage {

    private val sessions = ConcurrentHashMap<String, ByteArray>()
    private val sessionCollection = mongodb.getCollection<SessionData>()
    private val base64Encode = Base64.getEncoder()
    private val base64Decode = Base64.getDecoder()
    private val updateOptions = UpdateOptions().upsert(true)

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) {
        coroutineScope {
            val channel = writer(Dispatchers.Unconfined, autoFlush = true) {
                provider(channel)
            }.channel

            val rawSessionData = channel.toByteArray()
            sessions[id] = rawSessionData

            // TODO: Queue updates and flush into db with interval
            val encodedSessionData = base64Encode.encodeToString(rawSessionData)
            val sessionData = SessionData(id, encodedSessionData)
            sessionCollection.updateOne(sessionData, updateOptions)
        }
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R {
        return (sessions[id] ?: sessionCollection.findOneById(id)?.data?.run(base64Decode::decode))
            ?.let { data -> consumer(ByteReadChannel(data)) }
            ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun invalidate(id: String) {
        sessionCollection.deleteOneById(id)
    }
}
