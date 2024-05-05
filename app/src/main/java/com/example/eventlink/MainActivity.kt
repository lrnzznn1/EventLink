/*
   Elenco dei tipi di eventi:
   - concerto
   - sport
   - gastronomia
   - convegno
   - mostra
   - spettacolo
   - outdoor
   - escursione
   - networking
   - educazione



    TODO:
        - Finire prima pagina con
            - menu da fare le sue cose con i bottoni
            - filtri implementare filtri e quando
            - rendere o più leggerla l'app o allungare schermata caricamento
        - Gestione Account + schermate

            // Registra un nuovo utente con email e password
            mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registrazione riuscita, l'utente è stato creato correttamente
                    val user: FirebaseUser? = mAuth.currentUser
                    // Puoi aggiungere qui la logica per reindirizzare l'utente alla schemata successiva
                } else {
                    // Registrazione fallita
                    // Puoi gestire gli errori qui
                }
            }


*/
@file:Suppress("DEPRECATION")

package com.example.eventlink

    import android.annotation.SuppressLint
    import android.app.Activity
    import android.app.AlertDialog
    import android.content.Intent
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.location.Geocoder
    import android.net.Uri
    import android.os.Bundle
    import android.view.Gravity
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.ImageButton
    import android.widget.ImageView
    import android.widget.LinearLayout
    import android.widget.Spinner
    import android.widget.TextView
    import com.bumptech.glide.Glide
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.MapFragment
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.model.BitmapDescriptorFactory
    import com.google.android.gms.maps.model.LatLng
    import com.google.android.gms.maps.model.Marker
    import com.google.android.gms.maps.model.MarkerOptions
    //import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase

    @SuppressLint("StaticFieldLeak")
    val db = Firebase.firestore
    //val mAuth = FirebaseAuth.getInstance()
    class MainActivity : Activity(), OnMapReadyCallback {

        // Coordinate della posizione centrale dell'Italia
        private val italia = LatLng(42.0, 11.53)

        // Livello di zoom predefinito per visualizzare l'intera Italia sulla mappa
        private val zoomlvl = 6f

        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Imposta il layout dell'attività
            setContentView(R.layout.activity_main)

            val impostazioniView = findViewById<LinearLayout>(R.id.Impostazioni)
            impostazioniView.visibility = View.GONE

            val filtriView = findViewById<LinearLayout>(R.id.filtrilayout)
            filtriView.visibility = View.GONE

            val zoomView = findViewById<LinearLayout>(R.id.zoomview)

            // Trova il frammento della mappa nel layout dell'attività
            val mapFragment: MapFragment? =
                fragmentManager.findFragmentById(R.id.map) as? MapFragment

            // Trova il frammento della mappa nel layout dell'attività
            mapFragment?.getMapAsync(this)

            val buttonMostraNascondiImpostazioni = findViewById<ImageButton>(R.id.button_menu)
            val buttonMostraFiltri = findViewById<Button>(R.id.filtri)
            val buttonNascondiFiltri = findViewById<Button>(R.id.chiudifiltri)

            buttonMostraNascondiImpostazioni.setOnClickListener {
                if (impostazioniView.visibility == View.VISIBLE) {
                    // Se le impostazioni sono già visibili, nasconderle
                    impostazioniView.visibility = View.GONE
                } else {
                    // Altrimenti, mostrale
                    impostazioniView.visibility = View.VISIBLE
                }
            }
            buttonMostraFiltri.setOnClickListener{
                buttonMostraFiltri.visibility= View.GONE
                filtriView.visibility = View.VISIBLE
                val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
                val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_120dp) // Sostituisci R.dimen.margin_top_show_filters con la tua dimensione desiderata
                layoutParams.topMargin = newMarginTop
                zoomView.layoutParams = layoutParams
            }
            buttonNascondiFiltri.setOnClickListener{
                filtriView.visibility = View.GONE
                buttonMostraFiltri.visibility= View.VISIBLE
                val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
                val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_400dp) // R.dimen.margin_top_400dp è una dimensione di 400dp definita nelle risorse
                layoutParams.topMargin = newMarginTop
                zoomView.layoutParams = layoutParams
            }

            // Array di stringhe con gli elementi da caricare sullo Spinner
            val items = arrayOf("Oggi", "Domani", "Weekend", "Questa settimana", "Prossima settimana", "Questo mese")

            // Creazione di un adapter per lo Spinner utilizzando l'array di stringhe
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

            // Imposta lo stile del dropdown
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Ottieni il riferimento al tuo Spinner utilizzando l'ID
            val spinnerDate = findViewById<Spinner>(R.id.date)

            // Imposta l'adapter sullo Spinner
            spinnerDate.adapter = adapter

        }


        @SuppressLint("PotentialBehaviorOverride", "DiscouragedApi")
        override fun onMapReady(googleMap: GoogleMap) {
            with(googleMap) {

                // Disabilita i pulsanti di navigazione sulla mappa
                uiSettings.isMapToolbarEnabled = false

                // Sposta la telecamera al centro dell'Italia con il livello di zoom predefinito
                moveCamera(CameraUpdateFactory.newLatLngZoom(italia,zoomlvl))

                // Crea un oggetto Geocoder per la geocodifica degli indirizzi
                val geocoder = Geocoder(this@MainActivity)

                // Ottiene la collezione "Eventi" dal database Firestore
                db.collection("Eventi")
                    .get()
                    .addOnSuccessListener { result->
                        // Loop attraverso ogni documento nell'insieme di risultati
                        for(document in result){
                            // Ottiene il tipo di evento dal documento
                            val ico = document.data.getValue("Tipo").toString()

                            // Ottiene l'ID della risorsa drawable utilizzando il suo nome
                            val resourceId = resources.getIdentifier(ico, "drawable", packageName)

                            // Carica la risorsa drawable come bitmap
                            val bitmap = BitmapFactory.decodeResource(resources, resourceId)

                            // Ridimensiona il bitmap
                            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100 ,100, false)


                            // Ottiene le informazioni sulla posizione dall'indirizzo nel documento
                            val locations = geocoder.getFromLocationName(document.data.getValue("Indirizzo").toString(), 1)
                            val firstLocation = locations!![0]


                            // Crea un oggetto MarkerOptions per impostare le proprietà del marker sulla mappa
                            val punto = MarkerOptions()
                                .position(LatLng(firstLocation.latitude, firstLocation.longitude)) // Imposta la posizione del marker
                                .anchor(-0.1f, 1.0f) // Imposta l'ancoraggio del marker
                                .title(document.data.getValue("Titolo").toString()) // Imposta il titolo del marker
                                .snippet( // Imposta il testo aggiuntivo del marker (informazioni sull'evento)
                                    "Indirizzo: " + document.data.getValue("Indirizzo").toString() + "\n" +
                                    "Data: " + document.data.getValue("Data").toString() + "\n" +
                                    "Ora: " + document.data.getValue("Ora").toString() + "\n" +
                                    "Prezzo: " + document.data.getValue("Prezzo").toString()
                                )

                            // Imposta l'icona personalizzata per il marker utilizzando il bitmap ridimensionato
                            punto.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))

                            // Aggiunge il marker alla mappa e memorizza il suo ID del documento come tag
                            val marker: Marker? = googleMap.addMarker(punto)
                            marker?.tag=document.id
                        }
                    }


                // Imposta un listener per i click sui marker sulla mappa
                googleMap.setOnMarkerClickListener { marker ->
                    // Ottiene l'ID del marker dal suo tag
                    val id = marker.tag as? String

                    // Infla la vista del layout personalizzato per il contenuto dell'indicatore
                    val view = layoutInflater.inflate(R.layout.indicatore_info_contents, null)

                    // Trova le viste all'interno della vista del layout personalizzato
                    val titleTextView = view.findViewById<TextView>(R.id.title)
                    val snippetTextView = view.findViewById<TextView>(R.id.description)
                    val button1 = view.findViewById<Button>(R.id.button1)
                    val button2 = view.findViewById<Button>(R.id.button2)

                    // Imposta il titolo e il testo aggiuntivo del marker nelle viste appropriate
                    titleTextView.text = marker.title
                    snippetTextView.text = marker.snippet

                    // Imposta il listener del bottone 1 per avviare l'attività PaginaEvento con l'ID del marker come extra
                    button1.setOnClickListener {
                        val intent = Intent(this@MainActivity, PaginaEvento::class.java)
                        intent.putExtra("markerId", id)
                        startActivity(intent)
                    }

                    // Imposta il listener del bottone 2 per aprire Google Maps con la posizione del marker
                    button2.setOnClickListener {
                        val latitude = marker.position.latitude
                        val longitude = marker.position.longitude
                        val uri = "http://maps.google.com/maps?q=loc:$latitude,$longitude"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        startActivity(intent)
                    }

                    // Sposta leggermente la camera al di sotto del marker
                    val target = LatLng(marker.position.latitude - 0.001, marker.position.longitude)
                    val zoomLevel = 11f // Imposta il livello di zoom desiderato
                    val currentZoom = googleMap.cameraPosition.zoom
                    // Controlla se il livello di zoom attuale è inferiore al livello di zoom desiderato
                    if (currentZoom < zoomLevel) {
                        // Se sì, crea un'animazione per spostare e zoomare la camera alla posizione desiderata
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(target, zoomLevel)
                        googleMap.animateCamera(cameraUpdate, 1000, null)
                    } else {
                        // Altrimenti, crea un'animazione per spostare la camera alla posizione desiderata mantenendo lo stesso livello di zoom
                        val cameraUpdate = CameraUpdateFactory.newLatLng(target)
                        googleMap.animateCamera(cameraUpdate, 1000, null)
                    }

                    // Crea e mostra un AlertDialog personalizzato con la vista del layout personalizzato
                    val dialog = AlertDialog.Builder(this@MainActivity, R.style.AlertDialogCustom)
                        .setView(view)
                        .create()

                    // Imposta un listener per mostrare l'AlertDialog personalizzato in una posizione specifica
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

                    dialog.show() // Mostra l'AlertDialog personalizzato


                    true// Restituisce true per indicare che l'evento di click sul marker è stato gestito
                }
                val zommpiu = findViewById<Button>(R.id.btp)
                val zommmeno = findViewById<Button>(R.id.btm)

                zommpiu.setOnClickListener {
                    // Incrementa il livello di zoom attuale di un valore desiderato
                    val currentZoomLevel = googleMap.cameraPosition.zoom
                    val newZoomLevel = currentZoomLevel + 1 // Modifica il valore di incremento del livello di zoom
                    val cameraUpdate = CameraUpdateFactory.zoomTo(newZoomLevel)
                    googleMap.animateCamera(cameraUpdate)
                }

                // Imposta un listener per il bottone di zoom out (-)
                zommmeno.setOnClickListener {
                    // Decrementa il livello di zoom attuale di un valore desiderato
                    val currentZoomLevel = googleMap.cameraPosition.zoom
                    val newZoomLevel = currentZoomLevel - 1 // Modifica il valore di decremento del livello di zoom
                    val cameraUpdate = CameraUpdateFactory.zoomTo(newZoomLevel)
                    googleMap.animateCamera(cameraUpdate)
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

                        val urlImmagine = document.data?.getValue("Immagine").toString()

                        try {
                            Glide.with(this@PaginaEvento).load(urlImmagine).into(srcImage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

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
                .addOnFailureListener {
                    // Gestisci eventuali errori nel recupero dei dati dalla collezione "Eventi"
                }

        }
    }