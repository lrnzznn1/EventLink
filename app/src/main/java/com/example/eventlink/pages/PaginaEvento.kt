package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.eventlink.R
import com.example.eventlink.db
import com.example.eventlink.global_email
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.runBlocking

class PaginaEvento : Activity(){
    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)

        // Get the marker ID passed from the Main Activity
        val markerId = intent.getStringExtra("markerId")

        // Initialize UI elements
        val srcImage = findViewById<ImageView>(R.id.ImmagineEvento)
        val titleView = findViewById<TextView>(R.id.TitoloEvento)
        val infoView = findViewById<TextView>(R.id.InfoEvento)
        val descView = findViewById<TextView>(R.id.DescrizioneEvento)
        val btn = findViewById<Button>(R.id.PrenotaEvento1)
        var posti : Int = 0
        // Retrieve event details from Firestore based on the marker ID
        runBlocking {
            db.collection("Eventi")
                .get()
                .addOnSuccessListener { result ->
                    // Find the document associated with the marker ID
                    val document = result.documents.find { it.id == markerId }
                    if (document != null) {
                        posti = document.data?.getValue("Max_Prenotazioni").toString().toInt()
                        // Load event image using Glide library
                        val urlImmagine = document.data?.getValue("Immagine").toString()
                        try {
                            Glide.with(this@PaginaEvento).load(urlImmagine).into(srcImage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if(document.data?.getValue("Prenotazione").toString()=="0") {
                            btn.visibility= View.GONE
                        }
                        // Set text for title, info, and description views
                        titleView.text = document.data?.getValue("Titolo").toString()
                        infoView.text =
                            "Indirizzo: ${document.data?.getValue("Indirizzo").toString()}\n" +
                                    "Quando: ${document.data?.getValue("Data").toString()}" +
                                    " ore ${document.data?.getValue("Ora").toString()}\n" +
                                    "Prezzo: ${document.data?.getValue("Prezzo").toString()}"
                        descView.text = document.data?.getValue("Descrizione").toString()
                    }
                }
                .addOnFailureListener {}
        }
        btn.setOnClickListener {
            Log.d("sborring", global_email)
            Log.d("cumming", posti.toString())
            runBlocking {
                db.collection("Prenotazioni").get().addOnSuccessListener { result ->
                    val risultato = result.documents.find{it.data?.getValue("ID_Utente")== global_email&&it.data?.getValue("ID_Evento")==markerId}
                    if(global_email!=""&&posti>0&&risultato==null) {
                        db.collection("Prenotazioni").add(
                            mapOf
                                (
                                "ID_Utente" to global_email,
                                "ID_Evento" to markerId
                            )
                        )
                        val documento = db.collection("Eventi").document(markerId.toString())
                        documento.update(
                            mapOf(
                                "Max_Prenotazioni" to (posti-1)
                            )
                        )

                    }
                    else if(posti==0) {
                        val builder = AlertDialog.Builder(this@PaginaEvento)
                        builder.setTitle("Posti Esauriti")
                        builder.setMessage("Ci dispiace!\nControlla successivamente, potrebbe liberarsi un posto!")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    else if(global_email=="") {
                        val builder = AlertDialog.Builder(this@PaginaEvento)
                        builder.setTitle("Accedi!")
                        builder.setMessage("Non puoi prenotare senza aver eseguito l'accesso!")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    else {
                        val builder = AlertDialog.Builder(this@PaginaEvento)
                        builder.setTitle("Hai già prenotato questo evento!")
                        builder.setMessage("Non puoi prenotare un evento più di una volta!")
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