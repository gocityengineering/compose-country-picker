package com.gocity.countrypicker.model

/**
 * Whether a phone number is valid for the selected country.
 */
enum class PhoneNumberValidity {
    /** Nothing has been entered. */
    Empty,

    /** Too few digits for the selected country. Normal while the user is still typing. */
    TooShort,

    /** Too many digits for the selected country. */
    TooLong,

    /** The right length but not a number that the selected country issues. */
    Invalid,

    Valid;

    /** Whether this should be shown to the user as an error once they have finished typing. */
    val isError: Boolean get() = this == TooShort || this == TooLong || this == Invalid
}
