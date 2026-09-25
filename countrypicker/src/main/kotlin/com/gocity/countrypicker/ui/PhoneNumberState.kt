package com.gocity.countrypicker.ui

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import com.gocity.countrypicker.model.Country
import com.gocity.countrypicker.model.PhoneNumberValidity
import com.gocity.countrypicker.model.countryFor
import com.gocity.countrypicker.model.getAllPhoneCountries
import com.gocity.countrypicker.phone.LibPhoneNumberRules
import com.gocity.countrypicker.phone.PhoneNumberInput
import com.gocity.countrypicker.phone.PhoneNumberRules

/**
 * State for a [PhoneNumberPicker].
 *
 * Holds the selected country and the number, which are kept in step with each other: typing
 * digits that only belong to another country with the same calling code changes the country, and
 * typing or pasting an international number such as "+44 7400 123456" sets the country from it.
 *
 * @param initialCountry the country selected at first.
 * @param initialNumber the number shown at first, either national or international, e.g. "+447400123456".
 * @param countries the countries the picker offers. The country is never changed to one outside this list.
 */
@Stable
class PhoneNumberState(
    initialCountry: Country,
    initialNumber: String = "",
    countries: List<Country> = getAllPhoneCountries(),
) {
    private val rules: PhoneNumberRules = LibPhoneNumberRules

    internal var countries by mutableStateOf(countries)

    private var isoCode by mutableStateOf(initialCountry.isoCode)

    /** The selected country */
    val country: Country
        get() = countries.find { it.isoCode == isoCode } ?: countryFor(isoCode)

    /** Holds the national digits. Formatting is only applied on screen by [outputTransformation]. */
    internal val textFieldState = TextFieldState()

    /**
     * The national number that has been entered, as digits without formatting, e.g. "7400123456".
     */
    val number: String by derivedStateOf {
        textFieldState.text.toString().takeUnless { it.startsWith('+') }.orEmpty()
    }

    /** Whether [number] is a valid phone number for [country] */
    val validity: PhoneNumberValidity by derivedStateOf { rules.validity(isoCode, number) }

    /** The number in E.164 format, e.g. "+447400123456", or null if it isn't valid. */
    val e164: String? by derivedStateOf { rules.e164(isoCode, number) }

    /** Errors are only shown once the user has finished typing, or after [validate]. */
    internal var hasFinishedEditing by mutableStateOf(false)

    /** Whether the picker is showing an error */
    val isError: Boolean get() = hasFinishedEditing && validity.isError

    init {
        setNumber(initialNumber)
    }

    /** Selects a country, keeping the digits that have been entered. */
    fun selectCountry(country: Country) {
        isoCode = country.isoCode
        // A half-typed calling code no longer applies
        if (textFieldState.text.startsWith('+')) textFieldState.clearText()
    }

    /**
     * Replaces the number, e.g. with one the user saved earlier. International numbers such as
     * "+447400123456" also change the country. Numbers that can't be entered are ignored.
     */
    fun setNumber(number: String) {
        val result = input().process(isoCode, "", number) ?: return
        isoCode = result.isoCode
        textFieldState.setTextAndPlaceCursorAtEnd(result.text)
    }

    /**
     * Shows any error straight away, e.g. when a form is submitted.
     *
     * @return whether the number is valid
     */
    fun validate(): Boolean {
        hasFinishedEditing = true
        return validity == PhoneNumberValidity.Valid
    }

    internal val inputTransformation = InputTransformation {
        val result = input().process(isoCode, originalText.toString(), asCharSequence().toString())
        if (result == null) {
            revertAllChanges()
        } else {
            if (!asCharSequence().contentEquals(result.text)) replace(0, length, result.text)
            isoCode = result.isoCode
        }
    }

    internal val outputTransformation = OutputTransformation {
        val digits = asCharSequence().toString()
        if (digits.startsWith('+')) return@OutputTransformation
        // The formatted number is the digits with separators added, so insert each separator
        rules.format(isoCode, digits).forEachIndexed { index, char ->
            if (!char.isDigit()) insert(index, char.toString())
        }
    }

    private fun input() = PhoneNumberInput(
        allowedRegions = countries.mapTo(HashSet()) { it.isoCode },
        preferredRegion = java.util.Locale.getDefault().country,
        rules = rules,
    )

    companion object {
        /** Saves the state for [rememberSaveable]. The countries are supplied again on restore. */
        fun saver(countries: List<Country>): Saver<PhoneNumberState, Any> = listSaver(
            save = { listOf(it.isoCode, it.textFieldState.text.toString(), it.hasFinishedEditing) },
            restore = { saved ->
                // Restored as it was rather than processed again, which could change the country
                PhoneNumberState(countryFor(saved[0] as String), countries = countries).apply {
                    textFieldState.setTextAndPlaceCursorAtEnd(saved[1] as String)
                    hasFinishedEditing = saved[2] as Boolean
                }
            }
        )
    }
}

/**
 * Creates a [PhoneNumberState] that survives configuration changes and process death.
 *
 * @param initialCountry the country selected at first. Defaults to the user's own country.
 * @param initialNumber the number shown at first, either national or international, e.g. "+447400123456".
 * @param countries the countries the picker offers. Defaults to every country that has phone numbers,
 * with the user's own country first.
 */
@Composable
fun rememberPhoneNumberState(
    initialCountry: Country? = null,
    initialNumber: String = "",
    countries: List<Country> = remember(Locale.current) { getAllPhoneCountries() },
): PhoneNumberState {
    val state = rememberSaveable(saver = PhoneNumberState.saver(countries)) {
        PhoneNumberState(initialCountry ?: countries.first(), initialNumber, countries)
    }
    // Keep the country names up to date if the Locale changes
    SideEffect { state.countries = countries }
    return state
}
