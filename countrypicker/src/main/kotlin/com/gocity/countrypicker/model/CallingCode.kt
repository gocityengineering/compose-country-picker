package com.gocity.countrypicker.model

import com.gocity.countrypicker.phone.LibPhoneNumberRules
import java.util.Locale

/**
 * The country's international calling code, e.g. 44 for the United Kingdom, or null if it has none.
 *
 * Several countries can share a calling code, e.g. the United States, Canada and much of the
 * Caribbean all use 1.
 */
val Country.callingCode: Int?
    get() = LibPhoneNumberRules.callingCode(isoCode)

/**
 * The country's calling code as it is dialled, e.g. "+44", or null if it has none.
 */
val Country.dialCode: String?
    get() = callingCode?.let { "+$it" }

/**
 * Get all countries that have phone numbers
 *
 * Returns the same list as [getAllCountries] without the few territories that have no calling code.
 *
 * @param locale defaults to the current Locale of the app.
 */
fun getAllPhoneCountries(locale: Locale = Locale.getDefault()): List<Country> =
    getAllCountries(locale).filter { it.callingCode != null }
