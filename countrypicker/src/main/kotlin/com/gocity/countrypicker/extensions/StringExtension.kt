package com.gocity.countrypicker.extensions

import java.text.Normalizer

private val DIACRITICS = Regex("\\p{InCombiningDiacriticalMarks}+")

internal val String.removeDiacritics
    get() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(DIACRITICS, "")

internal val String.firstLetters
    get() = split(" ").joinToString("") { it.first().toString() }
