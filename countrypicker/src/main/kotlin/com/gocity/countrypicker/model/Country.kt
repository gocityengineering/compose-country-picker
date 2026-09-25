package com.gocity.countrypicker.model

import android.os.Parcelable
import com.gocity.countrypicker.extensions.removeDiacritics
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.IllformedLocaleException
import java.util.Locale
import java.util.MissingResourceException

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
    Locale.getISOCountries().map { countryFor(it, locale) }
        // Put user's current locale's country at the top of the list
        .partition { it.isoCode == locale.country }
        .let { (currentCountry, remainingCounties) ->
            currentCountry + remainingCounties.sortedBy { it.name.removeDiacritics }
        } // Ensures that countries with accents in the name are sorted correctly

internal fun countryFor(isoCode: String, locale: Locale = Locale.getDefault()) =
    Country(isoCode, regionLocale(isoCode).getDisplayCountry(locale))

/** The ISO 3166-1 alpha-3 code, e.g. "GBR", or null if Java doesn't know it */
internal val Country.iso3Code: String?
    get() = try {
        regionLocale(isoCode).isO3Country.takeIf { it.isNotEmpty() }
    } catch (_: IllformedLocaleException) {
        null
    } catch (_: MissingResourceException) {
        null
    }

/** A [Locale] for just a region, which is all that's needed to look up a country's details */
private fun regionLocale(isoCode: String): Locale = Locale.Builder().setRegion(isoCode).build()
