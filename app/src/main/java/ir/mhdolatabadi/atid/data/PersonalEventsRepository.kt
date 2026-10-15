package ir.mhdolatabadi.atid.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PersonalEvent(val id: String, val title: String)

/** All-day events keyed by civil Jalali date, never by a timezone-dependent instant. */
class PersonalEventsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("personal_events", Context.MODE_PRIVATE)

    @Synchronized
    fun on(day: String): List<PersonalEvent> {
        val array = JSONArray(preferences.getString(day, "[]"))
        return (0 until array.length()).map { index ->
            val event = array.getJSONObject(index)
            PersonalEvent(event.getString("id"), event.getString("title"))
        }
    }

    @Synchronized
    fun add(day: String, title: String) {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty() && trimmed.length <= MAX_TITLE_LENGTH)
        save(day, on(day) + PersonalEvent(UUID.randomUUID().toString(), trimmed))
    }

    @Synchronized
    fun delete(day: String, id: String) = save(day, on(day).filterNot { it.id == id })

    private fun save(day: String, events: List<PersonalEvent>) {
        val array = JSONArray()
        events.forEach { array.put(JSONObject().put("id", it.id).put("title", it.title)) }
        check(preferences.edit().putString(day, array.toString()).commit())
    }

    companion object {
        const val MAX_TITLE_LENGTH = 120
    }
}
