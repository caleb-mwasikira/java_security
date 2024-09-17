package org.example

import java.security.MessageDigest
import java.util.*

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

fun main() {
    val scanner = Scanner(System.`in`)

    try {
        print("Enter password: ")
        val password: String = scanner.nextLine()
        val hashedPassword: String = hashPassword(password)

        println("Hashed password is $hashedPassword")
    } catch (e: Exception) {
        println("error capturing user password: ${e.message}")
    }

    scanner.close()
}