package ir.mhdolatabadi.atid.ui.dashboard

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
                deletingId = null
            } else {
                error = "ذخیرهٔ تغییرات انجام نشد؛ دوباره تلاش کن."
            }
        }
    }

    Column(Modifier.padding(top = 16.dp).fillMaxWidth().glass().padding(16.dp)) {
        Text("رویدادهای من", style = MaterialTheme.typography.titleMedium, color = colors.text)
        Text("تمام‌روز • فقط روی این دستگاه", style = MaterialTheme.typography.bodySmall, color = colors.muted)
        when {
            result == null -> Text("در حال خواندن…", color = colors.muted)
            result?.isFailure == true -> {
                Text("خواندن رویدادها انجام نشد.", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { revision++ }, modifier = Modifier.heightIn(min = 48.dp)) { Text("تلاش دوباره") }
            }
            events.isEmpty() -> Text("برای این روز رویدادی ثبت نکرده‌ای.", modifier = Modifier.padding(top = 12.dp), color = colors.muted)
            else -> events.forEach { event ->
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(event.title, modifier = Modifier.weight(1f).padding(top = 12.dp), color = colors.text)
                    TextButton(
                        onClick = { error = null; deletingId = event.id }, enabled = !busy,
                        modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "حذف ${event.title}" }
                    ) { Text("حذف") }
                }
            }
        }
        TextButton(
            onClick = { error = null; adding = true }, enabled = result?.isSuccess == true && !busy,
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Text("افزودن رویداد") }
    }

    if (adding) {
        AlertDialog(
            onDismissRequest = { if (!busy) adding = false },
            title = { Text("رویداد جدید") },
            text = {
                Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text(dateLabel, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = title, onValueChange = { title = it; error = null }, enabled = !busy,
                        label = { Text("عنوان رویداد") }, modifier = Modifier.fillMaxWidth(),
                        isError = title.length > PersonalEventsRepository.MAX_TITLE_LENGTH,
                        supportingText = { Text(if (title.length > PersonalEventsRepository.MAX_TITLE_LENGTH) "عنوان حداکثر ۱۲۰ نویسه باشد." else "رویداد تمام‌روز ثبت می‌شود.") }
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = { mutate { repository.add(day, title) } },
                    enabled = !busy && title.trim().isNotEmpty() && title.trim().length <= PersonalEventsRepository.MAX_TITLE_LENGTH,
                    modifier = Modifier.heightIn(min = 48.dp)) { Text(if (busy) "در حال ذخیره…" else "ذخیره") }
            },
            dismissButton = { TextButton(onClick = { adding = false }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("انصراف") } }
        )
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
