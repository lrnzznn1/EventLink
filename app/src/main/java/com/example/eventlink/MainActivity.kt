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
        - MainActivity
            - Implementare filtri tag funzionano ma sono un fia lentini 2 secondi di caricamento secondo me copriamo un una schermatina di caricamento o vediamo che fare
            - I filtri della data funzionano ma il giorno del db dice che è nel 1970 wat...
            - Alleggerire loadMap()
            - Implementare bottone Preferiti al indicatore_info_contents + logica
            - Implementare geolocalizzazione
        - PaginaEvento
            - Implementare menu e profilo
            - Implementare prenota
        - PaginaSignIn
            - Implementare menu e back
        - PaginaLogin
            - Aggiungere variabile globale con id utente se loggato
        - PaginaProfilo
            - Implementare Preferiti, Impostazioni Profilo ed eventuali bottoni(torna alla mappa, ecc..)
        - PaginaImpostazioni
            - Implementare cambio lingua e cambio tema app
        - PaginaContatti
            - Aggiungere info
        - PaginaAiuto
            - Aggiungere FAQ
*/

@file:Suppress("DEPRECATION")

package com.example.eventlink

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.eventlink.other.CustomClusterRenderer
import com.example.eventlink.other.MyClusterItem
import com.example.eventlink.pages.PaginaAiuto
import com.example.eventlink.pages.PaginaContatti
import com.example.eventlink.pages.PaginaEvento
import com.example.eventlink.pages.PaginaImpostazioni
import com.example.eventlink.pages.PaginaLogin
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore
private lateinit var clusterManager: ClusterManager<MyClusterItem>
private lateinit var customClusterRenderer: CustomClusterRenderer
var filtriApplicati = mutableListOf<Boolean>()


class MainActivity : Activity(), OnMapReadyCallback {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the map fragment
        val mapFragment: MapFragment? = fragmentManager.findFragmentById(R.id.map) as? MapFragment
        mapFragment?.getMapAsync(this)

        // Initialize UI elements
        val settingsViewMain = findViewById<LinearLayout>(R.id.Impostazioni)
        val showHideSettingsMain = findViewById<ImageButton>(R.id.button_menu)
        val settingsButtonMain = findViewById<Button>(R.id.impostazioniMain)
        val contactsButtonMain = findViewById<Button>(R.id.contattiMain)
        val helpButtonMain = findViewById<Button>(R.id.aiutoMain)
        val filterView = findViewById<LinearLayout>(R.id.filtrilayout)
        val zoomView = findViewById<LinearLayout>(R.id.zoomview)
        val buttonShowFilter = findViewById<Button>(R.id.filtri)
        val buttonHideFilter = findViewById<Button>(R.id.chiudifiltri)
        val spinnerDate = findViewById<Spinner>(R.id.date)
        val accountButton = findViewById<ImageButton>(R.id.button_profile)


        // Setup for main settings view
        settingsViewMain.visibility = View.GONE
        showHideSettingsMain.setOnClickListener {
            // Toggle visibility of settings view
            if (settingsViewMain.visibility == View.VISIBLE) {
                settingsViewMain.visibility = View.GONE
            } else {
                settingsViewMain.visibility = View.VISIBLE
            }
        }

        // Click listeners for main settings, contacts, and help buttons
        settingsButtonMain.setOnClickListener {
            val intent = Intent(this@MainActivity, PaginaImpostazioni::class.java)
            startActivity(intent)
        }
        contactsButtonMain.setOnClickListener {
            val intent = Intent(this@MainActivity, PaginaContatti::class.java)
            startActivity(intent)
        }
        helpButtonMain.setOnClickListener {
            val intent = Intent(this@MainActivity, PaginaAiuto::class.java)
            startActivity(intent)
        }

        // Setup for filter view and its visibility
        filterView.visibility = View.GONE

