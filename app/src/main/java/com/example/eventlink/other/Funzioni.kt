package com.example.eventlink.other

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

// Funzione per calcolare l'hash di una stringa utilizzando SHA-256
fun hashString(input: String): String {
    val bytes = input.toByteArray() // Converti la stringa in un array di byte
    val md = MessageDigest.getInstance("SHA-256") // Ottieni un'istanza dell'algoritmo SHA-256
    val digest = md.digest(bytes) // Calcola l'hash dei byte
    return digest.fold("") { str, it -> str + "%02x".format(it) } // Converte l'hash in una stringa esadecimale
}

// Funzione per inviare un oggetto JSON grezzo a un endpoint specifico utilizzando una richiesta HTTP POST
@OptIn(DelicateCoroutinesApi::class)
fun rawJSON(email : String, subject : String ,text : String) {
    // Crea un oggetto JSON con i parametri forniti
    val jsonObject = JSONObject()
    jsonObject.put("email", email)
    jsonObject.put("subject", subject)
    jsonObject.put("text", text)

    val jsonObjectString = jsonObject.toString()

    // Avvia una coroutine per eseguire il codice in modo asincrono
    GlobalScope.launch(Dispatchers.IO) {
        val url = URL("https://us-central1-eventlinkv2.cloudfunctions.net/handlePostRequest")
        val httpsURLConnection = url.openConnection() as HttpsURLConnection

        httpsURLConnection.requestMethod = "POST"
        httpsURLConnection.setRequestProperty("Content-Type", "application/json")
        httpsURLConnection.setRequestProperty("Accept", "application/json")
        httpsURLConnection.doInput = true
        httpsURLConnection.doOutput = true

        // Scrive l'oggetto JSON nella richiesta
        val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
        outputStreamWriter.write(jsonObjectString)
        outputStreamWriter.flush()

        // Controlla il codice di risposta della richiesta
        val responseCode = httpsURLConnection.responseCode
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            val response = httpsURLConnection.inputStream.bufferedReader().use { it.readText() }
            withContext(Dispatchers.Main) {
                val gson = GsonBuilder().setPrettyPrinting().create()
                gson.toJson(JsonParser.parseString(response))
            }
        }
    }
}

// Funzione per generare una password casuale di 12 caratteri
fun generateRandomPassword(): String {
    val length  = 12
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("") // Combina i caratteri casuali in una stringa
}

// Funzione sospesa per verificare se un documento esiste in una collezione Firestore
suspend fun existsInDB(collectionName: String, documentId: String): Boolean {
    return withContext(Dispatchers.IO) {
        val document = db.collection(collectionName).document(documentId).get().await()
        document != null && document.exists()// Restituisce true se il documento esiste
    }
}