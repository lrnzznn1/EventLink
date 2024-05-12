package com.example.eventlink.Pages

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
        val email = intent.getStringExtra("email")
        val parent = this.findViewById<LinearLayout>(R.id.parente_nascosto)
        runBlocking {
            setPre(email, this@PaginaProfilo, parent)
        }
    }
    @SuppressLint("SetTextI18n", "InflateParams")
    suspend fun setPre(email: String?, context: Context, parent: LinearLayout){
        val events = db.collection("Prenotazioni").whereEqualTo("ID_Utente", email).get().await()
        for(document in events ){
            val eventId = document.data["ID_Evento"]
            val event = db.collection("Eventi").document(eventId!!.toString()).get().await()
            val image = event.data?.get("Immagine")
            val title = event.data?.get("Titolo")
            val time = event.data?.get("Ora")
            val date = event.data?.get("Data")
            val inflater = LayoutInflater.from(context)
            val duplicateView = inflater.inflate(R.layout.baseeventi, null)
            val text = duplicateView.findViewById<TextView>(R.id.pUtente_DescrizioneEvento)
            text.text = "$title\n\n$date $time"
            val img = duplicateView.findViewById<ImageView>(R.id.immagine_Evento)
            Glide.with(context).load(image).into(img)
            img.contentDescription = "Image"
            parent.addView(duplicateView)
        }
    }

}