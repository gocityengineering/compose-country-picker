package com.gocity.countrypicker.phone

import com.gocity.countrypicker.phone.PhoneNumberInput.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class PhoneNumberInputTest {

    private val allRegions = Locale.getISOCountries().toSet()
    private val input = PhoneNumberInput(allRegions, preferredRegion = "GB")

    /** Types [text] one character at a time, as a user would */
    private fun PhoneNumberInput.type(isoCode: String, text: String): Result =
        text.fold(Result(isoCode, "")) { result, char ->
            process(result.isoCode, result.text, result.text + char) ?: result
        }

    @Test
    fun `keeps national digits`() {
        assertThat(input.process("GB", "", "7700900123")).isEqualTo(Result("GB", "7700900123"))
    }

    @Test
    fun `strips national prefix`() {
        assertThat(input.type("GB", "07700900123")).isEqualTo(Result("GB", "7700900123"))
        assertThat(input.type("US", "12125551234")).isEqualTo(Result("US", "2125551234"))
    }

    @Test
    fun `keeps national prefix when it is part of the number`() {
        // Russian toll-free numbers start with 800, and 8 is also Russia's national prefix
        assertThat(input.type("RU", "800")).isEqualTo(Result("RU", "800"))
        // But 8 followed by a mobile number is the national prefix
        assertThat(input.type("RU", "89")).isEqualTo(Result("RU", "9"))
    }

    @Test
    fun `digits override the selected country once they only fit one`() {
        // The user picked Canada, then typed a New York number
        assertThat(input.type("CA", "2")).isEqualTo(Result("CA", "2"))
        assertThat(input.type("CA", "21")).isEqualTo(Result("US", "21"))
        assertThat(input.type("CA", "212")).isEqualTo(Result("US", "212"))
    }

    @Test
    fun `digits switch between countries that share a calling code`() {
        assertThat(input.type("US", "416")).isEqualTo(Result("CA", "416"))
        assertThat(input.type("US", "876")).isEqualTo(Result("JM", "876"))
        assertThat(input.type("GB", "1534")).isEqualTo(Result("JE", "1534"))
    }

    @Test
    fun `shared numbers keep the selected country`() {
        assertThat(input.type("CA", "800")).isEqualTo(Result("CA", "800"))
        assertThat(input.type("JM", "800")).isEqualTo(Result("JM", "800"))
    }

    @Test
    fun `digits that fit no country keep the selected country`() {
        assertThat(input.type("GB", "7700900123").isoCode).isEqualTo("GB")
        assertThat(input.type("CA", "0").isoCode).isEqualTo("CA")
    }

    @Test
    fun `typing a calling code moves it to the country`() {
        assertThat(input.process("GB", "", "+")).isEqualTo(Result("GB", "+"))
        assertThat(input.process("GB", "+", "+4")).isEqualTo(Result("GB", "+4"))
        assertThat(input.process("FR", "+4", "+44")).isEqualTo(Result("GB", ""))
        assertThat(input.type("FR", "+447700")).isEqualTo(Result("GB", "7700"))
    }

    @Test
    fun `shared calling code picks the main country`() {
        assertThat(input.type("GB", "+1")).isEqualTo(Result("US", ""))
        assertThat(input.type("GB", "+7")).isEqualTo(Result("RU", ""))
    }

    @Test
    fun `shared calling code prefers the user's country`() {
        val canadian = PhoneNumberInput(allRegions, preferredRegion = "CA")
        assertThat(canadian.type("GB", "+1")).isEqualTo(Result("CA", ""))
        assertThat(canadian.type("GB", "+1212")).isEqualTo(Result("US", "212"))
    }

    @Test
    fun `pasted international numbers`() {
        assertThat(input.process("FR", "", "+1 (416) 555-1234")).isEqualTo(
            Result(
                "CA",
                "4165551234"
            )
        )
        assertThat(input.process("FR", "", "+44 (0)7700 900123")).isEqualTo(
            Result(
                "GB",
                "7700900123"
            )
        )
        assertThat(input.process("FR", "", "0044 7700 900123")).isEqualTo(
            Result(
                "GB",
                "7700900123"
            )
        )
    }

    @Test
    fun `pasted national numbers`() {
        assertThat(input.process("GB", "", "07700 900123")).isEqualTo(Result("GB", "7700900123"))
    }

    @Test
    fun `converts other scripts' digits`() {
        assertThat(input.process("GB", "", "٧٧٠٠")).isEqualTo(Result("GB", "7700"))
    }

    @Test
    fun `rejects unknown calling codes`() {
        assertThat(input.process("GB", "+99", "+999")).isNull()
    }

    @Test
    fun `rejects calling codes for countries that aren't offered`() {
        val ukOnly = PhoneNumberInput(setOf("GB"), preferredRegion = "GB")
        assertThat(ukOnly.process("GB", "", "+1 212 555 1234")).isNull()
        assertThat(ukOnly.type("GB", "1534").isoCode).isEqualTo("GB")
    }

    @Test
    fun `rejects digits beyond the maximum length`() {
        val full = input.type("GB", "7700900123")
        assertThat(input.process("GB", full.text, full.text + "7890123")).isNull()
    }

    @Test
    fun `allows deleting from an overlong number`() {
        val tooLong = "7".repeat(20)
        assertThat(input.process("GB", tooLong, tooLong.dropLast(1))).isNotNull()
    }
}
