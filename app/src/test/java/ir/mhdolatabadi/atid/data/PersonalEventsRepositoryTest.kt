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
}
