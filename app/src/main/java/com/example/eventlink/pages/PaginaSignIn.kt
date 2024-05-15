package com.example.eventlink.pages

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.eventlink.R
import com.example.eventlink.db
import com.example.eventlink.other.hashString
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PaginaSignIn : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        // Initializing UI elements
        val spinnerDay = findViewById<Spinner>(R.id.Giorno)
        val spinnerMonth = findViewById<Spinner>(R.id.Mese)
        val spinnerYear = findViewById<Spinner>(R.id.Anno)
        val reg = findViewById<Button>(R.id.buttonSignup)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val nameField = findViewById<EditText>(R.id.editTextNome)
        val surnameField = findViewById<EditText>(R.id.editTextCognome)
        val phoneField = findViewById<EditText>(R.id.editTextTelefono)

        // Creating lists for spinners
        val itemsDay = ArrayList<String>()
        for (i in 1..31) itemsDay.add(String.format("%02d", i))

        val itemsMonth = ArrayList<String>()
        for (i in 1..12) itemsMonth.add(String.format("%02d", i))

        val itemsYear = ArrayList<String>()
        for (i in 1900..2030) itemsYear.add(i.toString())

        // Creating adapters for spinners
        val adapterDay = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsDay)
        val adapterMonth = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsMonth)
        val adapterYear = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsYear)

        // Setting dropdown view resource for adapters
        adapterDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Setting adapters for spinners
        spinnerDay.adapter = adapterDay
        spinnerMonth.adapter = adapterMonth
        spinnerYear.adapter = adapterYear

        spinnerDay.setSelection(0)
        spinnerMonth.setSelection(0)
        spinnerYear.setSelection(100)

        // Setting click listener for sign-up button
        reg.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
            reg.startAnimation(animation)
            val dateOfBirth = spinnerDay.getSelectedItem().toString() + "/" +
                    spinnerMonth.getSelectedItem().toString() + "/" +
                    spinnerYear.getSelectedItem().toString()
            val name = nameField.text.toString()
            val surname = surnameField.text.toString()
            val phone = phoneField.text.toString()
            val email = emailField.text.toString()
            val password = generateRandomPassword()
            var block = false
            var exist: Boolean
            exist = false

            // Validating user input
            if (name.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Nome non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            } else if (surname.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Cognome non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            } else if (email.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Email non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            else if (phone.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Numero di Telefono non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            }

            // Proceeding with sign-up if input is valid
            if(!block) {
                runBlocking {
                    exist = existsInDB("Utenti", email)
                }
                if(exist)
                {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Registrazione Fallita")
                    builder.setMessage("La tua registrazione non è avvenuta con successo, potrebbe esistere già un account con questa mail, in caso Accedi!")
                    builder.setPositiveButton("OK",null)
                    val dialog = builder.create()
                    dialog.show()
                }
                else{
                    // Creating user document in Firestore
                    db.collection("Utenti").document(email).set(
                        mapOf
                            (
                            "Nome" to name,
                            "Cognome" to surname,
                            "Telefono" to phone,
                            "Password" to hashString(password),
                            "DDN" to dateOfBirth
                        )
                    ).addOnSuccessListener {
                        // Sending registration confirmation email
                        rawJSON(email,"Registrazione EventLink", "" +
                                "Gentile Cliente $name $surname, \n\n" +
                                "Siamo lieti di darle il benvenuto alla nostra applicazione. La sua registrazione è stata completata con successo. \n\n" +
                                "Di seguito troverà le credenziali necessarie per accedere al suo account:\n\n" +
                                "Email: $email \n" +
                                "Password: $password \n\n" +
                                "Grazie per aver scelto di unirsi a noi. Per qualsiasi domanda o assistenza, non esiti a contattarci. \n\n" +
                                "Cordiali saluti, \n\n" +
                                "EventLink"
                        )
                        // Showing success dialog
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Registrazione Avvenuta")
                        builder.setMessage("La tua registrazione è avvenuta con successo!\nControlla la mail per ottenere la password.")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }.addOnFailureListener{
                        // Showing failure dialog
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Registrazione Fallita")
                        builder.setMessage("La tua registrazione non è avvenuta con successo, potresti avere già un account, in caso Accedi!")
                        builder.setPositiveButton("OK", null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }
    }

    // Generates a random password consisting of uppercase letters, lowercase letters, and digits.
    private fun generateRandomPassword(): String {
        val length  = 12
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    // Checks if a document exists in the specified Firestore collection.
    private suspend fun existsInDB(collectionName: String, documentId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val document= db.collection(collectionName).document(documentId).get().await()
            document!=null && document.exists()
        }
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
}