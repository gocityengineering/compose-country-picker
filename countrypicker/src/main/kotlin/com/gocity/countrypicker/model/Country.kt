package com.gocity.countrypicker.model

import android.os.Parcelable
import com.gocity.countrypicker.extensions.removeDiacritics
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Country object.
 *
 * Represents an ISO 3166-1 country
 *
 * @param isoCode a 2-character uppercase country code
 * @param name the name of the country in the language for a particular [Locale]
 *
 * @property flag an emoji representation of the country's flag
 *
 */
@Parcelize
data class Country(val isoCode: String, val name: String) : Parcelable {
    @IgnoredOnParcel
    val flag = isoCode.map { it.getRegionalIndicatorSymbol() }.joinToString("")

    companion object {
        /**
         * The regional indicators go from 0x1F1E6 (A) to 0x1F1FF (Z).
         * This is the A regional indicator value minus 65 decimal so
         * that we can just add this to the A-Z char
         */
        private const val REGIONAL_INDICATOR_OFFSET = 0x1F1A5

        fun Char.getRegionalIndicatorSymbol(): String {
            check(this in 'A'..'Z') { "Invalid character: you must use A-Z" }
            return String(Character.toChars(REGIONAL_INDICATOR_OFFSET + code))
        }
    }

    fun toUiString(): String = "$flag  $name"
}

/**
 * Get all countries
 *
 * Returns a list of all ISO 3166-1 countries for a given [Locale].
 * The country for the [Locale] is returned first in the list, the remaining countries are sorted alphabetically.
 *
 * @param locale defaults to the current Locale of the app.
 */
fun getAllCountries(locale: Locale = Locale.getDefault()): List<Country> =
    Locale.getISOCountries().map {
        Country(it, Locale("", it).getDisplayCountry(locale))
    }
        // Put user's current locale's country at the top of the list
        .partition { it.isoCode == locale.country }
        .let { (currentCountry, remainingCounties) ->
            currentCountry + remainingCounties.sortedBy { it.name.removeDiacritics }
        } // Ensures that countries with accents in the name are sorted correctly
