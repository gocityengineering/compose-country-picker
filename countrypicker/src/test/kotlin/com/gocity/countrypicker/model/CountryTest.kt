package com.gocity.countrypicker.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class CountryTest {

    @Test
    fun `countries for UK`() {
        val countries = getAllCountries(Locale.UK)
        assertThat(countries.first()).isEqualTo(Country("GB", "United Kingdom"))
        assertThat(countries[2]).isEqualTo(
            Country("AX", "Åland Islands")
        ) // Handles accents correctly
        assertThat(countries.last()).isEqualTo(Country("ZW", "Zimbabwe"))
        assertThat(countries.size).isEqualTo(249)
    }

    @Test
    fun `countries for France`() {
        val countries = getAllCountries(Locale.FRANCE)
        assertThat(countries.first()).isEqualTo(Country("FR", "France"))
        assertThat(countries[2]).isEqualTo(Country("ZA", "Afrique du Sud"))
        assertThat(countries.find { it.isoCode == "GB" }).isEqualTo(Country("GB", "Royaume-Uni"))
        assertThat(countries.last()).isEqualTo(Country("ZW", "Zimbabwe"))
        assertThat(countries.size).isEqualTo(249)
    }

}
