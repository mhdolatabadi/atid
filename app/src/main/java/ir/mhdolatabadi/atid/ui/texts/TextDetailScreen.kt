package ir.mhdolatabadi.atid.ui.texts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.ui.BottomBarClearance
import ir.mhdolatabadi.atid.ui.components.glass
import ir.mhdolatabadi.atid.ui.theme.Atid
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mhdolatabadi.atid.data.ReligiousTexts

@Composable
fun TextDetailScreen(textId: String, onBack: () -> Unit) {
    val text = ReligiousTexts.byId(textId)
    val colors = Atid.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = BottomBarClearance)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 4.dp)
        ) {
            // The chevron points right: "back" in a right-to-left layout.
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = colors.accent
            )
            Text(
                text = "بازگشت",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Text(
            text = text.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)
        )
        Text(
            text = text.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 12.dp)
        )

        Text(
            text = text.note,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 20.sp,
            color = colors.text,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.accentSoft, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )

        Text(
            text = text.body.trim(),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            textAlign = TextAlign.Justify,
            color = colors.text,
            lineHeight = 36.sp,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .glass()
                .padding(horizontal = 18.dp, vertical = 20.dp)
        )
    }
}
