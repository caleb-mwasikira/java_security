package org.example

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.Key
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlin.io.path.name
import kotlin.system.exitProcess


fun getOrCreateKey(keyFile: Path): Key? {
    var key: Key?

    try {
        val objInputStream = ObjectInputStream(
            FileInputStream(keyFile.toString())
        )
        key = objInputStream.readObject() as Key
        objInputStream.close()

    } catch (error: Exception) {
        println("error loading secret key from file: ${error.message}")

        val keyGen = KeyGenerator.getInstance("DES")
        keyGen.init(SecureRandom())
        key = keyGen.generateKey()

        // save secret key to file
        val objOutputStream = ObjectOutputStream(
            FileOutputStream(keyFile.toString())
        )
        objOutputStream.writeObject(key)
        objOutputStream.close()
    }

    return key
}

enum class UserAction {
    Encrypt, Decrypt
}

fun getUserAction(): UserAction {
    var userAction: UserAction? = null
    val scanner = Scanner(System.`in`)

    while (userAction == null) {
        try {
            print("What do you want to do: \n(e)ncrypt/(d)ecrypt? ")
            val userInput: String = scanner.nextLine().lowercase()

            userAction = when (userInput) {
                "e", "encrypt" -> {
                    UserAction.Encrypt
                }

                "d", "decrypt" -> {
                    UserAction.Decrypt
                }

                else -> {
                    println("Please select e/encrypt for encryption or d/decrypt for decryption")
                    null
                }
            }
        } catch (e: Exception) {
            println("Please enter a valid value")
        }
    }
    return userAction
}

fun encryptFile(cipher: Cipher, file: Path): Boolean {
    try {
        val fileInputStream = FileInputStream(file.toString())
        val fileData: ByteArray = fileInputStream.readBytes()

        println("encrypting file data")
        var encryptedData: ByteArray = cipher.doFinal(fileData)
        val fileOutputStream = FileOutputStream(file.toString())

        // store encrypted data onto file in base64 encoded form
        encryptedData = Base64.getEncoder().encode(encryptedData)
        fileOutputStream.write(encryptedData)

        // rename file to mark it as encrypted
        val newFile: Path = Path.of(file.parent.toString(), "${file.name}.enc")
        println("renaming file from ${file.name} to ${newFile.name}")
        Files.move(file, newFile)

        return true
    } catch (e: Exception) {
        println("error encrypting file $file: ${e.message}")
        return false
    }
}

fun decryptFile(cipher: Cipher, file: Path): Boolean {
    try {
        val fileInputStream = FileInputStream(file.toString())
        val fileData: ByteArray = fileInputStream.readBytes()

        // encrypted data is stored in files in its base64-encoded form,
        // so we need to base64 decode any read data before decrypting
        val encryptedData = Base64.getDecoder().decode(fileData)

        println("decrypting file data")
        val decryptedBytes: ByteArray = cipher.doFinal(encryptedData)

        val fileOutputStream = FileOutputStream(file.toString())
        fileOutputStream.write(decryptedBytes)

        // rename file
        val newFileName: String = file.name.removeSuffix(".enc")
        val newFile: Path = Path.of(file.parent.toString(), newFileName)
        println("renaming file from ${file.name} to ${newFile.name}")
        Files.move(file, newFile)
        return true

    } catch (e: Exception) {
        println("error decrypting file $file: ${e.message}")
        return false
    }
}

fun String.isInt(): Boolean {
    val num: Int? = this.toIntOrNull()
    return num != null
}

fun getResourceFile(resourcesDir: Path): Path? {
    println("Searching for files in dir $resourcesDir...")
    val files = Files.walk(resourcesDir).filter { file ->
        Files.isRegularFile(file)
    }.toList()

    if (files.isEmpty()) {
        return null
    }

    var selectedFile: Path? = null

    do {
        // display files to the user to select
        println("\n${files.size} files found in resources dir")
        for ((index, file) in files.withIndex()) {
            println("$index $file")
        }

        print("Please select a file to work on: ")
        val scanner = Scanner(System.`in`)
        val userInput = scanner.nextLine()

        if (userInput.isInt()) {
            val fileIndex: Int = userInput.toInt()
            val isValidFileIndex = fileIndex in 0..<files.size

            if (isValidFileIndex) {
                selectedFile = files[fileIndex]
            } else {
                println("Please select a valid file index between 0 to ${files.size - 1}")
            }
            continue
        }

        selectedFile = Path.of(userInput)

    } while (selectedFile == null || !files.contains(selectedFile))

    return selectedFile
}

fun main() {
    val projectDir: Path = Paths.get("").toAbsolutePath()
    val resourcesDir: Path = Paths.get(projectDir.toString(), "app/src/main/resources")

    // Get or create key
    val keyFile = Paths.get(resourcesDir.toString(), "secret.ser")
    val key: Key? = getOrCreateKey(keyFile)
    if (key == null) {
        println("error reading and generating key")
        exitProcess(1)
    }

    // Ask user to enter file to encrypt/decrypt
    val userAction: UserAction = getUserAction()
    val file: Path? = getResourceFile(resourcesDir)
    if (file == null) {
        println("There are no files in $resourcesDir directory for you to ${userAction.name.lowercase()}")
        exitProcess(1)
    }

    val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
    when (userAction) {
        UserAction.Encrypt -> {
            cipher.init(Cipher.ENCRYPT_MODE, key)
            encryptFile(cipher, file)
        }

        UserAction.Decrypt -> {
            cipher.init(Cipher.DECRYPT_MODE, key)
            decryptFile(cipher, file)
        }
    }

}