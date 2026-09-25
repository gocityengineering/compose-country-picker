package com.gocity.countrypicker.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Icon(CountryPickerIcon.Check, "")
}

internal val CountryPickerIcon.Check: ImageVector by lazy {
    materialIcon(name = "Filled.Check") {
        materialPath {
            moveTo(9.0f, 16.17f)
            lineTo(4.83f, 12.0f)
            lineToRelative(-1.42f, 1.41f)
            lineTo(9.0f, 19.0f)
            lineTo(21.0f, 7.0f)
            lineToRelative(-1.41f, -1.41f)
            close()
        }
    }
}
