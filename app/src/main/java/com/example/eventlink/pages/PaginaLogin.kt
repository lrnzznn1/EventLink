package com.example.eventlink.pages

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.eventlink.R
import com.example.eventlink.db
import com.example.eventlink.global_email
import com.example.eventlink.other.hashString
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class PaginaLogin : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Recupero i riferimenti agli elementi UI
        val emailField = findViewById<EditText>(R.id.editTextEmailLogin)
        val passwordField = findViewById<EditText>(R.id.editTextPasswordLogin)
        val buttonSignUp = findViewById<TextView>(R.id.registatiTesto)
        val buttonLogin = findViewById<Button>(R.id.buttonlogin)
        val buttonDimenticata = findViewById<TextView>(R.id.dimenticataTesto)

        // Gestisco il click sul testo "Registrati"
        buttonSignUp.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaSignIn::class.java)
            startActivity(intent)
        }

        // Gestisco il click sul testo "Password dimenticata"
        buttonDimenticata.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaDimenticata::class.java)
            startActivity(intent)
        }

        // Gestisco il click sul pulsante "Login"
        buttonLogin.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
            buttonLogin.startAnimation(animation)

            // Recupero i valori degli input email e password
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            var auth: Boolean
            auth = false
            var block = false

            // Controllo se i campi email e password sono vuoti e mostro un alert se necessario
            if (email.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Email non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            } else if (password.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Password non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            }

            // Se i campi non sono vuoti, controllo le credenziali
            if(!block){
                runBlocking {
                    auth = passwordCheck(email, password)
                }

                // Se le credenziali sono corrette, apro la PaginaProfilo
                if(auth) {
                    val intent = Intent(this@PaginaLogin, PaginaProfilo::class.java)
                    intent.putExtra("email", email)
                    auth=true
                    global_email=email
                    startActivity(intent)
                    finish()
                }
                else {
                    // Se le credenziali sono errate, mostro un alert
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Accesso Negato")
                    builder.setMessage("Email o Password errati!")
                    builder.setPositiveButton("OK", null)
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }
    }

    // Funzione per verificare le credenziali
    private suspend fun passwordCheck(documentId: String, password: String): Boolean {
        return try {
            // Recupero il documento dell'utente dal database
            val documentSnapshot = db.collection("Utenti").document(documentId).get().await()
            val documentData = documentSnapshot.data
            val documentPassword = documentData?.get("Password")
            // Controllo se la password hashata corrisponde a quella memorizzata nel database
            documentPassword == hashString(password)
        }catch (e: Exception){
            false
        }
    }

}