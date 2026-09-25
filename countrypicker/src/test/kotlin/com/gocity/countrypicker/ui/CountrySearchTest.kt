package com.gocity.countrypicker.ui

import com.gocity.countrypicker.model.getAllCountries
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class CountrySearchTest {

    private val countries = getAllCountries(Locale.UK)

    private fun search(term: String) = countries.filter { it.matches(term) }.map { it.isoCode }

    @Test
    fun `matches alpha-2 codes`() {
        assertThat(search("gb")).contains("GB")
        assertThat(search("GB")).contains("GB")
    }

    @Test
    fun `matches alpha-3 codes`() {
        assertThat(search("gbr")).containsExactly("GB")
        assertThat(search("DEU")).containsExactly("DE")
        assertThat(search("usa")).containsExactly("US")
    }

    @Test
    fun `matches names and initials`() {
        assertThat(search("aland")).containsExactly("AX", "NZ") // Åland and New Zealand
        assertThat(search("uae")).contains("AE")
    }
}
