package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.eventlink.R
import com.example.eventlink.db
import com.example.eventlink.global_email
import com.example.eventlink.lista
import com.example.eventlink.other.hashString
import com.example.eventlink.other.rawJSON
import com.example.eventlink.posizioneData
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
class PaginaProfilo : Activity(){
    @SuppressLint("InflateParams")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profilo)

        // Recupero delle viste dall'interfaccia utente
        val bottoneprenotazioni = findViewById<ImageView>(R.id.bottone_prenotazioni)
        val bottoneimpostazioni = findViewById<ImageView>(R.id.bottone_setting)
        val testoprenotazioni = findViewById<TextView>(R.id.testo_prenotazioni)
        val testoimpostazioni = findViewById<TextView>(R.id.testo_setting)
        val linearprenotazioni = findViewById<LinearLayout>(R.id.linear_prenotazioni)
        val linearimpostazioni = findViewById<LinearLayout>(R.id.linear_setting)
        val granderelativo = findViewById<RelativeLayout>(R.id.granderelativo1)
        val prenotazioniview = findViewById<FrameLayout>(R.id.prenotazioniview)
        val newview  : View = layoutInflater.inflate(R.layout.setting, null)

        // Impostazione dei colori per gli elementi dell'interfaccia utente
        val baseColor = ContextCompat.getColor(this, R.color.arancio)
        val alphaValue = 200
        val colorWithAlpha = (alphaValue shl 24) or (baseColor and 0x00FFFFFF)
        val baseColorB = ContextCompat.getColor(this, R.color.grigio)
        val colorFilter = PorterDuffColorFilter(colorWithAlpha, PorterDuff.Mode.SRC_IN)
        val colorFilterB = PorterDuffColorFilter(baseColorB, PorterDuff.Mode.SRC_IN)
        bottoneprenotazioni.colorFilter = colorFilter
        bottoneimpostazioni.colorFilter = colorFilterB
        testoprenotazioni.setTextColor(colorWithAlpha)
        testoimpostazioni.setTextColor(baseColorB)


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

            // Recupero delle viste dall'interfaccia utente per la sezione delle impostazioni
            val newPass = findViewById<Button>(R.id.cambiaPassword)
            val password = findViewById<EditText>(R.id.NewPassword)
            val oldpass = findViewById<EditText>(R.id.oldPassword)
            val delAcc = findViewById<Button>(R.id.EliminaAccountBtn)
            val delText = findViewById<EditText>(R.id.delAcc)
            val logout = findViewById<Button>(R.id.LogOutBtn)

            // Impostazione del listener per il pulsante "Elimina Account"
            delAcc.setOnClickListener{
                var piena = true
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
                            if(utenti.data["Password"] == hashString(text)){
                                val builder = AlertDialog.Builder(this@PaginaProfilo)
                                builder.setTitle("Message")
                                builder.setMessage("Sicuro di voler eliminare l'account?\nL'azione è irriversibile")
                                builder.setPositiveButton("Si") { _, _ ->
                                    // Eliminazione dell'account dalla collezione Utenti
                                    db.collection("Utenti").document(utenti.id).delete()
                                    runBlocking {
                                        // Eliminazione di tutte le prenotazioni associate all'account
                                        val prenotazioni = db.collection("Prenotazioni").whereEqualTo("ID_Utente", utenti.id).get().await()
                                        for(pre in prenotazioni){
                                            db.collection("Prenotazioni").document(pre.id).delete()
                                        }
                                    }
                                    // Visualizzazione del messaggio di successo dopo l'eliminazione dell'account
                                    val builderd = AlertDialog.Builder(this@PaginaProfilo)
                                    builderd.setTitle("Eliminazione Account")
                                    builderd.setMessage("Account eliminato.")
                                    builderd.setPositiveButton("OK") { _, _ ->
                                        finish()
                                        global_email=""
                                    }
                                    val dialog = builderd.create()
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

            // Impostazione del listener per il pulsante "Cambia Password"
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
                if(vecchia&&passText=="") {
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
                    // Calcolo dell'hash della nuova password
                    val pcrypt = hashString(passText)
                    runBlocking {
                        // Recupero dell'utente corrente dal database
                        val query = db.collection("Utenti").whereEqualTo(FieldPath.documentId(), global_email).get().await()
                        for(utenti in query){
                            val utente = db.collection("Utenti").document(utenti.id).get().await()
                            // Verifica se la vecchia password è corretta
                            if(hashString(oldText)==utente.data?.get("Password")){
                                // Aggiornamento della password nel database
                                db.collection("Utenti").document(utenti.id).update(
                                    mapOf(
                                        "Password" to pcrypt
                                    )
                                ).addOnSuccessListener {
                                    // Visualizzazione di un messaggio di successo
                                    val builder = AlertDialog.Builder(this@PaginaProfilo)
                                    builder.setTitle("Cambio password")
                                    builder.setMessage("Il cambio password è avvenuto con successo!\nTi arriverà una mail di conferma con le nuove credenziali.")
                                    builder.setPositiveButton("OK") { _, _ ->
                                        global_email=""
                                        finish()
                                    }
                                    val dialog = builder.create()
                                    dialog.show()
                                    // Invio di una email di conferma del cambio password
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
                                // Visualizzazione di un messaggio di errore se la vecchia password è errata
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

            // Gestione dell'evento di clic sul pulsante "Logout"
            logout.setOnClickListener{
                val builder = AlertDialog.Builder(this@PaginaProfilo)
                builder.setTitle("Message")
                builder.setMessage("Sicuro di voler uscire?")
                builder.setPositiveButton("Si") { _, _ ->
                    // Effettua il logout, cancellando l'email globale e chiudendo l'activity
                    global_email=""
                    finish()
                }
                builder.setNegativeButton("No"){_, _ ->
                }
                val dialog = builder.create()
                dialog.show()
            }
        }



        val email = intent.getStringExtra("email")

        val parent = this.findViewById<LinearLayout>(R.id.parente_nascosto)

        runBlocking {
            setPre(email, this@PaginaProfilo, parent)
        }
    }

    // Impostazione dei dati relativi alle prenotazioni dell'utente nell'interfaccia grafica
    @SuppressLint("SetTextI18n", "InflateParams", "MissingInflatedId")
    suspend fun setPre(email: String?, context: Context, parent: LinearLayout){
        // Recupero delle prenotazioni dall'utente dal database
        val events = db.collection("Prenotazioni").whereEqualTo("ID_Utente", email).get().await()

        // Iterazione su tutte le prenotazioni dell'utente
        for(document in events ){
            val eventId = document.data["ID_Evento"]
            val event = lista.find{it.ID_Evento==eventId}
            if(event!=null){
                // Recupero delle informazioni sull'evento
                val image = event.Immagine
                val title = event.Titolo
                val time = event.Ora
                val date = event.Data

                // Creazione di una vista per l'evento
                val inflater = LayoutInflater.from(context)
                val duplicateView = inflater.inflate(R.layout.baseeventi, null)

                // Impostazione delle informazioni sull'evento nella vista
                val text = duplicateView.findViewById<TextView>(R.id.pUtente_DescrizioneEvento)
                text.text = "$title\n\n$date $time"
                val img = duplicateView.findViewById<ImageView>(R.id.immagine_Evento)
                Glide.with(context).load(image).transform(RoundedCorners(16)).into(img)
                img.contentDescription = "Image"

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_top_16dp) // Ad esempio, 16dp di margine inferiore
                duplicateView.layoutParams = layoutParams

                // Impostazione dei listener per la visualizzazione dettagli evento e per l'annullamento della prenotazione
                val annulla = duplicateView.findViewById<Button>(R.id.BottoneAnnulla)
                val finestra = duplicateView.findViewById<LinearLayout>(R.id.FinestraEvento)

                finestra.setOnClickListener{
                    val intent = Intent(this@PaginaProfilo, PaginaEvento::class.java)
                    intent.putExtra("markerId", event.ID_Evento)
                    startActivity(intent)
                }

                annulla.setOnClickListener{
                    val builder = AlertDialog.Builder(this@PaginaProfilo)
                    builder.setTitle("Message")
                    builder.setMessage("Sicuro di voler annullare l'evento?\nL'azione è irriversibile")
                    builder.setPositiveButton("Si") { _, _ ->
                        runBlocking {
                            val documentz = db.collection("Prenotazioni")
                                .whereEqualTo("ID_Utente", global_email)
                                .whereEqualTo("ID_Evento", event.ID_Evento).get().await()
                            for (eventi in documentz) {
                                db.collection("Prenotazioni").document(eventi.id).delete()
                            }
                            event.Max_Prenotazioni = (event.Max_Prenotazioni.toInt() + 1).toString()
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
                            overridePendingTransition(0, 0)
                        }
                    }
                    builder.setNegativeButton("No"){_,_ ->

                    }
                    val dialog = builder.create()
                    dialog.show()
                }
                // Aggiunta della vista dell'evento al layout principale
                parent.addView(duplicateView)
            }
        }
    }
}