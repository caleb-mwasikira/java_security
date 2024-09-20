package org.example

import java.security.MessageDigest

fun ByteArray.toHex(): String {
    val sb = StringBuilder()
    for (b: Byte in this) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}

fun hashPassword(password: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(password.toByteArray())

    val digest: ByteArray = md.digest()
    return digest.toHex()
}