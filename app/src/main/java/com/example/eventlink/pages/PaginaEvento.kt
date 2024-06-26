package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
        // Sovrascrivo il metodo finish() per restituire un risultato
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)

        // Recupero l'ID del marker dall'intent
        val markerId = intent.getStringExtra("markerId")

        // Recupero il pulsante per i preferiti
        val btnPreferitiIndicatore = findViewById<ImageButton>(R.id.preferitievento)

        // Verifico se l'evento è già nei preferiti
        var bho : Boolean
        runBlocking {
            bho = markerId?.let { it1 -> databaseLoc.DAOEventoLocale().doesEventExist(it1) } == true
        }

        // Imposto l'icona dei preferiti se l'evento è già nei preferiti
        if (bho) btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_1002)

        // Gestisco il click sul pulsante dei preferiti
        btnPreferitiIndicatore.setOnClickListener{
            runBlocking {
                bho = markerId?.let { it1 -> databaseLoc.DAOEventoLocale().doesEventExist(it1) } == true
            }
            if (!bho) {
                btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_1002)
                runBlocking {
                    markerId?.let { it1 -> EventoLocale(it1) }
                        ?.let { it2 -> databaseLoc.DAOEventoLocale().insert(it2) }
                }
            } else {
                btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_100)
                runBlocking {
                    markerId?.let { it1 -> EventoLocale(it1) }
                        ?.let { it2 -> databaseLoc.DAOEventoLocale().delete(it2) }
                }

            }
        }

        // Recupero gli elementi della UI
        val srcImage = findViewById<ImageView>(R.id.ImmagineEvento)
        val titleView = findViewById<TextView>(R.id.TitoloEvento)
        val infoView = findViewById<TextView>(R.id.InfoEvento)
        val descView = findViewById<TextView>(R.id.DescrizioneEvento)
        val btn = findViewById<Button>(R.id.PrenotaEvento1)
        val linearbtn = findViewById<LinearLayout>(R.id.linearPrenotaEvento1)
        var posti = 0

        // Trovo l'evento nella lista
        val document = lista.find { it.ID_Evento == markerId }
        if (document != null) {
            posti = document.Max_Prenotazioni.toInt()

            val urlImmagine = document.Immagine
            try {
                // Carico l'immagine dell'evento
                Glide.with(this@PaginaEvento).load(urlImmagine).into(srcImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Nascondo il pulsante di prenotazione se non è richiesto
            if(document.Prenotazione=="0") {
                btn.visibility= View.GONE
                linearbtn.visibility = View.GONE
            }

            // Imposto i dettagli dell'evento nella UI
            titleView.text = document.Titolo
            infoView.text = "Indirizzo: ${document.Indirizzo}\n" +
                                        "Quando: ${document.Data}" +
                                        " ore ${document.Ora}\n" +
                                        "Prezzo: ${document.Prezzo}"
            descView.text = document.Descrizione
        }

        // Gestisco il click sul pulsante di prenotazione
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

                        if(evento!=null) evento.Max_Prenotazioni =(evento.Max_Prenotazioni.toInt()-1).toString()
                        posti -= 1
                        val eventminusone = db.collection("Eventi").document(markerId.toString())
                        eventminusone.update(
                            mapOf(
                                "Max_Prenotazioni" to (posti).toString()
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

                        // Mostro un dialogo di conferma prenotazione
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
                        // Mostro un dialogo di posti esauriti
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
                        // Mostro un dialogo di accesso richiesto
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
                        // Mostro un dialogo di prenotazione duplicata
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