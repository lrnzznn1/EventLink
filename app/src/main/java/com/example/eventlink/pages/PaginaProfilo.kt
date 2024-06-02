package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.eventlink.R
import com.example.eventlink.db
import com.example.eventlink.global_email
import com.example.eventlink.lista
import com.example.eventlink.other.hashString
import com.example.eventlink.other.rawJSON
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.processNextEventInCurrentThread
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class PaginaProfilo : Activity(){
    @SuppressLint("InflateParams")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profilo)


        val bottoneprenotazioni = findViewById<ImageView>(R.id.bottone_prenotazioni)
        val bottoneimpostazioni = findViewById<ImageView>(R.id.bottone_setting)
        val testoprenotazioni = findViewById<TextView>(R.id.testo_prenotazioni)
        val testoimpostazioni = findViewById<TextView>(R.id.testo_setting)

        val linearprenotazioni = findViewById<LinearLayout>(R.id.linear_prenotazioni)
        val linearimpostazioni = findViewById<LinearLayout>(R.id.linear_setting)


        val baseColor = ContextCompat.getColor(this, R.color.arancio)
        val alphaValue = 200 // Alpha di 0.925
        val colorWithAlpha = (alphaValue shl 24) or (baseColor and 0x00FFFFFF)
        val baseColorB = ContextCompat.getColor(this, R.color.grigio)

        val colorFilter = PorterDuffColorFilter(colorWithAlpha, PorterDuff.Mode.SRC_IN)
        val colorFilterB = PorterDuffColorFilter(baseColorB, PorterDuff.Mode.SRC_IN)


        bottoneprenotazioni.colorFilter = colorFilter
        bottoneimpostazioni.colorFilter = colorFilterB

        testoprenotazioni.setTextColor(colorWithAlpha)
        testoimpostazioni.setTextColor(baseColorB)

        val granderelativo = findViewById<RelativeLayout>(R.id.granderelativo1)
        val prenotazioniview = findViewById<FrameLayout>(R.id.prenotazioniview)
        var newview  : View = layoutInflater.inflate(R.layout.setting, null)

        linearprenotazioni.setOnClickListener {
            bottoneprenotazioni.colorFilter = colorFilter
            bottoneimpostazioni.colorFilter = colorFilterB

            testoprenotazioni.setTextColor(colorWithAlpha)
            testoimpostazioni.setTextColor(baseColorB)

            granderelativo.removeView(newview)
            prenotazioniview.visibility = View.VISIBLE


        }
        linearimpostazioni.setOnClickListener {

            bottoneprenotazioni.colorFilter = colorFilterB
            bottoneimpostazioni.colorFilter = colorFilter

            testoprenotazioni.setTextColor(baseColorB)
            testoimpostazioni.setTextColor(colorWithAlpha)
            prenotazioniview.visibility = View.GONE
            granderelativo.addView(newview)

            val newPass = findViewById<Button>(R.id.cambiaPassword)
            val password = findViewById<EditText>(R.id.NewPassword)
            val oldpass = findViewById<EditText>(R.id.oldPassword)
            val delAcc = findViewById<Button>(R.id.EliminaAccountBtn)
            val delText = findViewById<EditText>(R.id.delAcc)
            val logout = findViewById<Button>(R.id.LogOutBtn)

            delAcc.setOnClickListener{
                var piena = true
                var scelta = true
                val text = delText.text.toString()
                if(text==""){
                    piena = false
                    val builder = AlertDialog.Builder(this@PaginaProfilo)
                    builder.setTitle("Campo Vuoto")
                    builder.setMessage("Campo password vuoto!")
                    builder.setPositiveButton("OK") { _, _ ->
                    }
                    val dialog = builder.create()
                    dialog.show()
                }

                if(piena){
                    runBlocking {
                        val query = db.collection("Utenti")
                            .whereEqualTo(FieldPath.documentId(), global_email).get().await()
                        for(utenti in query){
                            if(utenti.data.get("Password")== hashString(text)){
                                val builder = AlertDialog.Builder(this@PaginaProfilo)
                                builder.setTitle("Message")
                                builder.setMessage("Sicuro di voler eliminare l'account?\nL'azione è irriversibile")
                                builder.setPositiveButton("Si") { _, _ ->
                                    db.collection("Utenti").document(utenti.id).delete()
                                    val builder = AlertDialog.Builder(this@PaginaProfilo)
                                    builder.setTitle("Eliminazione Account")
                                    builder.setMessage("Account eliminato.")
                                    builder.setPositiveButton("OK") { _, _ ->
                                        finish()
                                        global_email=""
                                    }
                                    val dialog = builder.create()
                                    dialog.show()
                                }
                                builder.setNegativeButton("No"){_, _ ->
                                }
                                val dialog = builder.create()
                                dialog.show()
                            }
                            else{
                                val builder = AlertDialog.Builder(this@PaginaProfilo)
                                builder.setTitle("Errore")
                                builder.setMessage("Password errata!")
                                builder.setPositiveButton("OK") { _, _ ->
                                }
                                val dialog = builder.create()
                                dialog.show()
                            }
                        }
                    }
                }
            }


            newPass.setOnClickListener{
                val oldText = oldpass.text.toString()
                var vecchia = true
                var nuova = true
                if(oldText==""){
                    vecchia = false
                    val builder = AlertDialog.Builder(this@PaginaProfilo)
                    builder.setTitle("Errore")
                    builder.setMessage("Campo vecchia password vuoto!")
                    builder.setPositiveButton("OK") { _, _ ->
                    }
                    val dialog = builder.create()
                    dialog.show()
                }

                val passText = password.text.toString()
                if(vecchia&&passText=="")
                {
                    nuova=false
                    val builder = AlertDialog.Builder(this@PaginaProfilo)
                    builder.setTitle("Errore")
                    builder.setMessage("Campo nuova password vuoto!")
                    builder.setPositiveButton("OK") { _, _ ->
                    }
                    val dialog = builder.create()
                    dialog.show()
                }

                if(nuova&&vecchia){
                    val pcrypt = hashString(passText)
                    runBlocking {
                        val query = db.collection("Utenti").whereEqualTo(FieldPath.documentId(), global_email).get().await()
                        for(utenti in query){
                            val utente = db.collection("Utenti").document(utenti.id).get().await()
                            if(hashString(oldText)==utente.data?.get("Password")){
                                db.collection("Utenti").document(utenti.id).update(
                                    mapOf(
                                        "Password" to pcrypt
                                    )
                                ).addOnSuccessListener {
                                    val builder = AlertDialog.Builder(this@PaginaProfilo)
                                    builder.setTitle("Cambio password")
                                    builder.setMessage("Il cambio password è avvenuto con successo!\nTi arriverà una mail di conferma con le nuove credenziali.")
                                    builder.setPositiveButton("OK") { _, _ ->
                                        global_email=""
                                        finish()
                                    }
                                    val dialog = builder.create()
                                    dialog.show()
                                    rawJSON(
                                        global_email,"Cambio password EventLink", "" +
                                                "Gentile Cliente, \n\n" +
                                                "Abbiamo ricevuto la sua richiesta di cambio password. \n\n" +
                                                "Di seguito troverà le nuove credenziali necessarie per accedere al suo account:\n\n" +
                                                "Email: $global_email \n" +
                                                "Password: $passText \n\n" +
                                                "Grazie per continuare ad usare il nostro servizio. Per qualsiasi domanda o assistenza, non esiti a contattarci. \n\n" +
                                                "Cordiali saluti, \n\n" +
                                                "EventLink"
                                    )
                                }
                            }else{
                                val builder = AlertDialog.Builder(this@PaginaProfilo)
                                builder.setTitle("Errore")
                                builder.setMessage("Password vecchia errata!")
                                builder.setPositiveButton("OK") { _, _ ->
                                }
                                val dialog = builder.create()
                                dialog.show()
                            }
                        }
                    }
                }
            }

            logout.setOnClickListener{
                val builder = AlertDialog.Builder(this@PaginaProfilo)
                builder.setTitle("Message")
                builder.setMessage("Sicuro di voler uscire?")
                builder.setPositiveButton("Si") { _, _ ->
                    global_email=""
                    finish()
                }
                builder.setNegativeButton("No"){_, _ ->
                }
                val dialog = builder.create()
                dialog.show()
            }
        }



        // Retrieve the email from the intent
        val email = intent.getStringExtra("email")

        // Find the parent LinearLayout
        val parent = this.findViewById<LinearLayout>(R.id.parente_nascosto)

        // Run the suspended function to set up the profile asynchronously
        runBlocking {
            setPre(email, this@PaginaProfilo, parent)
        }
    }

    //Sets up the user profile asynchronously.
    @SuppressLint("SetTextI18n", "InflateParams", "MissingInflatedId")
    suspend fun setPre(email: String?, context: Context, parent: LinearLayout){
        // Retrieve the events associated with the user from the database
        val events = db.collection("Prenotazioni").whereEqualTo("ID_Utente", email).get().await()

        // Iterate over each event document
        for(document in events ){
            // Retrieve event details from the database
            val eventId = document.data["ID_Evento"]
            val event = lista.find{it.ID_Evento==eventId}
            if(event!=null){
                val image = event.Immagine
                val title = event.Titolo
                val time = event.Ora
                val date = event.Data

                // Inflate the base event layout
                val inflater = LayoutInflater.from(context)
                val duplicateView = inflater.inflate(R.layout.baseeventi, null)

                // Set the event details in the duplicate view
                val text = duplicateView.findViewById<TextView>(R.id.pUtente_DescrizioneEvento)
                text.text = "$title\n\n$date $time"
                val img = duplicateView.findViewById<ImageView>(R.id.immagine_Evento)
                Glide.with(context).load(image).into(img)
                img.contentDescription = "Image"

                val annulla = duplicateView.findViewById<Button>(R.id.BottoneAnnulla)
                val finestra = duplicateView.findViewById<LinearLayout>(R.id.FinestraEvento)

                finestra.setOnClickListener{
                    val intent = Intent(this@PaginaProfilo, PaginaEvento::class.java)
                    intent.putExtra("markerId", event.ID_Evento)
                    startActivity(intent)
                }

                annulla.setOnClickListener{
                    runBlocking {
                        val document = db.collection("Prenotazioni").whereEqualTo("ID_Utente", global_email).whereEqualTo("ID_Evento", event.ID_Evento).get().await()
                        for(eventi in document){
                            db.collection("Prenotazioni").document(eventi.id).delete()
                        }
                        event.Max_Prenotazioni = (event.Max_Prenotazioni.toInt() +1).toString()
                        val eventplusone = db.collection("Eventi").document(event.ID_Evento)
                        eventplusone.update(
                            mapOf(
                                "Max_Prenotazioni" to event.Max_Prenotazioni

                            )
                        )
                        finish()
                        val intent = Intent(this@PaginaProfilo, PaginaProfilo::class.java)
                        intent.putExtra("email", global_email)
                        startActivity(intent)
                        overridePendingTransition(0, 0) // Rimuove l'animazione

                    }
                }
                // Add the duplicate view to the parent layout
                parent.addView(duplicateView)
            }
        }
    }
}