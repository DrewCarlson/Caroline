package drewcarlson.caroline.crash

public interface CarolineCrash {

    /**
     * Set a default [key] [value] pair that will be
     * delivered with every [report].
     */
    public fun setProperty(key: String, value: String)

    /**
     * Remove the property with [key] or no-op.
     */
    public fun removeProperty(key: String)

    /**
     * Report the [throwable] and any [attributes] to
     * the server.
     */
    public fun report(
        throwable: Throwable,
        attributes: Map<String, String> = emptyMap()
    )
}
