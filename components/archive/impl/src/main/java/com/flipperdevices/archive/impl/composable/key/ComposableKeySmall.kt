package com.flipperdevices.archive.impl.composable.key

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flipperdevices.bridge.dao.api.model.FlipperKeyPath
import com.flipperdevices.core.ui.ktx.ComposableKeyType
import com.flipperdevices.core.ui.theme.LocalTypography

@Composable
fun ComposableKeySmall(
    modifier: Modifier = Modifier,
    keyPath: FlipperKeyPath,
    synchronizationContent: @Composable () -> Unit,
    onOpenKey: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 7.dp, vertical = 6.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onOpenKey
            )
    ) {
        Column {
            Row {
                ComposableKeyType(keyPath.path.keyType)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    synchronizationContent()
                }
            }
            Text(
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 12.dp
                ),
                text = keyPath.path.nameWithoutExtension,
                style = LocalTypography.current.bodyR14,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
