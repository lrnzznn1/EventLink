package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.eventlink.R
import com.example.eventlink.databaseLoc
import com.example.eventlink.db
import com.example.eventlink.global_email
import com.example.eventlink.other.EventoLocale
import com.example.eventlink.other.rawJSON
import kotlinx.coroutines.runBlocking
import com.example.eventlink.lista

class PaginaEvento : Activity(){
    override fun finish() {
        val returnintent : Intent? = null
        returnintent?.putExtra("result", 1)
        setResult(RESULT_OK, returnintent)
        super.finish()
    }
    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)
        val markerId = intent.getStringExtra("markerId")


        val btnPreferitiIndicatore = findViewById<ImageButton>(R.id.preferitievento)


        var bho : Boolean
        runBlocking {
            bho = markerId?.let { it1 -> databaseLoc.DAOEventoLocale().doesEventExist(it1) } == true
        }

        if (bho) btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_1002)


        btnPreferitiIndicatore.setOnClickListener{
            runBlocking {
                bho = markerId?.let { it1 -> databaseLoc.DAOEventoLocale().doesEventExist(it1) } == true
            }
            Log.d("BGO",bho.toString())
            if (!bho) {
                btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_1002)


                Log.d("BGO","if")
                runBlocking {
                    markerId?.let { it1 -> EventoLocale(it1) }
                        ?.let { it2 -> databaseLoc.DAOEventoLocale().insert(it2) }
                }


            } else {
                Log.d("BGO","else")
                btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_100)
                runBlocking {
                    markerId?.let { it1 -> EventoLocale(it1) }
                        ?.let { it2 -> databaseLoc.DAOEventoLocale().delete(it2) }
                }

            }
        }

        // Initialize UI elements
        val srcImage = findViewById<ImageView>(R.id.ImmagineEvento)
        val titleView = findViewById<TextView>(R.id.TitoloEvento)
        val infoView = findViewById<TextView>(R.id.InfoEvento)
        val descView = findViewById<TextView>(R.id.DescrizioneEvento)
        val btn = findViewById<Button>(R.id.PrenotaEvento1)
        var posti = 0
        // Retrieve event details from Firestore based on the marker ID
        val document = lista.find { it.ID_Evento == markerId }
        if (document != null) {
            posti = document.Max_Prenotazioni.toInt()
        // Load event image using Glide library
            val urlImmagine = document.Immagine
            try {
                Glide.with(this@PaginaEvento).load(urlImmagine).into(srcImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if(document.Prenotazione=="0") {
                btn.visibility= View.GONE
            }
            // Set text for title, info, and description views
            titleView.text = document.Titolo
            infoView.text = "Indirizzo: ${document.Indirizzo}\n" +
                                        "Quando: ${document.Data}" +
                                        " ore ${document.Ora}\n" +
                                        "Prezzo: ${document.Prezzo}"
            descView.text = document.Descrizione
        }
        btn.setOnClickListener {
            runBlocking {
                db.collection("Prenotazioni").get().addOnSuccessListener { result ->
                    val risultato = result.documents.find{it.data?.getValue("ID_Utente")== global_email&&it.data?.getValue("ID_Evento")==markerId}
                    if(global_email!=""&&posti>0&&risultato==null) {
                        val evento = lista.find{it.ID_Evento==markerId}
                        db.collection("Prenotazioni").add(
                            mapOf
                                (
                                "ID_Utente" to global_email,
                                "ID_Evento" to markerId
                            )
                        )
                        //Aggiornamento lista e db della prenotazione effettuata
                        if(evento!=null) evento.Max_Prenotazioni.toInt()-1
                        val eventminusone = db.collection("Eventi").document(markerId.toString())
                        eventminusone.update(
                            mapOf(
                                "Max_Prenotazioni" to (posti-1)

                            )
                        ).addOnSuccessListener {
                            if(evento!=null){
                                val nome = evento.Titolo
                                val data = evento.Data
                                val ora = evento.Ora
                                val descrizione = evento.Descrizione
                                val indirizzo = evento.Indirizzo
                                val prezzo = evento.Prezzo

                                val subject = "Conferma Prenotazione Evento: $nome"
                                val body = """
                                        Gentile Utente,
                                    
                                        Grazie per aver prenotato l'evento "$nome"!
                                    
                                        Di seguito i dettagli dell'evento:
                                        
                                        Data: $data
                                        Ora: $ora
                                        Descrizione: $descrizione
                                        Indirizzo: $indirizzo
                                        Prezzo: $prezzo
                                    
                                        Siamo lieti di averti con noi e ti aspettiamo all'evento.
                                    
                                        Cordiali Saluti,
                                        EventLink
                                    """.trimIndent()
                                rawJSON(global_email, subject, body)
                            }

                        }
                        val builder = AlertDialog.Builder(this@PaginaEvento)
                        builder.setTitle("Prenotazione Completata")
                        builder.setMessage("La tua prenotazione è stata completata con successo.\nControlla la tua email per ulteriori dettagli.")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    else if(posti==0) {
                        val builder = AlertDialog.Builder(this@PaginaEvento)
                        builder.setTitle("Posti Esauriti")
                        builder.setMessage("Ci dispiace!\nAl momento tutti i posti sono esauriti. Controlla più tardi, potrebbero liberarsi posti disponibili!")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    else if(global_email=="") {
                        val builder = AlertDialog.Builder(this@PaginaEvento)
                        builder.setTitle("Accesso Richiesto")
                        builder.setMessage("Devi eseguire l'accesso per poter effettuare una prenotazione.")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    else {
                        val builder = AlertDialog.Builder(this@PaginaEvento)
                        builder.setTitle("Prenotazione Duplicata")
                        builder.setMessage("Hai già prenotato questo evento. Non puoi prenotarlo più di una volta.")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }
    }
}