package com.gocity.countrypicker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gocity.countrypicker.R
import com.gocity.countrypicker.model.Country
import com.gocity.countrypicker.model.PhoneNumberValidity
import com.gocity.countrypicker.model.dialCode

@Preview(showBackground = true)
@Composable
private fun PreviewPhoneNumberPicker() {
    PhoneNumberPicker(
        rememberPhoneNumberState(Country("GB", "United Kingdom"), "7400123456"),
        Modifier.padding(16.dp),
    )
}

/**
 * Phone number picker.
 *
 * Shows the selected country's flag and calling code next to a field for the number. The number is
 * formatted and validated using the selected country's rules, and the country changes to match
 * what is typed, e.g. typing a Toronto number after +1 selects Canada.
 *
 * Read the result from [state], e.g. [PhoneNumberState.e164].
 *
 * @param state the state of the picker, see [rememberPhoneNumberState].
 * @param modifier the [Modifier] to be applied to this PhoneNumberPicker
 * @param label the label shown above the picker. Defaults to "Phone number". Pass null to show your own.
 * @param enabled whether the picker can be used
 * @param showSearch whether the search bar should be shown in the country list. Defaults to true
 * @param shape the shape of the country button, the number field and the search bar, e.g. [CircleShape] for fully rounded
 * @param colors the colors of the country button and the number field
 * @param textStyle the style of the calling code and the number
 * @param supportingText shown below the number field for each [PhoneNumberValidity]. `isError` is
 * true once the user has finished typing an invalid number, or after [PhoneNumberState.validate],
 * and the field is then shown in its error colors. Defaults to [PhoneNumberPickerDefaults.SupportingText].
 * @param onKeyboardAction called when the user presses the keyboard's action button. The default
 * action hides the keyboard.
 */
@Composable
fun PhoneNumberPicker(
    state: PhoneNumberState,
    modifier: Modifier = Modifier,
    label: String? = stringResource(R.string.phone_number),
    enabled: Boolean = true,
    showSearch: Boolean = true,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current,
    supportingText: (@Composable (validity: PhoneNumberValidity, isError: Boolean) -> Unit)? = { validity, isError ->
        PhoneNumberPickerDefaults.SupportingText(validity, isError)
    },
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchTerm by rememberSaveable { mutableStateOf("") }
    var hasFocused by rememberSaveable { mutableStateOf(false) }

    Column(modifier) {
        if (label != null) {
            Text(
                text = label,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    // The number field announces the label instead
                    .clearAndSetSemantics {},
                style = MaterialTheme.typography.labelLarge,
                color = if (state.isError) colors.errorLabelColor else colors.unfocusedLabelColor,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DialCodeButton(
                country = state.country,
                expanded = expanded,
                enabled = enabled,
                shape = shape,
                colors = colors,
                textStyle = textStyle,
                onClick = { expanded = true },
            )
            OutlinedTextField(
                state = state.textFieldState,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.isFocused) {
                            hasFocused = true
                        } else if (hasFocused) {
                            state.hasFinishedEditing = true
                        }
                    }
                    .semantics {
                        contentType = ContentType.PhoneNumber
                        if (label != null) contentDescription = label
                    },
                enabled = enabled,
                textStyle = textStyle,
                isError = state.isError,
                supportingText = supportingText?.let { { it(state.validity, state.isError) } },
                inputTransformation = state.inputTransformation,
                outputTransformation = state.outputTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                onKeyboardAction = { performDefaultAction ->
                    state.hasFinishedEditing = true
                    onKeyboardAction?.onKeyboardAction(performDefaultAction)
                        ?: performDefaultAction()
                },
                lineLimits = TextFieldLineLimits.SingleLine,
                shape = shape,
                colors = colors,
            )
        }
    }

    LargeBottomSheet(
        expanded = expanded,
        onDismiss = {
            expanded = false
            searchTerm = ""
        },
        items = state.countries.filter { it.matches(searchTerm) || it.dialCodeMatches(searchTerm) },
        currentItem = state.country,
        isCurrentItem = { it.isoCode == state.country.isoCode },
        onItemSelected = { state.selectCountry(it) },
        searchHeader = { if (showSearch) SearchHeader(searchTerm, shape) { searchTerm = it } },
        drawItem = { country, selected, itemEnabled, onClick ->
            LargeBottomMenuItem(
                text = country.toUiString(),
                trailingText = country.dialCode,
                selected = selected,
                enabled = itemEnabled,
                onClick = onClick,
            )
        },
    )
}

object PhoneNumberPickerDefaults {
    /**
     * Shows a message for each kind of error once [isError] is true, and nothing otherwise.
     */
    @Composable
    fun SupportingText(validity: PhoneNumberValidity, isError: Boolean) {
        if (isError) Text(errorMessage(validity))
    }

    /** The default message for an invalid number, e.g. "Phone number is too short" */
    @Composable
    fun errorMessage(validity: PhoneNumberValidity): String = stringResource(
        when (validity) {
            PhoneNumberValidity.TooShort -> R.string.phone_number_too_short
            PhoneNumberValidity.TooLong -> R.string.phone_number_too_long
            else -> R.string.phone_number_invalid
        }
    )
}

/**
 * Drawn with the same decoration and container as [OutlinedTextField] so that it has the same
 * height, padding and colors as the number field at any font size.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialCodeButton(
    country: Country,
    expanded: Boolean,
    enabled: Boolean,
    shape: Shape,
    colors: TextFieldColors,
    textStyle: TextStyle,
    onClick: () -> Unit,
) {
    val description = "${country.name}, ${country.dialCode}"
    val changeCountry = stringResource(R.string.change_country)
    val interactionSource = remember { MutableInteractionSource() }
    val dialCode = country.dialCode.orEmpty()
    Box(
        modifier = Modifier
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick,
            )
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick(label = changeCountry) {
                    onClick()
                    true
                }
            }
    ) {
        OutlinedTextFieldDefaults.DecorationBox(
            value = dialCode,
            innerTextField = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val textColor =
                        if (enabled) colors.unfocusedTextColor else colors.disabledTextColor
                    Text(country.flag, style = textStyle)
                    Text(dialCode, style = textStyle, color = textColor)
                }
            },
            enabled = enabled,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = colors,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = enabled,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = shape,
                )
            },
        )
    }
}

/** Matches "44" and "+44" to the countries that use them */
private fun Country.dialCodeMatches(searchTerm: String): Boolean {
    val digits = searchTerm.trim().removePrefix("+")
    return digits.isNotEmpty() && digits.all { it.isDigit() } && dialCode?.removePrefix("+")
        ?.startsWith(digits) == true
}
