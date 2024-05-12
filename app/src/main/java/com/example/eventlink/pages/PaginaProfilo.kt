package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.eventlink.R
import com.example.eventlink.db
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class PaginaProfilo : Activity(){
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profilo)

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
    @SuppressLint("SetTextI18n", "InflateParams")
    suspend fun setPre(email: String?, context: Context, parent: LinearLayout){
        // Retrieve the events associated with the user from the database
        val events = db.collection("Prenotazioni").whereEqualTo("ID_Utente", email).get().await()

        // Iterate over each event document
        for(document in events ){
            // Retrieve event details from the database
            val eventId = document.data["ID_Evento"]
            val event = db.collection("Eventi").document(eventId!!.toString()).get().await()
            val image = event.data?.get("Immagine")
            val title = event.data?.get("Titolo")
            val time = event.data?.get("Ora")
            val date = event.data?.get("Data")

            // Inflate the base event layout
            val inflater = LayoutInflater.from(context)
            val duplicateView = inflater.inflate(R.layout.baseeventi, null)

            // Set the event details in the duplicate view
            val text = duplicateView.findViewById<TextView>(R.id.pUtente_DescrizioneEvento)
            text.text = "$title\n\n$date $time"
            val img = duplicateView.findViewById<ImageView>(R.id.immagine_Evento)
            Glide.with(context).load(image).into(img)
            img.contentDescription = "Image"

            // Add the duplicate view to the parent layout
            parent.addView(duplicateView)
        }
    }

}