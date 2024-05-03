package com.example.eventlink

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : Activity(), OnMapReadyCallback {

    private val italia = LatLng(43.0, 11.53)

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
        setContentView(R.layout.activity_main)
        val mapFragment : MapFragment? =
            fragmentManager.findFragmentById(R.id.map) as? MapFragment
        mapFragment?.getMapAsync(this)

    }

    //Appertura mappa
    override fun onMapReady(googleMap: GoogleMap) {
        with(googleMap) {


            // Disabilita i pulsanti di navigazione
            uiSettings.isMapToolbarEnabled = false

            //Creazione delle icone
            moveCamera(CameraUpdateFactory.newLatLngZoom(italia,zoomlvl))
            val bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.logomontagna) // Carica l'immagine dalla risorsa
            val resizedBitmap1 = Bitmap.createScaledBitmap(bitmap1, 100, 100, false) // Ridimensiona l'immagine alle dimensioni desiderate

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

            //Aggiungi un marker da indirizzo
            val geocoder = Geocoder(this@MainActivity)
            val address = "Piazza dei donatori di sangue 1 Isola Vicentina VI"
            val locations = geocoder.getFromLocationName(address, 1)
            if (!locations.isNullOrEmpty()) {
                val firstLocation = locations[0]
                println("Latitude: ${firstLocation.latitude}, Longitude: ${firstLocation.longitude}")
                val aaa = MarkerOptions()
                    .position(LatLng(firstLocation.latitude, firstLocation.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap1))
                    .anchor(-0.01f, 1.0f)
                    .title("Test titolo")
                    .snippet("Test descrizione")
                googleMap.addMarker(aaa)
            }

            googleMap.setInfoWindowAdapter(object :GoogleMap.InfoWindowAdapter{
                override fun getInfoWindow(marker: Marker): View? {
                    // Ritorna null per utilizzare l'implementazione predefinita
                    return null
                }
                override fun getInfoContents(marker: Marker): View? {
                    val view = layoutInflater.inflate(R.layout.indicatore_info_contents, null)

                    val titleTextView = view.findViewById<TextView>(R.id.title)
                    val snippetTextView = view.findViewById<TextView>(R.id.description)
                    val button1 = view.findViewById<Button>(R.id.button1)
                    val button2 = view.findViewById<Button>(R.id.button2)


                    titleTextView.text = marker.title
                    snippetTextView.text = marker.snippet

                    button1.setOnClickListener {
                        // TODO: Evento Bottone Apri
                    }

                    button2.setOnClickListener {
                        // TODO: Evento Bottone Naviga
                    }
                    return view
                }
            })

        }
    }

}
