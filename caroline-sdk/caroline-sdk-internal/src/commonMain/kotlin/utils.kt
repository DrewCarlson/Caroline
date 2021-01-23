package drewcarlson.caroline.internal

import kotlinx.datetime.Clock

public fun currentSystemMs(): Long {
    return Clock.System.now().toEpochMilliseconds()
}
