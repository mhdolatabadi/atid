package ir.mhdolatabadi.atid.ui.texts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import ir.mhdolatabadi.atid.ui.BottomBarClearance
import ir.mhdolatabadi.atid.ui.components.GlassShape
import ir.mhdolatabadi.atid.ui.components.glass
import ir.mhdolatabadi.atid.ui.components.pressScale
import ir.mhdolatabadi.atid.ui.theme.Atid
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.data.ReligiousText
import ir.mhdolatabadi.atid.data.ReligiousTexts

@Composable
fun TextsScreen(onTextClick: (String) -> Unit) {
    val colors = Atid.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = BottomBarClearance),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                Text(
                    text = stringResource(R.string.title_texts),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    text = stringResource(R.string.label_religious_texts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        itemsIndexed(ReligiousTexts.all) { index, text ->
            TextCard(text = text, index = index, onClick = { onTextClick(text.id) })
        }
    }
}

@Composable
private fun TextCard(text: ReligiousText, index: Int, onClick: () -> Unit) {
    val colors = Atid.colors
    val interaction = remember { MutableInteractionSource() }
    // Cards rise into place one after another when the list first appears.
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(60L * index)
        appear.animateTo(1f, tween(520))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 24.dp.toPx()
            }
            .pressScale(interaction, pressed = 0.97f)
            .glass()
            .clip(GlassShape)
            .clickable(interactionSource = interaction, indication = rememberRipple(), onClick = onClick)
            .padding(18.dp)
    ) {
        Text(
            text = text.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Text(
            text = text.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
