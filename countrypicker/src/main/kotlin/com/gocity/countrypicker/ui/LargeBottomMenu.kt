package com.gocity.countrypicker.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gocity.countrypicker.extensions.ifPositive

/**
 * Because the performance of ExposedDropDownMenu is appalling when you get to 100+ rows we need an
 * alternative. Heavily borrowed from the below article
 * @see <a href="https://proandroiddev.com/improving-the-compose-dropdownmenu-88469b1ef34">Improving the Compose DropdownMenu by Peter Törnhult</a>
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> LargeBottomMenu(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String,
    items: List<T>,
    currentItem: T?,
    isCurrentItem: (T) -> Boolean = { it == currentItem },
    onItemSelected: (item: T) -> Unit,
    valueFormatter: (T) -> String = { it.toString() },
    drawItem: @Composable (T, Boolean, Boolean, () -> Unit) -> Unit = { item, selected, itemEnabled, onClick ->
        LargeBottomMenuItem(
            text = valueFormatter(item),
            selected = selected,
            enabled = itemEnabled,
            onClick = onClick,
        )
    },
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        var height by remember { mutableStateOf(64.dp) }
        val density = LocalDensity.current
        OutlinedTextField(
            value = currentItem?.let { valueFormatter(it) }.orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { with(density) { height = it.height.toDp() } },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            readOnly = true,
            singleLine = true
        )
        // Transparent clickable surface on top of OutlinedTextField
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .height(height)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled) { expanded = true },
        )
    }

    val bottomSheetState = rememberModalBottomSheetState()
    LaunchedEffect(expanded) {
        if (expanded) bottomSheetState.expand() else bottomSheetState.hide()
    }
    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            sheetState = bottomSheetState,
            dragHandle = null,
            windowInsets = WindowInsets.ime
        ) {
            val listState = rememberLazyListState()
            if (currentItem != null) {
                LaunchedEffect(Unit) {
                    items.indexOfFirst { isCurrentItem(it) }
                        .ifPositive { listState.scrollToItem(it) }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = listState,
                contentPadding = WindowInsets.systemBars.asPaddingValues()
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = item == currentItem
                    drawItem(item, isSelected, true) {
                        onItemSelected(item)
                        expanded = false
                    }
                    if (index < items.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}


@SuppressLint("PrivateResource")
@Composable
fun LargeBottomMenuItem(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(modifier = Modifier
            .clickable(enabled) { onClick() }
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            if (selected) {
                Icon(Icons.Default.Check, stringResource(androidx.compose.ui.R.string.selected))
            }
        }
    }
}
