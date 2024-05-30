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

        // Initialize UI elements
        val emailField = findViewById<EditText>(R.id.editTextEmailLogin)
        val passwordField = findViewById<EditText>(R.id.editTextPasswordLogin)
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
                    auth=true
                    global_email=email
                    startActivity(intent)
                    finish()
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