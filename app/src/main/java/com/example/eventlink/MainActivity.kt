@file:Suppress("DEPRECATION")

package com.example.eventlink

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.eventlink.other.CustomClusterRenderer
import com.example.eventlink.other.DatabaseLocale
import com.example.eventlink.other.Evento
import com.example.eventlink.other.EventoLocale
import com.example.eventlink.other.MyClusterItem
import com.example.eventlink.pages.PaginaEvento
import com.example.eventlink.pages.PaginaLogin
import com.example.eventlink.pages.PaginaProfilo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore
private lateinit var clusterManager: ClusterManager<MyClusterItem>
private lateinit var customClusterRenderer: CustomClusterRenderer
var filtriApplicati = mutableListOf<Boolean>()
var global_email : String = ""
private lateinit var fusedLocationClient: FusedLocationProviderClient
private const val LOCATION_PERMISSION_REQUEST_CODE = 1
var lista = mutableListOf<Evento>()
var currentLatLng : LatLng = LatLng(0.0, 0.0)
lateinit var databaseLoc: DatabaseLocale
@SuppressLint("StaticFieldLeak")
lateinit var global_parent:LinearLayout
class MainActivity : Activity(), OnMapReadyCallback {
    @SuppressLint("InflateParams", "CutPasteId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inizializzazione della mappa
        val mapFragment: MapFragment? = fragmentManager.findFragmentById(R.id.map) as? MapFragment
        mapFragment?.getMapAsync(this)

        // Inizializzazione del database locale e del servizio di localizzazione
        databaseLoc = DatabaseLocale.getInstance(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Richiesta dei permessi di localizzazione se non sono stati concessi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }

        // Gestione del risultato della richiesta di permessi di localizzazione
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location ->
            if(location!=null) {
                // Se è disponibile la posizione corrente, aggiorna la variabile currentLatLng e mostra gli elementi UI
                val caricamneto = findViewById<RelativeLayout>(R.id.caricamento)
                val bordoview = findViewById<View>(R.id.bordoview)
                val secondlinearlayout = findViewById<LinearLayout>(R.id.second_linear_layout)
                val mappaview = findViewById<FrameLayout>(R.id.mappa_view)

                currentLatLng = LatLng(location.latitude, location.longitude)

                caricamneto.visibility = View.GONE
                bordoview.visibility = View.VISIBLE
                secondlinearlayout.visibility = View.VISIBLE
                mappaview.visibility = View.VISIBLE
            }else{
                // Se la posizione corrente non è disponibile, imposta una posizione predefinita e mostra gli elementi UI
                val caricamneto = findViewById<RelativeLayout>(R.id.caricamento)
                val bordoview = findViewById<View>(R.id.bordoview)
                val secondlinearlayout = findViewById<LinearLayout>(R.id.second_linear_layout)
                val mappaview = findViewById<FrameLayout>(R.id.mappa_view)

                currentLatLng = LatLng(42.0, 11.53)

                caricamneto.visibility = View.GONE
                bordoview.visibility = View.VISIBLE
                secondlinearlayout.visibility = View.VISIBLE
                mappaview.visibility = View.VISIBLE
            }
        }

