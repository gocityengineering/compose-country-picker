package com.gocity.countrypicker.phone

import com.gocity.countrypicker.model.PhoneNumberValidity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LibPhoneNumberRulesTest {

    private val rules = LibPhoneNumberRules

    @Test
    fun `calling codes`() {
        assertThat(rules.callingCode("GB")).isEqualTo(44)
        assertThat(rules.callingCode("US")).isEqualTo(1)
        assertThat(rules.callingCode("CA")).isEqualTo(1)
        assertThat(rules.callingCode("JE")).isEqualTo(44)
        // Bouvet Island has no phone numbers
        assertThat(rules.callingCode("BV")).isNull()
    }

    @Test
    fun `main region is first`() {
        assertThat(rules.regionsFor(1).first()).isEqualTo("US")
        assertThat(rules.regionsFor(1)).containsAtLeast("CA", "JM", "PR")
        assertThat(rules.regionsFor(44).first()).isEqualTo("GB")
        assertThat(rules.regionsFor(7).first()).isEqualTo("RU")
    }

    @Test
    fun `splits calling code`() {
        assertThat(rules.splitCallingCode("4")).isNull()
        assertThat(rules.splitCallingCode("44")).isEqualTo(InternationalNumber(44, ""))
        assertThat(rules.splitCallingCode("447700")).isEqualTo(InternationalNumber(44, "7700"))
        assertThat(rules.splitCallingCode("1212")).isEqualTo(InternationalNumber(1, "212"))
        assertThat(rules.splitCallingCode("35312")).isEqualTo(InternationalNumber(353, "12"))
    }

    @Test
    fun `partial numbers match their region`() {
        assertThat(rules.couldMatch("US", "212")).isTrue()
        assertThat(rules.couldMatch("CA", "212")).isFalse()
        assertThat(rules.couldMatch("CA", "416")).isTrue()
        assertThat(rules.couldMatch("US", "416")).isFalse()
        assertThat(rules.couldMatch("JM", "876")).isTrue()
        // Toll-free numbers are shared across NANP
        assertThat(rules.couldMatch("US", "800")).isTrue()
        assertThat(rules.couldMatch("CA", "800")).isTrue()
        // UK numbers never start with the national prefix
        assertThat(rules.couldMatch("GB", "0")).isFalse()
        assertThat(rules.couldMatch("GB", "7")).isTrue()
    }

    @Test
    fun `national prefixes`() {
        assertThat(rules.nationalPrefix("GB")).isEqualTo("0")
        assertThat(rules.nationalPrefix("US")).isEqualTo("1")
        // Italian numbers keep their leading 0 so there is no national prefix
        assertThat(rules.nationalPrefix("IT")).isNull()
    }

    @Test
    fun `formats national numbers`() {
        assertThat(rules.format("US", "2125551234")).isEqualTo("(212) 555-1234")
        assertThat(rules.format("GB", "7700900123")).isEqualTo("7700 900123")
        assertThat(rules.format("FR", "612345678")).isEqualTo("6 12 34 56 78")
    }

    @Test
    fun `formats partial numbers`() {
        assertThat(rules.format("US", "212")).isEqualTo("212")
        assertThat(rules.format("US", "2125")).isEqualTo("212-5")
        assertThat(rules.format("GB", "")).isEqualTo("")
    }

    @Test
    fun validity() {
        assertThat(rules.validity("GB", "")).isEqualTo(PhoneNumberValidity.Empty)
        assertThat(rules.validity("GB", "7700")).isEqualTo(PhoneNumberValidity.TooShort)
        assertThat(rules.validity("GB", "7400123456")).isEqualTo(PhoneNumberValidity.Valid)
        assertThat(rules.validity("GB", "74001234567890")).isEqualTo(PhoneNumberValidity.TooLong)
        assertThat(rules.validity("US", "2125551234")).isEqualTo(PhoneNumberValidity.Valid)
        // A valid US number isn't a valid Canadian one
        assertThat(rules.validity("CA", "2125551234")).isEqualTo(PhoneNumberValidity.Invalid)
    }

    @Test
    fun e164() {
        assertThat(rules.e164("GB", "7400123456")).isEqualTo("+447400123456")
        assertThat(rules.e164("IT", "0612345678")).isEqualTo("+390612345678")
        assertThat(rules.e164("GB", "7700")).isNull()
    }
}
