package ir.mhdolatabadi.atid.notification

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ir.mhdolatabadi.atid.R

/** Gives every Saatbashi notification the approved portrait and the brand color. */
internal fun NotificationCompat.Builder.saatbashiBranding(context: Context): NotificationCompat.Builder {
    // A missing or undecodable portrait must never stop the notification itself.
    runCatching { BitmapFactory.decodeResource(context.resources, R.drawable.saatbashi_notification) }
        .getOrNull()?.let { setLargeIcon(it) }
    return setColor(ContextCompat.getColor(context, R.color.brand_primary))
}
