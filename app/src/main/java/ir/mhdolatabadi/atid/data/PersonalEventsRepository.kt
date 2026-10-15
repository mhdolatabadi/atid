package ir.mhdolatabadi.atid.data

import android.content.Context
import ir.mhdolatabadi.atid.notification.EventReminderScheduler
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PersonalEvent(val id: String, val title: String, val minuteOfDay: Int? = null, val reminder: Boolean = false, val reminderLeadMinutes: Int = 0) {
    init { require(minuteOfDay == null || minuteOfDay in 0..1439); require(!reminder || minuteOfDay != null); require(reminderLeadMinutes in REMINDER_LEADS) }
    companion object { val REMINDER_LEADS = listOf(0, 5, 15, 30, 60) }
}

/** Personal events keyed by civil Jalali date, never by a timezone-dependent instant. */
class PersonalEventsRepository(private val context: Context) {
    private val preferences = context.getSharedPreferences("personal_events", Context.MODE_PRIVATE)

    @Synchronized
    fun on(day: String): List<PersonalEvent> {
        val array = JSONArray(preferences.getString(day, "[]"))
        return (0 until array.length()).map { index ->
            val event = array.getJSONObject(index)
            PersonalEvent(
                event.getString("id"), event.getString("title"),
                if (event.isNull("minuteOfDay")) null else event.getInt("minuteOfDay"),
                event.optBoolean("reminder", false), event.optInt("reminderLeadMinutes", 0)
            )
        }.sortedBy { it.minuteOfDay ?: -1 }
    }

    @Synchronized
    fun add(day: String, title: String, minuteOfDay: Int? = null, reminder: Boolean = false, reminderLeadMinutes: Int = 0) {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty() && trimmed.length <= MAX_TITLE_LENGTH)
        save(day, on(day) + PersonalEvent(UUID.randomUUID().toString(), trimmed, minuteOfDay, reminder, reminderLeadMinutes))
    }

    @Synchronized
    fun update(day: String, id: String, title: String, minuteOfDay: Int? = null, reminder: Boolean = false, reminderLeadMinutes: Int = 0) {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty() && trimmed.length <= MAX_TITLE_LENGTH)
        val replacement = PersonalEvent(id, trimmed, minuteOfDay, reminder, reminderLeadMinutes)
        val events = on(day)
        require(events.any { it.id == id }) { "Event no longer exists" }
        save(day, events.map { if (it.id == id) replacement else it })
    }

    @Synchronized
    fun delete(day: String, id: String) = save(day, on(day).filterNot { it.id == id })

    fun days(): List<String> = preferences.all.keys.filter { it.matches(Regex("[0-9]+-[0-9]+-[0-9]+")) }

    private fun save(day: String, events: List<PersonalEvent>) {
        val previous = on(day)
        val array = JSONArray()
        events.forEach { array.put(JSONObject().put("id", it.id).put("title", it.title).put("minuteOfDay", it.minuteOfDay ?: JSONObject.NULL).put("reminder", it.reminder).put("reminderLeadMinutes", it.reminderLeadMinutes)) }
        check(preferences.edit().putString(day, array.toString()).commit())
        // Persistence succeeded even if the OS cannot accept an alarm; restore retries on next launch.
        runCatching { EventReminderScheduler.replaceDay(context, day, previous, events) }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 120
    }
}
