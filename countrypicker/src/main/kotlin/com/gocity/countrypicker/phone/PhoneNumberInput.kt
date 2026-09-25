package com.gocity.countrypicker.phone

/**
 * Turns whatever the user typed, pasted or autofilled into the selected region and the national
 * digits to keep in the number field.
 *
 * @param allowedRegions the regions the picker offers. The region is never changed to one outside it.
 * @param preferredRegion the user's own region, used to break ties between regions that share a
 * calling code, e.g. a Canadian user typing +1 gets Canada rather than the US.
 */
internal class PhoneNumberInput(
    private val allowedRegions: Set<String>,
    private val preferredRegion: String,
    private val rules: PhoneNumberRules = LibPhoneNumberRules,
) {

    data class Result(val isoCode: String, val text: String)

    /**
     * @param isoCode the currently selected region
     * @param original the field text before the edit
     * @param proposed the field text after the edit
     * @return the new region and field text, or null to reject the edit
     */
    fun process(isoCode: String, original: String, proposed: String): Result? {
        val digits = proposed.asciiDigits()
        val isInternational = proposed.trimStart().startsWith('+') ||
                (digits.startsWith("00") && !rules.couldMatch(isoCode, digits))
        val result = if (isInternational) {
            processInternational(
                isoCode,
                if (proposed.trimStart().startsWith('+')) digits else digits.drop(2)
            )
        } else {
            processNational(isoCode, digits)
        } ?: return null
        val isTooLong =
            !result.text.startsWith('+') && result.text.length > rules.maxLength(result.isoCode)
        return if (isTooLong && result.text.length > original.length) null else result
    }

    private fun processInternational(isoCode: String, digits: String): Result? {
        val split = rules.splitCallingCode(digits)
        // Keep the "+" while the user is still typing the calling code
            ?: return if (digits.length < 3) Result(isoCode, "+$digits") else null
        val regions = rules.regionsFor(split.callingCode).filter { it in allowedRegions }
        if (regions.isEmpty()) return null
        // Resolve against the new calling code's main region first so that the national prefix is
        // stripped using the right rules, then again once we know the digits
        val provisional = resolve(regions, isoCode, "") ?: regions.first()
        val national = stripNationalPrefix(provisional, split.digits)
        return Result(resolve(regions, isoCode, national) ?: provisional, national)
    }

    private fun processNational(isoCode: String, digits: String): Result {
        val national = stripNationalPrefix(isoCode, digits)
        val regions = rules.callingCode(isoCode)
            ?.let { code -> rules.regionsFor(code).filter { it in allowedRegions } }
            .orEmpty()
        return Result(resolve(regions, isoCode, national) ?: isoCode, national)
    }

    /**
     * Picks the region for [digits] out of [regions], which all share a calling code.
     *
     * Once the digits only fit one region it wins, even over a region the user picked themselves.
     * While several regions still fit, the current one is kept if possible so that the flag
     * doesn't jump around. Returns null if the digits don't fit any of them.
     */
    private fun resolve(regions: List<String>, current: String, digits: String): String? {
        val candidates =
            if (digits.isEmpty()) regions else regions.filter { rules.couldMatch(it, digits) }
        return when {
            candidates.isEmpty() -> null
            current in candidates -> current
            preferredRegion in candidates -> preferredRegion
            // regionsFor lists the main region first, e.g. US for +1
            else -> candidates.first()
        }
    }

    /**
     * People often type the national prefix out of habit, e.g. 07654 in the UK. Only strip it when
     * the digits can't be a number with it, as some prefixes are also valid first digits.
     */
    private fun stripNationalPrefix(isoCode: String, digits: String): String {
        val prefix = rules.nationalPrefix(isoCode) ?: return digits
        return if (digits.startsWith(prefix) && !rules.couldMatch(isoCode, digits)) {
            digits.removePrefix(prefix)
        } else {
            digits
        }
    }

    /** Keeps only digits, converting other scripts' digits such as Arabic-Indic ones to ASCII */
    private fun String.asciiDigits(): String =
        mapNotNull { char -> Character.digit(char, 10).takeIf { it >= 0 }?.digitToChar() }
            .joinToString("")
}
