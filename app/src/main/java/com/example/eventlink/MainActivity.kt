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
            - Implementare filtri: tag + data
            - Alleggerire loadMap()
            - Implementare bottone Preferiti al indicatore_info_contents + logica
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
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.example.eventlink.Other.CustomClusterRenderer
import com.example.eventlink.Other.MyClusterItem
import com.example.eventlink.Pages.PaginaAiuto
import com.example.eventlink.Pages.PaginaContatti
import com.example.eventlink.Pages.PaginaEvento
import com.example.eventlink.Pages.PaginaImpostazioni
import com.example.eventlink.Pages.PaginaLogin
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

@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore
private lateinit var clusterManager: ClusterManager<MyClusterItem>
private lateinit var customClusterRenderer: CustomClusterRenderer
class MainActivity : Activity(), OnMapReadyCallback {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment: MapFragment? =
            fragmentManager.findFragmentById(R.id.map) as? MapFragment
        mapFragment?.getMapAsync(this)

        val settingsViewMain = findViewById<LinearLayout>(R.id.Impostazioni)
        settingsViewMain.visibility = View.GONE
        val showHideSettingsMain = findViewById<ImageButton>(R.id.button_menu)
        showHideSettingsMain.setOnClickListener {
            if (settingsViewMain.visibility == View.VISIBLE) {
                settingsViewMain.visibility = View.GONE
            } else {
                settingsViewMain.visibility = View.VISIBLE
            }
        }
        val settingsButtonMain = findViewById<Button>(R.id.impostazioniMain)
        val contactsButtonMain = findViewById<Button>(R.id.contattiMain)
        val helpButtonMain = findViewById<Button>(R.id.aiutoMain)
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


        val filterView = findViewById<LinearLayout>(R.id.filtrilayout)
        filterView.visibility = View.GONE

        val zoomView = findViewById<LinearLayout>(R.id.zoomview)

        val buttonShowFilter = findViewById<Button>(R.id.filtri)
        val buttonHideFilter = findViewById<Button>(R.id.chiudifiltri)

        buttonShowFilter.setOnClickListener{
            buttonShowFilter.visibility= View.GONE
            filterView.visibility = View.VISIBLE
            val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_120dp)
            layoutParams.topMargin = newMarginTop
            zoomView.layoutParams = layoutParams
        }
        buttonHideFilter.setOnClickListener{
            filterView.visibility = View.GONE
            buttonShowFilter.visibility= View.VISIBLE
            val layoutParams = zoomView.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = resources.getDimensionPixelSize(R.dimen.margin_top_400dp)
            layoutParams.topMargin = newMarginTop
            zoomView.layoutParams = layoutParams
        }

        val items = arrayOf("Oggi", "Domani", "Weekend", "Questa settimana", "Prossima settimana", "Questo mese")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerDate = findViewById<Spinner>(R.id.date)

        spinnerDate.adapter = adapter


        val accountButton = findViewById<ImageButton>(R.id.button_profile)
        accountButton.setOnClickListener{
            val intent = Intent(this@MainActivity, PaginaLogin::class.java)
            startActivity(intent)
        }


    }
    @SuppressLint("PotentialBehaviorOverride", "DiscouragedApi")
    override fun onMapReady(googleMap: GoogleMap) {
        with(googleMap) {
            uiSettings.isMapToolbarEnabled = false
            val italia = LatLng(42.0, 11.53)
            val zoomlvl = 6f
            moveCamera(CameraUpdateFactory.newLatLngZoom(italia,zoomlvl))
            runBlocking {
                loadMap(this@MainActivity,googleMap,resources,packageName)
            }
            googleMap.setOnMarkerClickListener(null)
            clusterManager.setOnClusterItemClickListener { item ->
                val id = item.tag as? String

                val view = layoutInflater.inflate(R.layout.indicatore_info_contents, null)

                val titleTextView = view.findViewById<TextView>(R.id.title)
                val snippetTextView = view.findViewById<TextView>(R.id.description)
                val buttonEvent = view.findViewById<Button>(R.id.button1)
                val buttonNavigator = view.findViewById<Button>(R.id.button2)

                titleTextView.text = item.title
                snippetTextView.text = item.snippet
                buttonEvent.setOnClickListener {
                    val intent = Intent(this@MainActivity, PaginaEvento::class.java)
                    intent.putExtra("markerId", id)
                    startActivity(intent)
                }
                buttonNavigator.setOnClickListener {
                    val latitude = item.position.latitude
                    val longitude = item.position.longitude
                    val uri = "http://maps.google.com/maps?q=loc:$latitude,$longitude"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(intent)
                }
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
        }
    }
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
                "Indirizzo: " + document.data.getValue("Indirizzo")
                    .toString() + "\n" +
                        "Data: " + document.data.getValue("Data")
                    .toString() + "\n" +
                        "Ora: " + document.data.getValue("Ora")
                    .toString() + "\n" +
                        "Prezzo: " + document.data.getValue("Prezzo").toString()
            val immagine = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
            val tag = document.id
            val clusterItem = MyClusterItem(position, title, description, immagine, tag)
            items.add(clusterItem)
        }
        clusterManager.addItems(items)
        clusterManager.setAnimation(false)
        clusterManager.cluster()
    }
}

