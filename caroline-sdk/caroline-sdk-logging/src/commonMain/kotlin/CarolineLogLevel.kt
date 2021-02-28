package tools.caroline.logging

/**
 * Logging severity identifier used for organization and configuration.
 * Includes: [DEBUG], [INFO], [WARN], [ERROR], [FATAL], [TRACE]
 */
public object CarolineLogLevel {
    public const val DEBUG: Int = 0
    public const val INFO: Int = 1
    public const val WARN: Int = 2
    public const val ERROR: Int = 3
    public const val FATAL: Int = 4
    public const val TRACE: Int = 5
}
