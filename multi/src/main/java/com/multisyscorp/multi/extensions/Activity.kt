package com.multisyscorp.multi.extensions

import android.app.Activity
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

    currentFocus?.let {
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}