        // Inizializzazione degli elementi UI
        val filterView = findViewById<LinearLayout>(R.id.filtrilayout)
        val zoomView = findViewById<LinearLayout>(R.id.zoomview)
        val buttonShowFilter = findViewById<Button>(R.id.filtri)
        val buttonHideFilter = findViewById<Button>(R.id.chiudifiltri)
        val spinnerDate = findViewById<Spinner>(R.id.date)
        val buttonMenu = findViewById<ImageView>(R.id.bottone_menu)
        val bottonepreferiti = findViewById<ImageView>(R.id.bottone_preferiti)
        val bottonemappa = findViewById<ImageView>(R.id.bottone_mappa)
        val bottonelista = findViewById<ImageView>(R.id.bottone_lista)
        val bottoneprofilo = findViewById<ImageView>(R.id.bottone_profilo)
        val testomenu = findViewById<TextView>(R.id.testo_menu)
        val testopreferiti = findViewById<TextView>(R.id.testo_preferiti)
        val testomappa = findViewById<TextView>(R.id.testo_mappa)
        val testolista = findViewById<TextView>(R.id.testo_lista)
        val testoprofilo = findViewById<TextView>(R.id.testo_profilo)
        val geobtn = findViewById<ImageButton>(R.id.dovesonobtn)
        val linearMenu = findViewById<LinearLayout>(R.id.linear_menu)
        val linearPreferiti = findViewById<LinearLayout>(R.id.linear_preferiti)
        val linearMappa = findViewById<LinearLayout>(R.id.linear_mappa)
        val linearLista = findViewById<LinearLayout>(R.id.linear_lista)
        val linearProfilo = findViewById<LinearLayout>(R.id.linear_profilo)
        val granderelativo = findViewById<RelativeLayout>(R.id.granderelativo)
        val mappaview = findViewById<FrameLayout>(R.id.mappa_view)

        // Inizializzazione visibilità degli elementi
        filterView.visibility = View.GONE

        // Definizione degli eventi per mostrare/nascondere i filtri
        buttonShowFilter.setOnClickListener{
            buttonShowFilter.visibility= View.GONE
            filterView.visibility = View.VISIBLE
            val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_120dp)
            layoutParams.topMargin = newMarginTop
            zoomView.layoutParams = layoutParams

