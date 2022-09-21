package com.multisyscorp.multi.extensions

import android.util.SparseArray
import androidx.core.util.forEach

fun <T> SparseArray<T>.toList(): List<T>? {
    val arrayList = ArrayList<T>()
    return try {
        forEach { _, value ->
            arrayList.add(value)
        }
        arrayList.toList()
    } catch (_: Exception) {
        null
    }
}