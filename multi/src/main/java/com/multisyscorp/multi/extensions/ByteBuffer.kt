package com.multisyscorp.multi.extensions

import java.nio.ByteBuffer

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    return ByteArray(remaining()).also {
        get(it)
    }
}