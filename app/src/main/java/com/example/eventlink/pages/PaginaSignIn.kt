package com.example.eventlink.pages

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.eventlink.R
import com.example.eventlink.db
import com.example.eventlink.other.existsInDB
import com.example.eventlink.other.generateRandomPassword
import com.example.eventlink.other.hashString
import com.example.eventlink.other.rawJSON
import kotlinx.coroutines.runBlocking

class PaginaSignIn : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        // Inizializzo gli elementi dell'interfaccia utente
        val spinnerDay = findViewById<Spinner>(R.id.Giorno)
        val spinnerMonth = findViewById<Spinner>(R.id.Mese)
        val spinnerYear = findViewById<Spinner>(R.id.Anno)
        val reg = findViewById<Button>(R.id.buttonSignup)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val nameField = findViewById<EditText>(R.id.editTextNome)
        val surnameField = findViewById<EditText>(R.id.editTextCognome)
        val phoneField = findViewById<EditText>(R.id.editTextTelefono)

        // Creo le liste per i valori dei spinner
        val itemsDay = ArrayList<String>()
        for (i in 1..31) itemsDay.add(String.format("%02d", i))

        val itemsMonth = ArrayList<String>()
        for (i in 1..12) itemsMonth.add(String.format("%02d", i))

        val itemsYear = ArrayList<String>()
        for (i in 1900..2030) itemsYear.add(i.toString())

        // Creo gli adapter per i spinner
        val adapterDay = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsDay)
        val adapterMonth = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsMonth)
        val adapterYear = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsYear)

        // Imposto la visualizzazione degli item nel dropdown dei spinner
        adapterDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Assegno gli adapter ai spinner
        spinnerDay.adapter = adapterDay
        spinnerMonth.adapter = adapterMonth
        spinnerYear.adapter = adapterYear

        // Imposto il listener per il pulsante di registrazione
        spinnerDay.setSelection(0)
        spinnerMonth.setSelection(0)
        spinnerYear.setSelection(100)

        reg.setOnClickListener {
            // Aggiungo un'animazione al pulsante
            val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
            reg.startAnimation(animation)

            // Recupero i valori dagli input dell'utente
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

            // Validazione dell'input dell'utente
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

            // Procedo con la registrazione se l'input è valido
            if(!block) {
                runBlocking {
                    exist = existsInDB("Utenti", email)
                }
                if(exist)
                {
                    // Mostro un messaggio di errore se l'email è già registrata
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Registrazione Fallita")
                    builder.setMessage("La tua registrazione non è avvenuta con successo, potrebbe esistere già un account con questa mail, in caso Accedi!")
                    builder.setPositiveButton("OK",null)
                    val dialog = builder.create()
                    dialog.show()
                }
                else{
                    // Creo un documento utente nel database Firestore
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
                        // Invio un'email di conferma registrazione
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
                        // Mostro un messaggio di successo
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Registrazione Avvenuta")
                        builder.setMessage("La tua registrazione è avvenuta con successo!\nControlla la mail per ottenere la password.")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }.addOnFailureListener{
                        // Mostro un messaggio di errore in caso di fallimento
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
}