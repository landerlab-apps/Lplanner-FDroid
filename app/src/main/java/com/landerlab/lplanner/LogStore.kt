package com.landerlab.lplanner

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Date

/**
 * LogStore.kt — Lplanner Android v1.1.0
 *
 * The log is written to the app's private storage so it survives the app being
 * closed. It previously lived only in the ViewModel, so every entry was lost on
 * process death — the log looked empty on the next launch.
 *
 * Plain org.json rather than a serialization library: it is in the Android
 * framework, so this adds no dependency and nothing to keep in step.
 */
class LogStore(context: Context) {

    private val file = File(context.filesDir, "log.json")

    fun load(): List<LogEntry> {
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        LogEntry(
                            id = o.optString("id"),
                            date = Date(o.optLong("date")),
                            summary = o.optString("summary"),
                            text = o.optString("text"),
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // A corrupt log must never stop the planner from starting.
            Log.w("LogStore", "could not read log.json, starting empty", e)
            emptyList()
        }
    }

    fun save(entries: List<LogEntry>) {
        try {
            val arr = JSONArray()
            entries.forEach { e ->
                arr.put(
                    JSONObject()
                        .put("id", e.id)
                        .put("date", e.date.time)
                        .put("summary", e.summary)
                        .put("text", e.text)
                )
            }
            // Write to a temp file and rename, so an interrupted write cannot
            // leave a half-written log behind.
            val tmp = File(file.parentFile, "log.json.tmp")
            tmp.writeText(arr.toString(2))
            if (!tmp.renameTo(file)) { file.writeText(arr.toString(2)); tmp.delete() }
        } catch (e: Exception) {
            Log.w("LogStore", "could not write log.json", e)
        }
    }
}
