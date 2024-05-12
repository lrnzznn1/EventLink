package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.eventlink.R
import com.example.eventlink.db

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

        // Retrieve event details from Firestore based on the marker ID
        db.collection("Eventi")
            .get()
            .addOnSuccessListener { result ->
                // Find the document associated with the marker ID
                val document = result.documents.find { it.id == markerId }
                if (document != null) {
                    // Load event image using Glide library
                    val urlImmagine = document.data?.getValue("Immagine").toString()
                    try {
                        Glide.with(this@PaginaEvento).load(urlImmagine).into(srcImage)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Set text for title, info, and description views
                    titleView.text= document.data?.getValue("Titolo").toString()
                    infoView.text= "Indirizzo: ${ document.data?.getValue("Indirizzo").toString() }\n" +
                            "Quando: ${document.data?.getValue("Data").toString()}" +
                            " ore ${document.data?.getValue("Ora").toString()}\n" +
                            "Prezzo: ${document.data?.getValue("Prezzo").toString()}"
                    descView.text= document.data?.getValue("Descrizione").toString()
                }
            }
            .addOnFailureListener {}
    }
}