        // Setup for zoom view and buttons to show/hide filter
        buttonShowFilter.setOnClickListener{
            // Show filter view and adjust top margin of zoom view
            buttonShowFilter.visibility= View.GONE
            filterView.visibility = View.VISIBLE
            val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_120dp)
            layoutParams.topMargin = newMarginTop
            zoomView.layoutParams = layoutParams
        }
        buttonHideFilter.setOnClickListener{
            // Hide filter view and adjust top margin of zoom view
            filterView.visibility = View.GONE
            buttonShowFilter.visibility= View.VISIBLE
            val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_400dp)
            layoutParams.topMargin = newMarginTop
            zoomView.layoutParams = layoutParams
        }

        // Setup spinner for selecting dates
        val items = arrayOf("Oggi", "Domani", "Weekend", "Questa settimana", "Prossima settimana", "Questo mese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDate.adapter = adapter

        // Click listener for account button to navigate to login page
        accountButton.setOnClickListener{
            val intent = Intent(this@MainActivity, PaginaLogin::class.java)
            startActivity(intent)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("PotentialBehaviorOverride", "DiscouragedApi")
    override fun onMapReady(googleMap: GoogleMap) {
        with(googleMap) {
            // Disable default map toolbar
            uiSettings.isMapToolbarEnabled = false

            // Set initial camera position to Italy
            val italia = LatLng(42.0, 11.53)
            val zoomlvl = 6f
            moveCamera(CameraUpdateFactory.newLatLngZoom(italia,zoomlvl))

            // Set click listeners for zoom buttons
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

            // Load map markers asynchronously
            runBlocking {
                loadMap(this@MainActivity,googleMap,resources,packageName)
            }

            // Disable default marker click event
            googleMap.setOnMarkerClickListener(null)

            // Set custom marker click listener
            clusterManager.setOnClusterItemClickListener { item ->
                // Get marker tag
                val id = item.tag as? String

                // Inflate custom info window layout
                val view = layoutInflater.inflate(R.layout.indicatore_info_contents, null)

                // Initialize views
                val titleTextView = view.findViewById<TextView>(R.id.title)
                val snippetTextView = view.findViewById<TextView>(R.id.description)
                val buttonEvent = view.findViewById<Button>(R.id.button1)
                val buttonNavigator = view.findViewById<Button>(R.id.button2)

                // Set marker title and snippet to views
                titleTextView.text = item.title
                snippetTextView.text = item.snippet

                // Click listener for event button
                buttonEvent.setOnClickListener {
                    val intent = Intent(this@MainActivity, PaginaEvento::class.java)
                    intent.putExtra("markerId", id)
                    startActivity(intent)
                }

                // Click listener for navigation button
                buttonNavigator.setOnClickListener {
                    val latitude = item.position.latitude
                    val longitude = item.position.longitude
                    val uri = "http://maps.google.com/maps?q=loc:$latitude,$longitude"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(intent)
                }

                // Adjust camera position to show info window
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

                // Customize and show AlertDialog with custom view
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
                true
            }



            val applicaFiltriBottone = findViewById<Button>(R.id.applicaFiltri)
            val resetFiltriBottone = findViewById<Button>(R.id.resetFiltri)

            applicaFiltriBottone.setOnClickListener {
                val checkboxIds = listOf(R.id.cb1,R.id.cb2,R.id.cb3,R.id.cb4,R.id.cb5,R.id.cb6,R.id.cb7,R.id.cb8,R.id.cb9,R.id.cb10)

                for (checkboxId in checkboxIds) {
                    val checkbox = findViewById<CheckBox>(checkboxId)
                    filtriApplicati.add(checkbox.isChecked)
                }
                var items: MutableList<MyClusterItem>

                val spinnerDate = findViewById<Spinner>(R.id.date)
                val selectedDate = spinnerDate.selectedItem.toString()

                runBlocking {
                    items = droppaItem(selectedDate)
                }



                clusterManager.clearItems()
                clusterManager.addItems(items)
                clusterManager.cluster()

                filtriApplicati.clear()

            }
            resetFiltriBottone.setOnClickListener {
                repeat(10) {
                    filtriApplicati.add(true)
                }
                // Disattiva tutte le checkbox
                val checkboxIds = listOf(R.id.cb1, R.id.cb2, R.id.cb3, R.id.cb4, R.id.cb5, R.id.cb6, R.id.cb7, R.id.cb8, R.id.cb9, R.id.cb10)
                for (checkboxId in checkboxIds) {
                    val checkbox = findViewById<CheckBox>(checkboxId)
                    checkbox.isChecked = false
                }
                var items: MutableList<MyClusterItem>
                runBlocking {
                    items = droppaItem("Reset")
                }
                clusterManager.clearItems()
                clusterManager.addItems(items)
                clusterManager.cluster()

                filtriApplicati.clear()
            }
        }
    }
    @SuppressLint("DiscouragedApi")
    suspend fun loadMap(context1: Context, googleMap: GoogleMap, resources :  android.content.res.Resources, packageName:String){
        // Initialize ClusterManager and custom renderer
        clusterManager = ClusterManager<MyClusterItem>(context1, googleMap)
        customClusterRenderer = CustomClusterRenderer(context1, googleMap, clusterManager)
        clusterManager.renderer = customClusterRenderer
        googleMap.setOnCameraIdleListener(clusterManager)

        // Initialize list for map markers
        val items = mutableListOf<MyClusterItem>()

        // Fetch event data from Firestore
        val result = db.collection("Eventi")
            .get()
            .await()

        // Process each event document
        for (document in result) {
            // Extract data from document
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

            // Create BitmapDescriptor for marker icon
            val immagine = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
            val tag = document.id

            // Create cluster item
            val clusterItem = MyClusterItem(position, title, description, immagine, tag)
            items.add(clusterItem)
        }

        // Add markers to ClusterManager
        clusterManager.addItems(items)
        clusterManager.setAnimation(false)
        clusterManager.cluster()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("DiscouragedApi")
    suspend fun droppaItem(data : String): MutableList<MyClusterItem> {
        val tipi = listOf("concerto", "sport", "gastronomia", "convegno", "mostra", "spettacolo", "outdoor", "escursione", "networking", "educazione")

        // Initialize list for map markers
        val items = mutableListOf<MyClusterItem>()

        // Fetch event data from Firestore
        val result = db.collection("Eventi")
            .get()
            .await()

        // Process each event document
        for (document in result) {
            // Extract data from document
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

            // Create BitmapDescriptor for marker icon
            val immagine = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
            val tag = document.id

            val oggi = LocalDate.now()
            var giornomassimo = oggi
            var giornominimo = oggi
            when(data){
                "Domani"->{
                    giornominimo = oggi.plusDays(1)
                    giornomassimo = giornominimo
                }
                "Weekend"->{
                    giornominimo = oggi.plusDays((6 - oggi.dayOfWeek.value).toLong())
                    giornomassimo = giornominimo.plusDays(1)
                }
                "Questa settimana"->{
                    giornomassimo = oggi.plusDays((7 - oggi.dayOfWeek.value).toLong())
                }
                "Prossima settimana"->{
                    giornominimo = oggi.plusDays((8 - oggi.dayOfWeek.value).toLong())
                    giornomassimo = giornominimo.plusDays(6)
                }
                "Questo mese"->{
                    giornomassimo = oggi.plusMonths(1)
                }
            }
            Log.d("testdataB", "$giornominimo $giornomassimo")
            val dataDb = convertiData(document.data.getValue("Data").toString())
            Log.d("testdataA", dataDb)
            // Create cluster item only if the tipo corrisponde a uno dei tipi nella lista
            if (filtriApplicati[tipi.indexOf(ico)]) {
                // Create cluster item
                val clusterItem = MyClusterItem(position, title, description, immagine, tag)
                items.add(clusterItem)
            }
        }

        return items
    }
    private fun convertiData(data: String): String {
        // Formato del giorno mese
        val formatoIngresso = SimpleDateFormat("dd MMMM", Locale.getDefault())

        // Formato di uscita "aaaa-gg-mm"
        val formatoUscita = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Parse della data di ingresso
        val dataIngresso = formatoIngresso.parse(data)

        // Formattazione della data nel formato di uscita
        return formatoUscita.format(dataIngresso!!)
    }

}

