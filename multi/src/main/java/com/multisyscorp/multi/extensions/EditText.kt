package com.multisyscorp.multi.extensions

import android.widget.EditText

fun EditText.stringValue() = this.text.toString()

fun EditText.hasValue(): Boolean = stringValue().isNotEmpty() && stringValue().isNotBlank()