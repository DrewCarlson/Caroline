package cloud.caroline.config

public interface CarolineConfig {

    public suspend fun getString(key: String): String
    public suspend fun getStringOrNull(key: String): String?
}
