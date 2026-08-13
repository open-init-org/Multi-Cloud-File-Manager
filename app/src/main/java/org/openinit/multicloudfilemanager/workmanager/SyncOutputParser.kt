package org.openinit.multicloudfilemanager.workmanager

import org.json.JSONException
import org.json.JSONObject

internal object SyncOutputParser {
    internal enum class Level {
        NOTICE,
        WARNING,
        ERROR,
        OTHER
    }

    internal data class Event(
        val level: Level,
        val json: JSONObject,
        val hasStats: Boolean
    )

    internal fun parse(line: String): Event? {
        return try {
            val json = JSONObject(line)
            val level = try {
                Level.valueOf(json.optString("level").uppercase())
            } catch (_: IllegalArgumentException) {
                Level.OTHER
            }
            Event(level, json, json.has("stats"))
        } catch (_: JSONException) {
            null
        }
    }
}
