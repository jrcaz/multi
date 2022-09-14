package com.multisyscorp.multi.extensions

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

fun Date.toDateString(format: String): String {
    return try {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.format(this)
    } catch (exception: Exception) {
        this.toString()
    }
}