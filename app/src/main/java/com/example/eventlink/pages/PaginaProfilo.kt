package com.example.eventlink.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.eventlink.R
import com.example.eventlink.db
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