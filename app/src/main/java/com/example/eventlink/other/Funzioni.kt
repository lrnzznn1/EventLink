package com.example.eventlink.other

import java.security.MessageDigest

// Function to hash a string using SHA-256 algorithm
fun hashString(input: String): String {
    // Convert input string to byte array
    val bytes = input.toByteArray()

    // Initialize MessageDigest with SHA-256 algorithm
    val md = MessageDigest.getInstance("SHA-256")

    // Compute hash digest of the input bytes
    val digest = md.digest(bytes)

    // Convert hash digest bytes to hexadecimal string format
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}