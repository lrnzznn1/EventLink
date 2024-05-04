    package com.example.eventlink

    import android.annotation.SuppressLint
    import android.app.Activity
    import android.app.AlertDialog
    import android.content.ContentValues.TAG
    import android.content.Intent
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.location.Geocoder
    import android.net.Uri
    import android.os.Bundle
    import android.util.Log
    import android.view.Gravity
    import android.widget.Button
    import android.widget.ImageView
    import android.widget.TextView
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.MapFragment
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.model.BitmapDescriptorFactory
    import com.google.android.gms.maps.model.LatLng
    import com.google.android.gms.maps.model.Marker
    import com.google.android.gms.maps.model.MarkerOptions
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase
    // Importa le necessarie classi Firebase Authentication
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.FirebaseUser

    val db = Firebase.firestore
    val mAuth = FirebaseAuth.getInstance()
    class MainActivity : Activity(), OnMapReadyCallback {

        private val italia = LatLng(42.0, 11.53)

        private val zoomlvl = 6f


        private val coordinates = arrayOf(
            LatLng(41.9028, 12.4964), // Roma
            LatLng(45.4642, 9.1900),  // Milano
            LatLng(45.0703, 7.6869),  // Torino
            LatLng(45.4384, 10.9916), // Verona
            LatLng(43.7696, 11.2558), // Firenze
            LatLng(40.8522, 14.2681), // Napoli
            LatLng(45.4386, 12.3266), // Venezia
            LatLng(41.1171, 16.8719), // Bari
            LatLng(37.5079, 15.0830)  // Catania
        )


        //Funzione Per creare la mappa da layout
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            var view = setContentView(R.layout.activity_main)
            val mapFragment: MapFragment? =
                fragmentManager.findFragmentById(R.id.map) as? MapFragment
            mapFragment?.getMapAsync(this)
        }

        //Appertura mappa
        @SuppressLint("PotentialBehaviorOverride")
        override fun onMapReady(googleMap: GoogleMap) {
            with(googleMap) {

                // Disabilita i pulsanti di navigazione
                uiSettings.isMapToolbarEnabled = false

                //Sposta Camera in centro italia
                moveCamera(CameraUpdateFactory.newLatLngZoom(italia,zoomlvl))

                // Carica l'immagine dalla risorsa
                val bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.logomontagna)
                // Ridimensiona l'immagine alle dimensioni desiderate
                val resizedBitmap1 = Bitmap.createScaledBitmap(bitmap1, 100, 100, false)

                val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.logomusica)
                val resizedBitmap2 = Bitmap.createScaledBitmap(bitmap2, 100 ,100, false)

                val bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.logosport)
                val resizedBitmap3 = Bitmap.createScaledBitmap(bitmap3, 100 ,100, false)



                // Aggiunge i marker sulla mappa
                for (i in coordinates.indices) {
                    val pos = coordinates[i]
                    val ico = when {
                        i < 3 -> resizedBitmap1
                        i < 6 -> resizedBitmap2
                        else -> resizedBitmap3
                    }
                    val markerOptions = MarkerOptions()
                        .position(pos)
                        .icon(BitmapDescriptorFactory.fromBitmap(ico))
                        .anchor(-0.1f, 1.0f)
                        .title("Test titolo")
                        .snippet("Test descrizione")
                    googleMap.addMarker(markerOptions)
                }



