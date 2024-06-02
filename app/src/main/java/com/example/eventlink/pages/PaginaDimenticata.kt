package com.example.eventlink.pages

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import com.example.eventlink.R
import com.example.eventlink.db
import com.example.eventlink.other.existsInDB
import com.example.eventlink.other.generateRandomPassword
import com.example.eventlink.other.hashString
import com.example.eventlink.other.rawJSON
import kotlinx.coroutines.runBlocking

class PaginaDimenticata : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dimenticata)

        // Recupero il campo email e il pulsante dal layout
        val emailField = findViewById<EditText>(R.id.editTextEmailDimenticata)
        val btnInvia = findViewById<Button>(R.id.buttonInviaPassword)

        // Carico l'animazione per il pulsante
        val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

        // Imposto il click listener per il pulsante
        btnInvia.setOnClickListener {
            btnInvia.startAnimation(animation) // Avvio l'animazione

            val email = emailField.text.toString() // Recupero l'email inserita

            // Controllo se il campo email è vuoto
            if (email.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Email non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            }else{
                var esiste : Boolean
                // Verifico se l'email esiste nel database
                runBlocking {
                    esiste = existsInDB("Utenti", email)
                }
                if(esiste){
                    // Genero una nuova password
                    val password = generateRandomPassword()
                    val utentedacambiare = db.collection("Utenti").document(email)

                    // Aggiorno la password nel database
                    utentedacambiare.update(
                        mapOf(
                            "Password" to hashString(password)
                        )
                    ).addOnSuccessListener{
                        // Preparo l'email con la nuova password
                        val subject = "Ripristino della Password - Nuove Credenziali"
                        val body = """
                            Gentile Utente,
                            
                            Abbiamo ricevuto una richiesta di ripristino della password per il tuo account.
                            
                            Di seguito trovi le tue nuove credenziali di accesso:
                            
                            Email: $email
                            Password: $password
                                                        
                            Se non hai richiesto un ripristino della password, per favore contattaci immediatamente.
                            
                            Cordiali Saluti,
                            EventLink
                        """.trimIndent()

                        // Invio l'email
                        rawJSON(email, subject, body)

                        // Mostro un dialog di successo
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Password Cambiata")
                        builder.setMessage("Ti è stata inviata la nuova password tramite Email.")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                }else{
                    // Mostro un messaggio di errore se l'email non esiste
                    AlertDialog.Builder(this)
                        .setTitle("Email non valida")
                        .setMessage("Questa email non è presente nella piattaforma.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}