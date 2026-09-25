package com.gocity.countrypicker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gocity.countrypicker.R
import com.gocity.countrypicker.extensions.firstLetters
import com.gocity.countrypicker.extensions.removeDiacritics
import com.gocity.countrypicker.model.Country
import com.gocity.countrypicker.model.getAllCountries
import com.gocity.countrypicker.model.iso3Code
import com.gocity.countrypicker.ui.icons.Clear
import com.gocity.countrypicker.ui.icons.CountryPickerIcon
import com.gocity.countrypicker.ui.icons.Search

@Preview(showBackground = true)
@Composable
private fun PreviewCountryPicker() {
    CountryPicker(
        Modifier.padding(16.dp),
        currentCountry = Country("GB", "United Kingdom")
    ) {
    }
}

/**
 * Country picker.
 *
 * Shows a list of all the countries with their associated emoji flag
 *
 * @param modifier the [Modifier] to be applied to this CountryPicker
 * @param countries the list of iso-3166-1 countries that the picker will show. Defaults to all iso countries with names from the current Locale.
 * @param currentCountry the currently selected country. Has a different presentation in the picker.
 * @param label the text label that the picker text box shows. Defaults to "Country"
 * @param showSearch whether the search bar should be shown. Defaults to true
 * @param shape the shape of the picker's text box and search bar, e.g. [CircleShape] for fully rounded
 * @param onCountrySelected the callback that is triggered when the user selects a countru. The selected country is returned.
 */
@Composable
fun CountryPicker(
    modifier: Modifier = Modifier,
    countries: List<Country> = getAllCountries(),
    currentCountry: Country? = null,
    label: String = stringResource(R.string.country),
    showSearch: Boolean = true,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    onCountrySelected: (Country) -> Unit
) {
    // Update the currently selected country if the Locale changes
    LaunchedEffect(Locale.current) {
        if (currentCountry != null) {
            countries.find { it.isoCode == currentCountry.isoCode }?.let { onCountrySelected(it) }
        }
    }
    var searchTerm by rememberSaveable { mutableStateOf("") }
    LargeBottomMenu(
        modifier = modifier,
        label = label,
        items = countries.filter { it.matches(searchTerm) },
        currentItem = currentCountry,
        isCurrentItem = { it.isoCode == currentCountry?.isoCode },
        onItemSelected = onCountrySelected,
        valueFormatter = { it.toUiString() },
        shape = shape,
        searchHeader = { if (showSearch) SearchHeader(searchTerm, shape) { searchTerm = it } }
    )
}

internal fun Country.matches(searchTerm: String) =
    name.removeDiacritics.contains(searchTerm.removeDiacritics, true) ||
            isoCode.equals(searchTerm, true) ||
            iso3Code.equals(searchTerm, true) ||
            name.firstLetters.equals(searchTerm, true)

@Composable
fun SearchHeader(
    searchTerm: String,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    updateSearchTerm: (String) -> Unit,
) {
    OutlinedTextField(
        value = searchTerm,
        onValueChange = updateSearchTerm,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        label = { Text(stringResource(R.string.search)) },
        leadingIcon = {
            Icon(
                CountryPickerIcon.Search,
                stringResource(R.string.search)
            )
        },
        trailingIcon = {
            if (searchTerm.isNotEmpty()) {
                Icon(
                    CountryPickerIcon.Clear, null,
                    Modifier.clickable { updateSearchTerm("") }
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        shape = shape,
    )
}
