package com.multisyscorp.multi.extensions

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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