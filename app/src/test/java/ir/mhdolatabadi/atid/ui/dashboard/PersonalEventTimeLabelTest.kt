package ir.mhdolatabadi.atid.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalEventTimeLabelTest {
    @Test fun formatsAllDayAndTimesWithPersianDigits() {
        assertEquals("تمام‌روز", eventTimeLabel(null))
        assertEquals("ساعت ۰۰:۰۰", eventTimeLabel(0))
        assertEquals("ساعت ۰۹:۰۵", eventTimeLabel(545))
        assertEquals("ساعت ۲۳:۵۹", eventTimeLabel(1439))
    }
}
