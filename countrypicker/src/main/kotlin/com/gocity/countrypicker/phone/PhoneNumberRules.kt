package com.gocity.countrypicker.phone

import com.gocity.countrypicker.model.PhoneNumberValidity

/**
 * Everything the phone number picker needs to know about numbering plans.
 *
 * All digits passed in and out are the national significant number, i.e. without the calling code
 * and without any national (trunk) prefix such as the UK's leading 0.
 */
internal interface PhoneNumberRules {
    /** The calling code for an ISO 3166-1 region, or null if it has none. */
    fun callingCode(isoCode: String): Int?

    /** All regions that share a calling code. The first is the main region, e.g. US for 1. */
    fun regionsFor(callingCode: Int): List<String>

    /**
     * Splits [digits] that follow a "+" into a calling code and the rest, e.g. "447654" into 44 and
     * "7654". Returns null until the digits start with a complete calling code.
     */
    fun splitCallingCode(digits: String): InternationalNumber?

    /** Whether [digits] is, or could be the start of, a number in [isoCode]. */
    fun couldMatch(isoCode: String, digits: String): Boolean

    /** The national (trunk) prefix that people type before local numbers, e.g. "0" for GB. */
    fun nationalPrefix(isoCode: String): String?

    /** The longest national significant number [isoCode] allows. */
    fun maxLength(isoCode: String): Int

    /** Formats [digits] using [isoCode]'s rules, e.g. "(212) 555-1234". */
    fun format(isoCode: String, digits: String): String

    fun validity(isoCode: String, digits: String): PhoneNumberValidity

    /** The number in E.164 format, e.g. "+12125551234", or null if it isn't valid. */
    fun e164(isoCode: String, digits: String): String?
}

internal data class InternationalNumber(val callingCode: Int, val digits: String)
