package com.gocity.countrypicker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gocity.countrypicker.R
import com.gocity.countrypicker.model.Country
import com.gocity.countrypicker.model.getAllCountries

@Preview(showBackground = true)
@Composable
fun PreviewCountryPicker() {
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
 * @param onCountrySelected the callback that is triggered when the user selects a countru. The selected country is returned.
 */
@Composable
fun CountryPicker(
    modifier: Modifier = Modifier,
    countries: List<Country> = getAllCountries(),
    currentCountry: Country? = null,
    label: String = stringResource(R.string.country),
    onCountrySelected: (Country) -> Unit
) {
    // Update the currently selected country if the Locale changes
    LaunchedEffect(Locale.current) {
        if (currentCountry != null) {
            countries.find { it.isoCode == currentCountry.isoCode }?.let { onCountrySelected(it) }
        }
    }
    LargeBottomMenu(
        modifier = modifier,
        label = label,
        items = countries,
        currentItem = currentCountry,
        isCurrentItem = { it.isoCode == currentCountry?.isoCode },
        onItemSelected = onCountrySelected,
        valueFormatter = { it.toUiString() }
    )
}
