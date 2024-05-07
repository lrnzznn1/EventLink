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
            - filtri implementare filtri e quando
            - rendere o più leggerla l'app o allungare schermata caricamento
        - Login al db su PaginLogin
        - Registrazione al db su PaginaSignIn
        - Sistemare xml delle date su PaginaSignIn Mi fa scifo
        - Sistemare i 3 bottoni su PaginaEvento + aggiungere al db Numero Partecipanti
        - Aggiungere bottone aggiungi a preferiti direttamente dal Marker Info + gestione
        - Fare PaginaProfilo tutto
        - Pagina Impostazioni tutto
        - Pagina Contatti tutto
        - Pagina Aiuto tutto
        - Aggiungere al db utenti, aziende, prenotazioni e preferiti
        - Variabile globale con id utente se 0 non loggato se n loggato
        - Spostare tutte le pagine in altri file




        CODICE UTILE + o -
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



//funzione che ci permette di inviare mail con connessione cryptata con STRATTLS
fun inviaEmail(destinatario: String, oggetto: String, testo: String) {
    // Configurazione delle proprietà per la connessione al server SMTP
    val props = Properties()
    props["mail.smtp.host"] = "smtp.gmail.com" // Indirizzo del server SMTP
    props["mail.smtp.port"] = "587" // Porta del server SMTP
    props["mail.smtp.auth"] = "true" // Abilita l'autenticazione SMTP
    props["mail.smtp.starttls.enable"] = "true" // Abilita STARTTLS per la crittografia

    // Autenticazione al server SMTP
    val autenticazione = object : Authenticator() {
        val pw = Properties().apply {
            load(FileInputStream(File("local.properties")))
        }.getProperty("PASSWORD")
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication("EventLinkAuth@gmail.com", pw)
        }
    }

    // Creazione della sessione
    val session = Session.getInstance(props, autenticazione)

    try {
        // Creazione del messaggio
        val message = MimeMessage(session)
        message.setFrom(InternetAddress("EventLinkAuth@gmail.com"))
        message.addRecipient(Message.RecipientType.TO, InternetAddress(destinatario))
        message.subject = oggetto
        message.setText(testo)

        // Invio dell'email
        Transport.send(message)
        println("Email inviata con successo!")
    } catch (e: MessagingException) {
        println("Si è verificato un errore durante l'invio dell'email: ${e.message}")
    }
}

*/










@file:Suppress("DEPRECATION")

