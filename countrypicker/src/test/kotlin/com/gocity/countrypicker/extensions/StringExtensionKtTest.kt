package com.gocity.countrypicker.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StringExtensionKtTest {

    @Test
    fun `removing diacritics`() {
        for ((input, result) in listOf(
            "Björn" to "Bjorn",
            "Français" to "Francais",
            "René" to "Rene",
            "Español" to "Espanol",
            "rápido" to "rapido",
            "déjà" to "deja",
            "àgbọ̀n" to "agbon",
            "Sự-phản-đối-việc-tách-nhà-thờ-ra-khỏi-nhà-nước" to "Su-phan-đoi-viec-tach-nha-tho-ra-khoi-nha-nuoc",
            // The đ isn't changed :(
            "最热门的景点" to "最热门的景点", // Not changed
            "最も人気" to "最も人気" // Not changed
        )) {
            assertThat(input.removeDiacritics).isEqualTo(result)
        }
    }

    @Test
    fun `first letters`() {
        for ((input, result) in listOf(
            "United States" to "US",
            "United Kingdom" to "UK"
        )) {
            assertThat(input.firstLetters).isEqualTo(result)
        }
    }
}
