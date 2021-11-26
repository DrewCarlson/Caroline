package cloud.caroline.analytics

public interface CarolineAnalytics {

    public fun track(eventName: String, attributes: Map<String, String> = emptyMap())
}
