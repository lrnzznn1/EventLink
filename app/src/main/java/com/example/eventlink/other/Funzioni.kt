package com.example.eventlink.other

import android.util.Log
import com.example.eventlink.db
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

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
//Sends a raw JSON payload to a specified URL using a POST request.
@OptIn(DelicateCoroutinesApi::class)
fun rawJSON(email : String, subject : String ,text : String) {
    // Create a JSON object with email and text properties
    val jsonObject = JSONObject()
    jsonObject.put("email", email)
    jsonObject.put("subject", subject)
    jsonObject.put("text", text)

    // Convert the JSON object to a string
    val jsonObjectString = jsonObject.toString()

    // Perform network operation in a background thread
    GlobalScope.launch(Dispatchers.IO) {
        // Define the URL to send the POST request
        val url = URL("https://us-central1-eventlinkv2.cloudfunctions.net/handlePostRequest")

        // Open a connection to the URL
        val httpsURLConnection = url.openConnection() as HttpsURLConnection

        // Set the request method to POST
        httpsURLConnection.requestMethod = "POST"

        // Set request headers
        httpsURLConnection.setRequestProperty("Content-Type", "application/json")
        httpsURLConnection.setRequestProperty("Accept", "application/json")

        // Allow input and output streams
        httpsURLConnection.doInput = true
        httpsURLConnection.doOutput = true

        // Write the JSON payload to the output stream
        val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
        outputStreamWriter.write(jsonObjectString)
        outputStreamWriter.flush()

        // Print the JSON response to logcat
        val responseCode = httpsURLConnection.responseCode
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }
            withContext(Dispatchers.Main) {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(JsonParser.parseString(response))
                Log.d("Pretty Printed JSON :", prettyJson)
            }
        } else {
            Log.e("HTTPSURLCONNECTION_ERROR", responseCode.toString())
        }
    }
}

// Generates a random password consisting of uppercase letters, lowercase letters, and digits.
fun generateRandomPassword(): String {
    val length  = 12
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

// Checks if a document exists in the specified Firestore collection.
suspend fun existsInDB(collectionName: String, documentId: String): Boolean {
    return withContext(Dispatchers.IO) {
        val document= db.collection(collectionName).document(documentId).get().await()
        document!=null && document.exists()
    }
}