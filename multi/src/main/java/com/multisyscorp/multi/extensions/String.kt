package com.multisyscorp.multi.extensions

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern

fun String.hasValue(): Boolean {
    return this.isNotEmpty() && this.isNotBlank()
}

fun String.hasNoValue(): Boolean = !hasValue()

fun String.isJSON(): Boolean {
    try {
        JSONObject(this)
    } catch (e: JSONException) {
        try {
            JSONArray(this)
        } catch (e: JSONException) {
            return false
        }
    }

    if (!this.contains("{") || !this.contains("}"))
        return false

    return true
}

fun String.isValidMobileNumber(): Boolean {
    val regEx = "^[0][0-9]{10}$"
    val inputStr: CharSequence = this
    val pattern: Pattern = Pattern.compile(regEx, Pattern.UNICODE_CASE)
    val matcher: Matcher = pattern.matcher(inputStr)
    return matcher.matches()
}