/*
                //Aggiungi un marker da indirizzo
                val geocoder1 = Geocoder(this@MainActivity)
                val address = "Piazza dei donatori di sangue 1 Isola Vicentina VI"
                val locations = geocoder1.getFromLocationName(address, 1)
                if (!locations.isNullOrEmpty()) {
                    //Database initialization
                    val db = Firebase.firestore
                    db.collection("Utenti")
                        .get()
                        .addOnSuccessListener { result ->
                            val firstLocation = locations[0]

                            for (document in result) {
                                val aaa = MarkerOptions()
                                    .position(LatLng(firstLocation.latitude, firstLocation.longitude))
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap1))
                                    .anchor(-0.1f, 1.0f)
                                    .title(document.id)
                                    .snippet("${document.data.getValue("Nome")}")
                                Log.d(TAG, "${document.id} => ${document.data}")
                                googleMap.addMarker(aaa)
                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents.", exception)
                        }


                }
*/


                val geocoder = Geocoder(this@MainActivity)
                db.collection("Eventi")
                    .get()
                    .addOnSuccessListener { result->
                        for(document in result){
                            val ico = document.data.getValue("Icona").toString()
                            val locations = geocoder.getFromLocationName(document.data.getValue("Indirizzo").toString(), 1)
                            val firstLocation = locations!![0]

                            val punto = MarkerOptions()
                                .position(LatLng(firstLocation.latitude, firstLocation.longitude))
                                .anchor(-0.1f, 1.0f)
                                .title(document.data.getValue("Titolo").toString())
                                .snippet(
                                    "Indirizzo: " + document.data.getValue("Indirizzo").toString() + "\n" +
                                            "Data: " + document.data.getValue("Data").toString() + "\n" +
                                            "Ora: " + document.data.getValue("Ora").toString() + "\n" +
                                            "Prezzo: " + document.data.getValue("Prezzo").toString()
                                )


                            when (ico) {
                                "1" -> punto.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap1))
                                "2" -> punto.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap2))
                                else -> punto.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap3))
                            }

                            var marker: Marker? =googleMap.addMarker(punto)
                            marker?.tag=document.id
                        }
                    }





                googleMap.setOnMarkerClickListener { marker ->


                    val TAG = marker.tag as? String

                    val view = layoutInflater.inflate(R.layout.indicatore_info_contents, null)

                    val titleTextView = view.findViewById<TextView>(R.id.title)
                    val snippetTextView = view.findViewById<TextView>(R.id.description)
                    val button1 = view.findViewById<Button>(R.id.button1)
                    val button2 = view.findViewById<Button>(R.id.button2)


                    titleTextView.text = marker.title
                    snippetTextView.text = marker.snippet

                    button1.setOnClickListener {
                        val intent = Intent(this@MainActivity, PaginaEvento::class.java)
                        intent.putExtra("markerId", TAG)
                        startActivity(intent)
                    }

                    button2.setOnClickListener {
                        val latitude = marker.position.latitude
                        val longitude = marker.position.longitude
                        val uri = "http://maps.google.com/maps?q=loc:$latitude,$longitude"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        startActivity(intent)
                    }

                    // Imposta la camera leggermente al di sotto del marker
                    val target = LatLng(marker.position.latitude - 0.001, marker.position.longitude)
                    val zoomLevel = 11f // Imposta lo zoom desiderato
                    val currentZoom = googleMap.cameraPosition.zoom
                    if (currentZoom < zoomLevel) {
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(target, zoomLevel)
                        googleMap.animateCamera(cameraUpdate, 1000, null)
                    } else {
                        val cameraUpdate = CameraUpdateFactory.newLatLng(target)
                        googleMap.animateCamera(cameraUpdate, 1000, null)
                    }


                    val dialog = AlertDialog.Builder(this@MainActivity, R.style.AlertDialogCustom)
                        .setView(view)
                        .create()

                    dialog.setOnShowListener {
                        val window = dialog.window
                        val params = window?.attributes

                        val gravity = Gravity.TOP
                        val xOffset = 0
                        val yOffset = 370 // Modifica il valore a seconda di quanto vuoi spostare l'AlertDialog

                        params?.gravity = gravity
                        params?.x = xOffset
                        params?.y = yOffset

                        window?.attributes = params
                    }

                    dialog.show()


                    true
                }

            }

        }

    }

    class PaginaEvento : Activity(){
        @SuppressLint("SetTextI18n")
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_evento)

            val markerId = intent.getStringExtra("markerId")
            db.collection("Eventi")
                .get()
                .addOnSuccessListener { result ->
                    val document = result.documents.find { it.id == markerId }
                    if (document != null) {
                        // Il documento esiste, esegui le azioni desiderate

                        val srcImage = findViewById<ImageView>(R.id.ImmagineEvento)
                        val titleView = findViewById<TextView>(R.id.TitoloEvento)
                        val infoView = findViewById<TextView>(R.id.InfoEvento)
                        val descView = findViewById<TextView>(R.id.DescrizioneEvento)

                        val nomeImmagine = document.data?.getValue("Immagine").toString()
                        val idRisorsa = resources.getIdentifier(nomeImmagine, "drawable", packageName)
                        srcImage.setImageResource(idRisorsa)
                        titleView.text= document.data?.getValue("Titolo").toString()
                        infoView.text= "Indirizzo: ${ document.data?.getValue("Indirizzo").toString() }\n" +
                                       "Quando: ${document.data?.getValue("Data").toString()}" +
                                       " ore ${document.data?.getValue("Ora").toString()}\n" +
                                       "Prezzo: ${document.data?.getValue("Prezzo").toString()}"
                        descView.text= document.data?.getValue("Descrizione").toString()


                    } else {
                        // Il documento non esiste, esegui un'altra azione o gestisci la mancanza del documento
                    }
                }
                .addOnFailureListener { exception ->
                    // Gestisci eventuali errori nel recupero dei dati dalla collezione "Eventi"
                }

        }
    }


/*
    // Inizializza l'istanza di FirebaseAuth


// Registra un nuovo utente con email e password
    mAuth.createUserWithEmailAndPassword(email, password)
    .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
            // Registrazione riuscita, l'utente è stato creato correttamente
            val user: FirebaseUser? = mAuth.currentUser
            // Puoi aggiungere qui la logica per reindirizzare l'utente alla schermata successiva
        } else {
            // Registrazione fallita
            // Puoi gestire gli errori qui
        }
    }
*/
