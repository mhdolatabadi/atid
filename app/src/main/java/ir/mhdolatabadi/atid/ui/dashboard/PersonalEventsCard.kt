package ir.mhdolatabadi.atid.ui.dashboard

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ir.mhdolatabadi.atid.data.PersonalEvent
import ir.mhdolatabadi.atid.data.PersonalEventsRepository
import ir.mhdolatabadi.atid.ui.components.glass
import ir.mhdolatabadi.atid.ui.theme.Atid
import ir.mhdolatabadi.atid.util.PersianDateUtils
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PersonalEventsCard(date: Date, dateLabel: String) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { PersonalEventsRepository(context) }
    val solar = PersianDateUtils.fromDate(date)
    val day = "${solar.year}-${solar.month}-${solar.day}"
    // Key the entire editor to the selected civil date so drafts never move to another day.
    key(day) { EventEditor(repository, day, dateLabel) }
}

@Composable
private fun EventEditor(repository: PersonalEventsRepository, day: String, dateLabel: String) {
    var revision by remember { mutableStateOf(0) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var minuteOfDay by rememberSaveable { mutableStateOf<Int?>(null) }
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val result by produceState<Result<List<PersonalEvent>>?>(null, day, revision) {
        value = withContext(Dispatchers.IO) { runCatching { repository.on(day) } }
    }
    val events = result?.getOrNull().orEmpty()
    val colors = Atid.colors

    fun mutate(action: () -> Unit) {
        busy = true
        error = null
        scope.launch {
            val saved = withContext(Dispatchers.IO) { runCatching { action() } }
            busy = false
            if (saved.isSuccess) {
                revision++
                adding = false
                title = ""
                editingId = null
                minuteOfDay = null
                deletingId = null
            } else {
                error = "ذخیرهٔ تغییرات انجام نشد؛ دوباره تلاش کن."
            }
        }
    }

    Column(Modifier.padding(top = 16.dp).fillMaxWidth().glass().padding(16.dp)) {
        Text("رویدادهای من", style = MaterialTheme.typography.titleMedium, color = colors.text)
        Text("فقط روی این دستگاه", style = MaterialTheme.typography.bodySmall, color = colors.muted)
        when {
            result == null -> Text("در حال خواندن…", color = colors.muted)
            result?.isFailure == true -> {
                Text("خواندن رویدادها انجام نشد.", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { revision++ }, modifier = Modifier.heightIn(min = 48.dp)) { Text("تلاش دوباره") }
            }
            events.isEmpty() -> Text("برای این روز رویدادی ثبت نکرده‌ای.", modifier = Modifier.padding(top = 12.dp), color = colors.muted)
            else -> events.forEach { event ->
                Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(event.title, color = colors.text)
                    Text(eventTimeLabel(event.minuteOfDay), style = MaterialTheme.typography.bodySmall, color = colors.muted)
                    Row {
                        TextButton(
                            onClick = {
                                error = null
                                editingId = event.id
                                title = event.title
                                minuteOfDay = event.minuteOfDay
                                adding = true
                            }, enabled = !busy,
                            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "ویرایش ${event.title}" }
                        ) { Text("ویرایش") }
                        TextButton(
                            onClick = { error = null; deletingId = event.id }, enabled = !busy,
                            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "حذف ${event.title}" }
                        ) { Text("حذف") }
                    }
                }
            }
        }
        TextButton(
            onClick = { error = null; editingId = null; title = ""; minuteOfDay = null; adding = true }, enabled = result?.isSuccess == true && !busy,
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Text("افزودن رویداد") }
    }

    if (adding) {
        AlertDialog(
            onDismissRequest = { if (!busy) adding = false },
            title = { Text(if (editingId == null) "رویداد جدید" else "ویرایش رویداد") },
            text = {
                Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text(dateLabel, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = title, onValueChange = { title = it; error = null }, enabled = !busy,
                        label = { Text("عنوان رویداد") }, modifier = Modifier.fillMaxWidth(),
                        isError = title.trim().length > PersonalEventsRepository.MAX_TITLE_LENGTH,
                        supportingText = { Text(if (title.trim().length > PersonalEventsRepository.MAX_TITLE_LENGTH) "عنوان حداکثر ۱۲۰ نویسه باشد." else "عنوانی کوتاه برای رویداد بنویس.") }
                    )
                    Text(eventTimeLabel(minuteOfDay), modifier = Modifier.padding(top = 12.dp))
                    TextButton(onClick = { pickingTime = true }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(if (minuteOfDay == null) "تعیین ساعت" else "تغییر ساعت")
                    }
                    if (minuteOfDay != null) {
                        TextButton(onClick = { minuteOfDay = null }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("تمام‌روز") }
                    }
                    Text("ساعت بر اساس زمان محلی دستگاه است؛ یادآوری ارسال نمی‌شود.", style = MaterialTheme.typography.bodySmall)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = editingId
                    val savedTitle = title
                    val savedTime = minuteOfDay
                    mutate {
                        if (id == null) repository.add(day, savedTitle, savedTime)
                        else repository.update(day, id, savedTitle, savedTime)
                    }
                },
                    enabled = !busy && title.trim().isNotEmpty() && title.trim().length <= PersonalEventsRepository.MAX_TITLE_LENGTH,
                    modifier = Modifier.heightIn(min = 48.dp)) { Text(if (busy) "در حال ذخیره…" else "ذخیره") }
            },
            dismissButton = { TextButton(onClick = { adding = false }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("انصراف") } }
        )
    }
    if (pickingTime && adding) {
        DisposableEffect(day) {
            val initial = minuteOfDay ?: 9 * 60
            val dialog = TimePickerDialog(context, { _, hour, minute ->
                minuteOfDay = hour * 60 + minute
                pickingTime = false
            }, initial / 60, initial % 60, true)
            dialog.setOnDismissListener { pickingTime = false }
            dialog.show()
            onDispose { dialog.setOnDismissListener(null); dialog.dismiss() }
        }
    }
    deletingId?.let { id ->
        AlertDialog(
            onDismissRequest = { if (!busy) deletingId = null },
            title = { Text("حذف رویداد؟") },
            text = { Column { Text(events.firstOrNull { it.id == id }?.title.orEmpty()); error?.let { Text(it, color = MaterialTheme.colorScheme.error) } } },
            confirmButton = { TextButton(onClick = { mutate { repository.delete(day, id) } }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("حذف") } },
            dismissButton = { TextButton(onClick = { deletingId = null }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("انصراف") } }
        )
    }
}

internal fun eventTimeLabel(minuteOfDay: Int?): String {
    if (minuteOfDay == null) return "تمام‌روز"
    val hour = (minuteOfDay / 60).toString().padStart(2, '0')
    val minute = (minuteOfDay % 60).toString().padStart(2, '0')
    return "ساعت ${PersianDateUtils.toPersianDigits(hour + ":" + minute)}"
}