            val layoutParams2 = geobtn.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop2 = resources.getDimensionPixelSize(R.dimen.margin_top_45dp)
            layoutParams2.topMargin = newMarginTop2
            geobtn.layoutParams = layoutParams2



        }
        buttonHideFilter.setOnClickListener{
            filterView.visibility = View.GONE
            buttonShowFilter.visibility= View.VISIBLE
            val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_480dp)
            layoutParams.topMargin = newMarginTop
            zoomView.layoutParams = layoutParams

            val layoutParams2 = geobtn.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop2 = resources.getDimensionPixelSize(R.dimen.margin_top_180dp)
            layoutParams2.topMargin = newMarginTop2
            geobtn.layoutParams = layoutParams2
        }

        // Inizializzazione e definizione degli eventi per lo spinner della data
        val items = arrayOf("Oggi", "Domani", "Weekend", "Questa settimana", "Prossima settimana", "Questo mese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDate.adapter = adapter

        // Inizializzazione dei colori di base per l'arancio e il grigio
        val baseColor = ContextCompat.getColor(this, R.color.arancio)
        val alphaValue = 200
        val colorWithAlpha = (alphaValue shl 24) or (baseColor and 0x00FFFFFF)
        val baseColorB = ContextCompat.getColor(this, R.color.grigio)
        val colorFilter = PorterDuffColorFilter(colorWithAlpha, PorterDuff.Mode.SRC_IN)
        val colorFilterB = PorterDuffColorFilter(baseColorB, PorterDuff.Mode.SRC_IN)

        // Applicazione dei filtri di colore e impostazione dei colori del testo per gli elementi UI
        bottonemappa.colorFilter = colorFilter
        buttonMenu.colorFilter = colorFilterB
        bottonepreferiti.colorFilter = colorFilterB
        bottonelista.colorFilter = colorFilterB
        bottoneprofilo.colorFilter = colorFilterB

        testomenu.setTextColor(baseColorB)
        testopreferiti.setTextColor(baseColorB)
        testomappa.setTextColor(colorWithAlpha)
        testolista.setTextColor(baseColorB)
        testoprofilo.setTextColor(baseColorB)

        // Impostazione del listener per il click sul layout del profilo utente.
        linearProfilo.setOnClickListener{
            if(global_email!="") {
                val intent = Intent(this@MainActivity, PaginaProfilo::class.java)
                intent.putExtra("email", global_email)
                startActivity(intent)
            }
            else{
                val intent = Intent(this@MainActivity, PaginaLogin::class.java)
                startActivity(intent)
            }
        }

        var newview  :  View = layoutInflater.inflate(R.layout.menuview, null)

        // Impostazione del listener per il click sul layout del menu.
        linearMenu.setOnClickListener{
            mappaview.visibility = View.GONE
            granderelativo.removeView(newview)
            bottonemappa.colorFilter = colorFilterB
            buttonMenu.colorFilter = colorFilter
            bottonepreferiti.colorFilter = colorFilterB
            bottonelista.colorFilter = colorFilterB
            bottoneprofilo.colorFilter = colorFilterB

            testomenu.setTextColor(colorWithAlpha)
            testopreferiti.setTextColor(baseColorB)
            testomappa.setTextColor(baseColorB)
            testolista.setTextColor(baseColorB)
            testoprofilo.setTextColor(baseColorB)
            newview = layoutInflater.inflate(R.layout.menuview, null)
            granderelativo.addView(newview)
        }
        // Impostazione del listener per il click sul layout della pagina preferiti.
        linearPreferiti.setOnClickListener{
            mappaview.visibility = View.GONE
            granderelativo.removeView(newview)

            bottonemappa.colorFilter = colorFilterB
            buttonMenu.colorFilter = colorFilterB
            bottonepreferiti.colorFilter = colorFilter
            bottonelista.colorFilter = colorFilterB
            bottoneprofilo.colorFilter = colorFilterB

            testomenu.setTextColor(baseColorB)
            testopreferiti.setTextColor(colorWithAlpha)
            testomappa.setTextColor(baseColorB)
            testolista.setTextColor(baseColorB)
            testoprofilo.setTextColor(baseColorB)
            var strada : Float
            for(document in lista){
                runBlocking {
                    strada = calcolaLoc(document.Posizione_Mappa)
                }
                document.distanza = strada
            }
            lista.sortBy {
                it.distanza
            }
            newview = layoutInflater.inflate(R.layout.preferitiview ,null)
            granderelativo.addView(newview)

            global_parent = this.findViewById(R.id.parente_nascosto2)
            setPre2(this@MainActivity, global_parent)
        }

        // Impostazione del listener per il click sul layout della pagina lista.
        linearLista.setOnClickListener{
            mappaview.visibility = View.GONE
            granderelativo.removeView(newview)

            bottonemappa.colorFilter = colorFilterB
            buttonMenu.colorFilter = colorFilterB
            bottonepreferiti.colorFilter = colorFilterB
            bottonelista.colorFilter = colorFilter
            bottoneprofilo.colorFilter = colorFilterB

            testomenu.setTextColor(baseColorB)
            testopreferiti.setTextColor(baseColorB)
            testomappa.setTextColor(baseColorB)
            testolista.setTextColor(colorWithAlpha)
            testoprofilo.setTextColor(baseColorB)
            var strada : Float
            for(document in lista){
                runBlocking {
                    strada = calcolaLoc(document.Posizione_Mappa)
                }
                document.distanza = strada
            }
            lista.sortBy {
                it.distanza
            }
            newview = layoutInflater.inflate(R.layout.listaview ,null)
            granderelativo.addView(newview)
            val parent = this.findViewById<LinearLayout>(R.id.parente_nascosto1)
            setPre(this@MainActivity, parent)

        }

        // Impostazione del listener per il click sul layout della pagina Mappa.
        linearMappa.setOnClickListener{
            granderelativo.removeView(newview)
            mappaview.visibility = View.VISIBLE


            bottonemappa.colorFilter = colorFilter
            buttonMenu.colorFilter = colorFilterB
            bottonepreferiti.colorFilter = colorFilterB
            bottonelista.colorFilter = colorFilterB
            bottoneprofilo.colorFilter = colorFilterB

            testomenu.setTextColor(baseColorB)
            testopreferiti.setTextColor(baseColorB)
            testomappa.setTextColor(colorWithAlpha)
            testolista.setTextColor(baseColorB)
            testoprofilo.setTextColor(baseColorB)
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("PotentialBehaviorOverride", "DiscouragedApi")
    override fun onMapReady(googleMap: GoogleMap) {
        with(googleMap) {
            // Disabilita la toolbar della mappa
            uiSettings.isMapToolbarEnabled = false

            // Imposta la posizione della mappa su Italia e lo zoom
            val italia = LatLng(42.0, 11.53)
            val zoomlvl = 6f
            moveCamera(CameraUpdateFactory.newLatLngZoom(italia,zoomlvl))

            // Gestione dei pulsanti per lo zoom
            val zoomInButton = findViewById<Button>(R.id.btp)
            val zoomOutButton = findViewById<Button>(R.id.btm)
            zoomInButton.setOnClickListener {
                val currentZoomLevel = googleMap.cameraPosition.zoom
                val newZoomLevel = currentZoomLevel + 1
                val cameraUpdate = CameraUpdateFactory.zoomTo(newZoomLevel)
                googleMap.animateCamera(cameraUpdate)
            }
            zoomOutButton.setOnClickListener {
                val currentZoomLevel = googleMap.cameraPosition.zoom
                val newZoomLevel = currentZoomLevel - 1
                val cameraUpdate = CameraUpdateFactory.zoomTo(newZoomLevel)
                googleMap.animateCamera(cameraUpdate)
            }

            // Gestione del pulsante "Dove sono"
            val dovesonobtn = findViewById<ImageButton>(R.id.dovesonobtn)
            dovesonobtn.setOnClickListener{
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                currentLatLng = LatLng(location.latitude, location.longitude)
                                val updateCamera = CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f)
                                googleMap.animateCamera(updateCamera)
                            }
                        }
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE)
                }

            }

            // Carica gli eventi sulla mappa
            runBlocking {
                loadMap(this@MainActivity,googleMap,resources,packageName)
            }

            // Disabilita la gestione predefinita dei click sui marker
            googleMap.setOnMarkerClickListener(null)

            // Gestione del click sui cluster o singoli marker
            clusterManager.setOnClusterItemClickListener { item ->
                val id = item.tag as? String

                // Crea e mostra il dialog con le informazioni sull'evento
                val view = layoutInflater.inflate(R.layout.indicatore_info_contents, null)
                val titleTextView = view.findViewById<TextView>(R.id.title)
                val snippetTextView = view.findViewById<TextView>(R.id.description)
                val buttonEvent = view.findViewById<Button>(R.id.button1)
                val buttonNavigator = view.findViewById<Button>(R.id.button2)
                val btnPreferitiIndicatore = view.findViewById<ImageButton>(R.id.preferitiindicatore)

                titleTextView.text = item.title
                snippetTextView.text = item.snippet

                // Gestione del click sul pulsante per navigare
                buttonNavigator.setOnClickListener {
                    val latitude = item.position.latitude
                    val longitude = item.position.longitude
                    val uri = "http://maps.google.com/maps?q=loc:$latitude,$longitude"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(intent)
                }

                // Gestione del pulsante "Preferiti"
                var ispreferito : Boolean
                runBlocking {
                    ispreferito = id?.let { it1 -> databaseLoc.DAOEventoLocale().doesEventExist(it1) } == true
                }
                if (ispreferito) btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_1002)
                btnPreferitiIndicatore.setOnClickListener{
                    runBlocking {
                        ispreferito = id?.let { it1 -> databaseLoc.DAOEventoLocale().doesEventExist(it1) } == true
                    }
                    if (!ispreferito) {
                        btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_1002)
                        runBlocking {
                            id?.let { it1 -> EventoLocale(it1) }
                                ?.let { it2 -> databaseLoc.DAOEventoLocale().insert(it2) }
                        }
                    } else {
                        btnPreferitiIndicatore.setImageResource(R.drawable.icons8_preferiti_100)
                        runBlocking {
                            id?.let { it1 -> EventoLocale(it1) }
                                ?.let { it2 -> databaseLoc.DAOEventoLocale().delete(it2) }
                        }
                    }
                }

                // Centra la mappa sulla posizione dell'evento
                val target = LatLng(item.position.latitude - 0.001, item.position.longitude)
                val zoomLevel = 11f
                val currentZoom = googleMap.cameraPosition.zoom
                if (currentZoom < zoomLevel) {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(target, zoomLevel)
                    googleMap.animateCamera(cameraUpdate, 1000, null)
                } else {
                    val cameraUpdate = CameraUpdateFactory.newLatLng(target)
                    googleMap.animateCamera(cameraUpdate, 1000, null)
                }

                // Visualizza il dialog
                val dialog = AlertDialog.Builder(this@MainActivity, R.style.AlertDialogCustom)
                    .setView(view)
                    .create()
                dialog.setOnShowListener {
                    val window = dialog.window
                    val params = window?.attributes
                    val gravity = Gravity.TOP
                    val xOffset = 0
                    val yOffset = 370
                    params?.gravity = gravity
                    params?.x = xOffset
                    params?.y = yOffset
                    window?.attributes = params
                }
                dialog.show()

                // Gestione del click sul pulsante "Dettagli evento"
                buttonEvent.setOnClickListener {
                    val intent = Intent(this@MainActivity, PaginaEvento::class.java)
                    intent.putExtra("markerId", id)
                    dialog.dismiss()
                    startActivity(intent)
                }
                true
            }

            // Gestione del pulsante "Applica filtri"
            val applicaFiltriBottone = findViewById<Button>(R.id.applicaFiltri)
            val resetFiltriBottone = findViewById<Button>(R.id.resetFiltri)
            applicaFiltriBottone.setOnClickListener {
                clusterManager.renderer = customClusterRenderer
                googleMap.setOnCameraIdleListener(clusterManager)
                val checkboxIds = listOf(R.id.cb1,R.id.cb2,R.id.cb3,R.id.cb4,R.id.cb5,R.id.cb6,R.id.cb7,R.id.cb8,R.id.cb9,R.id.cb10)
                for (checkboxId in checkboxIds) {
                    val checkbox = findViewById<CheckBox>(checkboxId)
                    filtriApplicati.add(checkbox.isChecked)
                }
                val spinnerDate = findViewById<Spinner>(R.id.date)
                val selectedDate = spinnerDate.selectedItem.toString()
                clusterManager.clearItems()
                clusterManager.addItems(droppaItem(selectedDate))
                clusterManager.cluster()
                filtriApplicati.clear()
            }

            // Gestione del pulsante "Resetta filtri"
            resetFiltriBottone.setOnClickListener {
                clusterManager.renderer = customClusterRenderer
                googleMap.setOnCameraIdleListener(clusterManager)
                repeat(10) {
                    filtriApplicati.add(true)
                }
                val checkboxIds = listOf(R.id.cb1, R.id.cb2, R.id.cb3, R.id.cb4, R.id.cb5, R.id.cb6, R.id.cb7, R.id.cb8, R.id.cb9, R.id.cb10)
                for (checkboxId in checkboxIds) {
                    val checkbox = findViewById<CheckBox>(checkboxId)
                    checkbox.isChecked = false
                }
                var items: MutableList<MyClusterItem>
                runBlocking {
                    items = droppaItem("null")
                }
                clusterManager.clearItems()
                clusterManager.addItems(items)
                clusterManager.cluster()
                filtriApplicati.clear()
            }
        }
    }

    //Carica gli eventi sulla mappa.
    @SuppressLint("DiscouragedApi")
    suspend fun loadMap(context1: Context, googleMap: GoogleMap, resources :  android.content.res.Resources, packageName:String){
        clusterManager = ClusterManager<MyClusterItem>(context1, googleMap)
        customClusterRenderer = CustomClusterRenderer(context1, googleMap, clusterManager)
        clusterManager.renderer = customClusterRenderer
        googleMap.setOnCameraIdleListener(clusterManager)

        val items = mutableListOf<MyClusterItem>()
        val result = db.collection("Eventi")
            .get()
            .await()

        for (document in result) {
            val ico = document.data.getValue("Tipo").toString()
            val resourceId = resources.getIdentifier(ico, "drawable", packageName)
            val bitmap = BitmapFactory.decodeResource(resources, resourceId)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
            val locString =  document.data.getValue("Posizione").toString()
            val delimiter ="&"
            val location = locString.split(delimiter)
            val position = LatLng(location[0].toDouble(), location[1].toDouble())
            val title = document.data.getValue("Titolo").toString()
            val description =
                        "Indirizzo: " + document.data.getValue("Indirizzo").toString() + "\n" +
                        "Data: " + document.data.getValue("Data").toString() + "\n" +
                        "Ora: " + document.data.getValue("Ora").toString() + "\n" +
                        "Prezzo: " + document.data.getValue("Prezzo").toString()

            val immagine = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
            val tag = document.id

            lista.add(Evento(
                tag,
                document.data.getValue("Data").toString(),
                document.data.getValue("Descrizione").toString(),
                document.data.getValue("ID_Azienda").toString(),
                document.data.getValue("Immagine").toString(),
                document.data.getValue("Indirizzo").toString(),
                document.data.getValue("Max_Prenotazioni").toString(),
                document.data.getValue("Ora").toString(),
                locString,
                document.data.getValue("Prenotazione").toString(),
                document.data.getValue("Prezzo").toString(),
                ico,
                title,
                description,
                immagine,
                position,
                0.0F
            ))
            val clusterItem = MyClusterItem(position, title, description, immagine, tag)
            items.add(clusterItem)
        }
        clusterManager.addItems(items)
        clusterManager.setAnimation(false)
        clusterManager.cluster()
    }

    //Restituisce una lista di oggetti MyClusterItem in base alla data specificata e ai filtri applicati.
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("DiscouragedApi")
    fun droppaItem(data : String): MutableList<MyClusterItem> {
        val items = mutableListOf<MyClusterItem>()
        if(data=="null"){
            for(eventi in lista){
                val clusterItem = MyClusterItem(eventi.Posizione_Mappa, eventi.Titolo, eventi.Descrizione_Mappa, eventi.Immagine_Mappa, eventi.ID_Evento)
                items.add(clusterItem)
            }
        }
        else{
            val tipi = listOf("concerto", "sport", "gastronomia", "convegno", "mostra", "spettacolo", "outdoor", "escursione", "networking", "educazione")
            for (document in lista) {
                val oggi = LocalDate.now()
                var giornomassimo = oggi
                var giornominimo = oggi
                when(data) {
                    "Domani" -> {
                        giornominimo = oggi.plusDays(1)
                        giornomassimo = giornominimo
                    }
                    "Weekend" -> {
                        giornominimo = oggi.plusDays((6 - oggi.dayOfWeek.value).toLong())
                        giornomassimo = giornominimo.plusDays(1)
                    }
                    "Questa settimana" -> {
                        giornomassimo = oggi.plusDays((7 - oggi.dayOfWeek.value).toLong())
                    }
                    "Prossima settimana" -> {
                        giornominimo = oggi.plusDays((8 - oggi.dayOfWeek.value).toLong())
                        giornomassimo = giornominimo.plusDays(6)
                    }
                    "Questo mese" -> {
                        giornomassimo = oggi.plusMonths(1)
                    }
                }
                val ico = document.Tipo
                val dataDb = convertiData(document.Data)
                val isWithinRange = (dataDb.isEqual(giornominimo) || dataDb.isAfter(giornominimo)) &&
                        (dataDb.isEqual(giornomassimo) || dataDb.isBefore(giornomassimo))
                if ((filtriApplicati[tipi.indexOf(ico)] && isWithinRange) || (isWithinRange  && !filtriApplicati.contains(true) )) {
                    val clusterItem = MyClusterItem(document.Posizione_Mappa, document.Titolo, document.Descrizione_Mappa, document.Immagine_Mappa, document.ID_Evento)
                    items.add(clusterItem)
                }
            }
        }
        return items
    }

    // Converte una stringa di data nel formato "d/M/yyyy" in un oggetto LocalDate.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertiData(data: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        return LocalDate.parse(data, formatter)
    }

    // Imposta la visualizzazione degli eventi nella lista.
    @SuppressLint("SetTextI18n", "InflateParams")
    fun setPre(context: Context, parent: LinearLayout){
        for(document in lista ){
            val image = document.Immagine
            val title = document.Titolo
            val time = document.Ora
            val date = document.Data
            val distance = BigDecimal(document.distanza.toDouble()).setScale(2,RoundingMode.HALF_EVEN)
            val inflater = LayoutInflater.from(context)
            val duplicateView = inflater.inflate(R.layout.visionelista, null)

            val text = duplicateView.findViewById<TextView>(R.id.pUtente_DescrizioneEvento1)
            text.text = "$title\n$time $date\n$distance KM"
            val img = duplicateView.findViewById<ImageView>(R.id.immagine_Evento1)
            Glide.with(context).load(image).transform(RoundedCorners(16)).into(img)
            img.contentDescription = "Image"

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_top_16dp) // Ad esempio, 16dp di margine inferiore
            duplicateView.layoutParams = layoutParams

            val btnvisionelista = duplicateView.findViewById<LinearLayout>(R.id.linearEventoLista)
            btnvisionelista.setOnClickListener{
                val intent = Intent(this@MainActivity, PaginaEvento::class.java)
                intent.putExtra("markerId", document.ID_Evento)
                startActivity(intent)
            }
            parent.addView(duplicateView)

        }
    }

    // Calcola la distanza in chilometri tra la posizione corrente e una posizione specificata.
    private fun calcolaLoc(position : LatLng): Float{
        val approx = Location("position").apply {
            latitude = position.latitude
            longitude = position.longitude
        }
        val end = Location("currentLatLng").apply {
            latitude = currentLatLng.latitude
            longitude = currentLatLng.longitude
        }
        return end.distanceTo(approx)/1000
    }

    // Metodo per impostare la visualizzazione degli eventi locali nella lista.
    @SuppressLint("SetTextI18n", "InflateParams", "CheckResult")
    fun setPre2(context: Context, parent: LinearLayout){
        parent.removeAllViews()
        var eEventiLocali : List<EventoLocale>
        runBlocking {
            eEventiLocali=eventilocaAll()
        }

        for(eventinilocali in eEventiLocali){
            val rispettivo = lista.filter { it.ID_Evento == eventinilocali.ID_Evento }
            rispettivo.forEach{
                val image = it.Immagine
                val title = it.Titolo
                val time = it.Ora
                val date = it.Data
                val distance = BigDecimal(it.distanza.toDouble()).setScale(2, RoundingMode.HALF_EVEN)
                val inflater = LayoutInflater.from(context)
                val duplicateView = inflater.inflate(R.layout.visionelista, null)

                val text = duplicateView.findViewById<TextView>(R.id.pUtente_DescrizioneEvento1)
                text.text = "$title\n$time $date\n$distance KM"
                val img = duplicateView.findViewById<ImageView>(R.id.immagine_Evento1)
                Glide.with(context)
                    .load(image)
                    .transform(RoundedCorners(16))
                    .into(img)
                img.contentDescription = "Image"

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_top_16dp) // Ad esempio, 16dp di margine inferiore
                duplicateView.layoutParams = layoutParams

                val idEventoOn = it.ID_Evento
                val btnvisionelista = duplicateView.findViewById<LinearLayout>(R.id.linearEventoLista)
                btnvisionelista.setOnClickListener{
                    val intent = Intent(context, PaginaEvento::class.java)
                    intent.putExtra("markerId",idEventoOn)
                    startActivityForResult(intent, 1)
                }
                parent.addView(duplicateView)
            }
        }
    }

    // Funzione sospensiva per ottenere tutti gli eventi locali dal database locale.
    private suspend fun eventilocaAll(): List<EventoLocale> {
        return databaseLoc.DAOEventoLocale().getAllEvent()
    }

    // Metodo per gestire il risultato di un'attività avviata con startActivityForResult.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                setPre2(this@MainActivity, global_parent)
            }
        }
    }
}