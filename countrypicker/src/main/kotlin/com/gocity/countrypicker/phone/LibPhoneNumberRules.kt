package com.gocity.countrypicker.phone

import com.gocity.countrypicker.model.PhoneNumberValidity
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.ValidationResult
import com.google.i18n.phonenumbers.Phonemetadata.PhoneMetadata
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.i18n.phonenumbers.metadata.DefaultMetadataDependenciesProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

/**
 * [PhoneNumberRules] backed by Google's libphonenumber.
 *
 * Region metadata is loaded lazily by libphonenumber the first time a region is used.
 */
internal object LibPhoneNumberRules : PhoneNumberRules {

    /** E.164 allows at most 15 digits including the calling code */
    private const val MAX_E164_LENGTH = 15

    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val metadataSource =
        DefaultMetadataDependenciesProvider.getInstance().phoneNumberMetadataSource

    /** Built from libphonenumber's static calling code map, so it doesn't load any region metadata */
    private val callingCodes: Map<String, Int> by lazy {
        util.supportedCallingCodes
            .flatMap { code -> util.getRegionCodesForCountryCode(code).map { it to code } }
            .toMap()
    }

    private val numberPatterns = ConcurrentHashMap<String, List<Pattern>>()

    override fun callingCode(isoCode: String): Int? = callingCodes[isoCode]

    override fun regionsFor(callingCode: Int): List<String> =
        util.getRegionCodesForCountryCode(callingCode)

    override fun splitCallingCode(digits: String): InternationalNumber? =
        // Calling codes are 1-3 digits and no calling code is the start of another
        (1..minOf(3, digits.length))
            .map { digits.take(it).toInt() }
            .firstOrNull { it in util.supportedCallingCodes }
            ?.let { InternationalNumber(it, digits.drop(it.toString().length)) }

    override fun couldMatch(isoCode: String, digits: String): Boolean =
        numberPatterns(isoCode).any {
            val matcher = it.matcher(digits)
            // hitEnd() means the match ran out of input, so more digits could still match
            matcher.matches() || matcher.hitEnd()
        }

    override fun nationalPrefix(isoCode: String): String? =
        util.getNddPrefixForRegion(isoCode, true)?.takeIf { it.isNotEmpty() }

    override fun maxLength(isoCode: String): Int =
        metadata(isoCode)?.generalDesc?.possibleLengthList?.maxOrNull()
            ?: (MAX_E164_LENGTH - (callingCode(isoCode)?.toString()?.length ?: 1))

    override fun format(isoCode: String, digits: String): String =
    // Some regions' formats, e.g. the UK's, only apply when the national prefix is typed, so
        // fall back to formatting with it and then with the calling code, and remove it afterwards
        listOfNotNull("", nationalPrefix(isoCode), callingCode(isoCode)?.let { "+$it" })
            .firstNotNullOfOrNull { lead -> formatWith(isoCode, lead, digits) }
            ?: digits

    /**
     * Formats [lead] followed by [digits] and then removes [lead]. Returns null unless the result
     * is just [digits] with separators, as occasionally the formatter gives up or adds digits, or
     * half-formats a partial number, e.g. "(212".
     */
    private fun formatWith(isoCode: String, lead: String, digits: String): String? {
        val formatter = util.getAsYouTypeFormatter(isoCode)
        val formatted = (lead + digits).fold("") { _, char -> formatter.inputDigit(char) }
        if (!formatted.startsWith(lead)) return null
        val withoutLead = formatted.removePrefix(lead).trimStart { it == ' ' || it == '-' }
        return withoutLead.takeIf { result ->
            result.filter { it.isDigit() } == digits &&
                    result.any { !it.isDigit() } &&
                    result.count { it == '(' } == result.count { it == ')' }
        }
    }

    override fun validity(isoCode: String, digits: String): PhoneNumberValidity {
        if (digits.isEmpty()) return PhoneNumberValidity.Empty
        val number = parse(isoCode, digits) ?: return PhoneNumberValidity.TooShort
        return when (util.isPossibleNumberWithReason(number)) {
            ValidationResult.IS_POSSIBLE ->
                if (util.isValidNumberForRegion(number, isoCode)) PhoneNumberValidity.Valid
                else PhoneNumberValidity.Invalid

            ValidationResult.TOO_SHORT, ValidationResult.IS_POSSIBLE_LOCAL_ONLY -> PhoneNumberValidity.TooShort
            ValidationResult.TOO_LONG -> PhoneNumberValidity.TooLong
            else -> PhoneNumberValidity.Invalid
        }
    }

    override fun e164(isoCode: String, digits: String): String? =
        if (validity(isoCode, digits) == PhoneNumberValidity.Valid) {
            parse(isoCode, digits)?.let { util.format(it, PhoneNumberFormat.E164) }
        } else {
            null
        }

    private fun parse(isoCode: String, digits: String): PhoneNumber? {
        val callingCode = callingCode(isoCode) ?: return null
        return try {
            // Parsing as an international number keeps significant leading zeros, e.g. Italy's
            util.parse("+$callingCode$digits", null)
        } catch (_: NumberParseException) {
            null
        }
    }

    private fun metadata(isoCode: String): PhoneMetadata? =
        try {
            metadataSource.getMetadataForRegion(isoCode)
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun numberPatterns(isoCode: String): List<Pattern> =
        numberPatterns.getOrPut(isoCode) {
            val metadata = metadata(isoCode) ?: return@getOrPut emptyList()
            // The general pattern is too broad to tell regions that share a calling code apart,
            // e.g. it matches every NANP number, so use the patterns for each type of number.
            val typePatterns = with(metadata) {
                listOf(
                    fixedLine, mobile, tollFree, premiumRate, sharedCost, personalNumber,
                    voip, pager, uan, voicemail
                )
            }.mapNotNull { desc -> desc?.nationalNumberPattern?.takeIf { it.isNotEmpty() } }
            val patterns = typePatterns.ifEmpty {
                listOfNotNull(metadata.generalDesc?.nationalNumberPattern?.takeIf { it.isNotEmpty() })
            }
            patterns.map { Pattern.compile(it) }
        }
}