package com.example.eventlink

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore
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


            //Pulsante pagina login
            val accountPulsante = findViewById<ImageButton>(R.id.button_profile)

            accountPulsante.setOnClickListener{
                val intent = Intent(this@MainActivity, PaginaLogin::class.java)
                startActivity(intent)
            }

            val impostazionePulsante = findViewById<Button>(R.id.impostazioniMain)
            val contattiPulsante = findViewById<Button>(R.id.contattiMain)
            val aiutoPulsante = findViewById<Button>(R.id.aiutoMain)

            impostazionePulsante.setOnClickListener {
                val intent = Intent(this@MainActivity, PaginaImpostazioni::class.java)
                startActivity(intent)
            }
            contattiPulsante.setOnClickListener {
                val intent = Intent(this@MainActivity, PaginaContatti::class.java)
                startActivity(intent)
            }
            aiutoPulsante.setOnClickListener {
                val intent = Intent(this@MainActivity, PaginaAiuto::class.java)
                startActivity(intent)
            }


        }


        @SuppressLint("PotentialBehaviorOverride", "DiscouragedApi")
        override fun onMapReady(googleMap: GoogleMap) {
            with(googleMap) {

                // Disabilita i pulsanti di navigazione sulla mappa
                uiSettings.isMapToolbarEnabled = false

                // Sposta la telecamera al centro dell'Italia con il livello di zoom predefinito
                moveCamera(CameraUpdateFactory.newLatLngZoom(italia,zoomlvl))

                /* LAGGGGA
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

                 */
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





    class PaginaSignIn : Activity() {
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.signup)
            val itemsGiorno = ArrayList<String>()
            for (i in 1..31) {
                itemsGiorno.add(
                    String.format(
                        "%02d",
                        i
                    )
                ) // Aggiunge giorni formattati con due cifre
            }

            // Popola l'array dei mesi
            val itemsMese = ArrayList<String>()
            for (i in 1..12) {
                itemsMese.add(String.format("%02d", i)) // Aggiunge mesi formattati con due cifre
            }

            // Popola l'array degli anni
            val itemsAnno = ArrayList<String>()
            for (i in 1900..2030) {
                itemsAnno.add(i.toString())
            }
            // Creazione di un adapter per lo Spinner utilizzando l'array di stringhe
            val adapterGiorno =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsGiorno)
            val adapterMese = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsMese)
            val adapterAnno = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsAnno)

            // Imposta lo stile del dropdown
            adapterGiorno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapterMese.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapterAnno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Ottieni il riferimento al tuo Spinner utilizzando l'ID
            val spinnerGiorno = findViewById<Spinner>(R.id.Giorno)
            val spinnerMese = findViewById<Spinner>(R.id.Mese)
            val spinnerAnno = findViewById<Spinner>(R.id.Anno)
            // Imposta l'adapter sullo Spinner
            spinnerGiorno.adapter = adapterGiorno
            spinnerMese.adapter = adapterMese
            spinnerAnno.adapter = adapterAnno

            spinnerGiorno.setSelection(1)
            spinnerMese.setSelection(1)
            spinnerAnno.setSelection(100)

            val reg = findViewById<Button>(R.id.buttonSignup)
            val emailfield = findViewById<EditText>(R.id.editTextEmail)
            val nomefield = findViewById<EditText>(R.id.editTextNome)
            val cognomefield = findViewById<EditText>(R.id.editTextCognome)
            val telefonofield = findViewById<EditText>(R.id.editTextTelefono)

            reg.setOnClickListener {
                val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
                reg.startAnimation(animation)

                val nome = nomefield.text.toString()
                val cognome = cognomefield.text.toString()
                val telefono = telefonofield.text.toString()
                val email = emailfield.text.toString()
                val password = generateRandomPassword(12)


            }
        }
        //funzione finta per creare password randomiche, ovviamente da cambiare
        private fun generateRandomPassword(length: Int): String {
            val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_-+=<>?/{}[]"
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }











    class PaginaLogin : Activity() {
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.login)
            val buttonSignUp = findViewById<TextView>(R.id.registatiTesto)
            val buttonLogin = findViewById<Button>(R.id.buttonlogin)
            buttonSignUp.setOnClickListener {
                val intent = Intent(this@PaginaLogin, PaginaSignIn::class.java)
                startActivity(intent)
            }
            buttonLogin.setOnClickListener {
                val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
                buttonLogin.startAnimation(animation)
                //Mettere controllo con db qui
                //if true
                val intent = Intent(this@PaginaLogin, PaginaProfilo::class.java)
                startActivity(intent)
            }

            val impostazioniViewLog = findViewById<LinearLayout>(R.id.ImpostazioniLoginComparsa)
            impostazioniViewLog.visibility = View.GONE

            val mostraNascondiImpostazioniLog = findViewById<ImageButton>(R.id.button_menu_log)
            mostraNascondiImpostazioniLog.setOnClickListener {
                if (impostazioniViewLog.visibility == View.VISIBLE) {
                    // Se le impostazioni sono già visibili, nasconderle
                    impostazioniViewLog.visibility = View.GONE
                } else {
                    // Altrimenti, mostrale
                    impostazioniViewLog.visibility = View.VISIBLE
                }
            }

            val tornaIndietroButton = findViewById<ImageButton>(R.id.tornaInDietroLog)
            tornaIndietroButton.setOnClickListener{
                finish()
            }

            val impostazionePulsanteLog = findViewById<Button>(R.id.impostazioniLogin)
            val contattiPulsanteLog = findViewById<Button>(R.id.contattiLogin)
            val aiutoPulsanteLog = findViewById<Button>(R.id.aiutoLogin)

            impostazionePulsanteLog.setOnClickListener {
                val intent = Intent(this@PaginaLogin, PaginaImpostazioni::class.java)
                startActivity(intent)
            }
            contattiPulsanteLog.setOnClickListener {
                val intent = Intent(this@PaginaLogin, PaginaContatti::class.java)
                startActivity(intent)
            }
            aiutoPulsanteLog.setOnClickListener {
                val intent = Intent(this@PaginaLogin, PaginaAiuto::class.java)
                startActivity(intent)
            }



        }
    }


    class PaginaProfilo : Activity(){
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.profilo)
        }
    }

    class PaginaImpostazioni : Activity() {
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.impostazioni)
        }
    }

    class PaginaContatti : Activity(){
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.contatti)
        }
    }
    class PaginaAiuto : Activity(){
        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.aiuto)
        }
    }








