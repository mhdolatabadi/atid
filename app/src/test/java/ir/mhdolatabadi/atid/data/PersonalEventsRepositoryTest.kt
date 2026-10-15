package ir.mhdolatabadi.atid.data

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class PersonalEventsRepositoryTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Before fun clear() { context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit().clear().commit() }

    @Test fun persistsPersianTitlesAcrossRepositoryRecreationAndSeparatesDays() {
        val repo = PersonalEventsRepository(context)
        repo.add("1405-7-22", "  جلسهٔ کتاب  ")
        repo.add("1405-7-23", "ورزش")
        val restored = PersonalEventsRepository(context)
        assertEquals("جلسهٔ کتاب", restored.on("1405-7-22").single().title)
        assertEquals("ورزش", restored.on("1405-7-23").single().title)
        assertTrue(restored.on("1405-7-24").isEmpty())
    }

    @Test fun duplicateTitlesHaveIndependentIdsAndDeletionDoesNotTouchOtherEvents() {
        val repo = PersonalEventsRepository(context)
        repeat(2) { repo.add("1405-7-22", "جلسه") }
        repo.add("1405-7-23", "جلسه")
        val events = repo.on("1405-7-22")
        assertNotEquals(events[0].id, events[1].id)
        repo.delete("1405-7-22", events[0].id)
        assertEquals(listOf(events[1]), repo.on("1405-7-22"))
        assertEquals(1, repo.on("1405-7-23").size)
    }

    @Test fun invalidTitlesDoNotChangeStoredEvents() {
        val repo = PersonalEventsRepository(context)
        for (title in listOf("   ", "x".repeat(121))) {
            try { repo.add("1405-7-22", title); fail("Invalid title accepted") } catch (_: IllegalArgumentException) { }
        }
        assertTrue(repo.on("1405-7-22").isEmpty())
    }

    @Test fun malformedDataIsNotSilentlyOverwritten() {
        context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit().putString("1405-7-22", "broken").commit()
        val repo = PersonalEventsRepository(context)
        try { repo.add("1405-7-22", "جلسه"); fail("Corrupt data overwritten") } catch (_: org.json.JSONException) { }
        assertEquals("broken", context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).getString("1405-7-22", null))
    }
    @Test fun legacyEventsRemainAllDayAndSurviveEditing() {
        context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit()
            .putString("1405-7-22", """[{"id":"legacy","title":"قدیمی"}]""").commit()
        val repo = PersonalEventsRepository(context)
        assertNull(repo.on("1405-7-22").single().minuteOfDay)
        repo.update("1405-7-22", "legacy", "  ویرایش‌شده  ", 570)
        assertEquals(PersonalEvent("legacy", "ویرایش‌شده", 570), PersonalEventsRepository(context).on("1405-7-22").single())
        repo.update("1405-7-22", "legacy", "تمام‌روز")
        assertNull(PersonalEventsRepository(context).on("1405-7-22").single().minuteOfDay)
    }

    @Test fun updatesPreserveOtherEventsAndDaysAndRejectMissingIds() {
        val repo = PersonalEventsRepository(context)
        repo.add("1405-7-22", "اول")
        repo.add("1405-7-22", "دوم", 720)
        repo.add("1405-7-23", "فردا", 600)
        val original = repo.on("1405-7-22")
        val tomorrow = repo.on("1405-7-23")
        repo.update("1405-7-22", original.first().id, "تغییر", 540)
        assertEquals(original.last(), repo.on("1405-7-22").last())
        assertEquals(tomorrow, repo.on("1405-7-23"))
        val changed = repo.on("1405-7-22")
        try { repo.update("1405-7-22", "missing", "ناموجود"); fail("Missing id accepted") } catch (_: IllegalArgumentException) { }
        assertEquals(changed, repo.on("1405-7-22"))
    }

    @Test fun allDayFirstThenChronologicalIncludingMidnightAndLastMinute() {
        val repo = PersonalEventsRepository(context)
        repo.add("day", "پایان", 1439)
        repo.add("day", "ظهر", 720)
        repo.add("day", "تمام‌روز")
        repo.add("day", "نیمه‌شب", 0)
        assertEquals(listOf(null, 0, 720, 1439), repo.on("day").map { it.minuteOfDay })
    }

    @Test fun invalidEditsAndTimesLeaveDataIntact() {
        val repo = PersonalEventsRepository(context)
        repo.add("day", "معتبر", 540)
        val before = repo.on("day")
        val id = before.single().id
        for (invalidTime in listOf(-1, 1440)) {
            try { repo.add("day", "رویداد", invalidTime); fail("Invalid time accepted") } catch (_: IllegalArgumentException) { }
            try { repo.update("day", id, "رویداد", invalidTime); fail("Invalid time accepted") } catch (_: IllegalArgumentException) { }
        }
        for (invalidTitle in listOf("   ", "x".repeat(121))) {
            try { repo.update("day", id, invalidTitle); fail("Invalid title accepted") } catch (_: IllegalArgumentException) { }
        }
        assertEquals(before, repo.on("day"))
    }

}
