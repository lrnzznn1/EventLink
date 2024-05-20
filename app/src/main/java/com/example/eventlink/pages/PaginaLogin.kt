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
import com.example.eventlink.other.hashString
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class PaginaLogin : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Initialize UI elements
        val emailField = findViewById<EditText>(R.id.editTextEmailLogin)
        val passwordField = findViewById<EditText>(R.id.editTextPasswordLogin)
        /*
        val settingsViewLog = findViewById<LinearLayout>(R.id.ImpostazioniLoginComparsa)
        val showHideSettingsLog = findViewById<ImageButton>(R.id.button_menu_log)
        val backButton = findViewById<ImageButton>(R.id.tornaInDietroLog)
        val settingsButtonLog = findViewById<Button>(R.id.impostazioniLogin)
        val contactsButtonLog = findViewById<Button>(R.id.contattiLogin)
        val helpButtonLog = findViewById<Button>(R.id.aiutoLogin)

         */
        val buttonSignUp = findViewById<TextView>(R.id.registatiTesto)
        val buttonLogin = findViewById<Button>(R.id.buttonlogin)

        // Set click listeners
        buttonSignUp.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaSignIn::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            // Perform login operation
            val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
            buttonLogin.startAnimation(animation)
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            var auth: Boolean
            auth = false
            var block = false

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

            if(!block){
                runBlocking {
                    auth = passwordCheck(email, password)
                }

                if(auth) {
                    val intent = Intent(this@PaginaLogin, PaginaProfilo::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)

                }
                else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Accesso Negato")
                    builder.setMessage("Email o Password errati!")
                    builder.setPositiveButton("OK", null)
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }
        /*

        // Set visibility of settings view
        settingsViewLog.visibility = View.GONE

        // Set click listener for showing/hiding settings
        showHideSettingsLog.setOnClickListener {
            if (settingsViewLog.visibility == View.VISIBLE) {
                settingsViewLog.visibility = View.GONE
            } else {
                settingsViewLog.visibility = View.VISIBLE
            }
        }

        // Set click listener for back button
        backButton.setOnClickListener{
            finish()
        }

        // Set click listeners for settings, contacts, and help buttons
        settingsButtonLog.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaImpostazioni::class.java)
            startActivity(intent)
        }
        contactsButtonLog.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaContatti::class.java)
            startActivity(intent)
        }
        helpButtonLog.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaAiuto::class.java)
            startActivity(intent)
        }

         */
    }

    //Suspended function to check if the provided password matches the one stored in the database for the given document ID.
    private suspend fun passwordCheck(documentId: String, password: String): Boolean {
        return try {
            val documentSnapshot = db.collection("Utenti").document(documentId).get().await()
            val documentData = documentSnapshot.data
            val documentPassword = documentData?.get("Password")
            documentPassword == hashString(password)
        }catch (e: Exception){
            false
        }
    }
}