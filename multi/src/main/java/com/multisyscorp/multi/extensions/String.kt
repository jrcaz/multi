package com.multisyscorp.multi.extensions

fun String.hasValue(): Boolean {
    return this.isNotEmpty() && this.isNotBlank()
}

fun String.hasNoValue(): Boolean = !hasValue()