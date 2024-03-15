package com.gocity.countrypicker.extensions

inline fun Int.ifPositive(block: (Int) -> Unit) =
    if (this > -1) block(this) else Unit
