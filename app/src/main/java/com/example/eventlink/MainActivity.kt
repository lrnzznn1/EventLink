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
        - Altro
            - Spostare classi in altri file
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
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

@SuppressLint("StaticFieldLeak")
private val db = Firebase.firestore
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

                }
            }
            .addOnFailureListener {}
    }
}
class PaginaSignIn : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)
        val itemsDay = ArrayList<String>()
        for (i in 1..31) {
            itemsDay.add(String.format("%02d", i))
        }
        val itemsMonth = ArrayList<String>()
        for (i in 1..12) {
            itemsMonth.add(String.format("%02d", i))
        }
        val itemsYear = ArrayList<String>()
        for (i in 1900..2030) {
            itemsYear.add(i.toString())
        }
        val adapterDay = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsDay)
        val adapterMonth = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsMonth)
        val adapterYear = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsYear)

        adapterDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerDay = findViewById<Spinner>(R.id.Giorno)
        val spinnerMonth = findViewById<Spinner>(R.id.Mese)
        val spinnerYear = findViewById<Spinner>(R.id.Anno)

        spinnerDay.adapter = adapterDay
        spinnerMonth.adapter = adapterMonth
        spinnerYear.adapter = adapterYear

        spinnerDay.setSelection(0)
        spinnerMonth.setSelection(0)
        spinnerYear.setSelection(100)

        val reg = findViewById<Button>(R.id.buttonSignup)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val nameField = findViewById<EditText>(R.id.editTextNome)
        val surnameField = findViewById<EditText>(R.id.editTextCognome)
        val phoneField = findViewById<EditText>(R.id.editTextTelefono)

        reg.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
            reg.startAnimation(animation)
            val dateOfBirth = spinnerDay.getSelectedItem().toString() + "/" +
                              spinnerMonth.getSelectedItem().toString() + "/" +
                              spinnerYear.getSelectedItem().toString()
            val name = nameField.text.toString()
            val surname = surnameField.text.toString()
            val phone = phoneField.text.toString()
            val email = emailField.text.toString()
            val password = generateRandomPassword()
            var block = false
            var exist: Boolean
            exist = false
            if (name.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Nome non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            } else if (surname.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Cognome non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            } else if (email.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Email non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            else if (phone.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Numero di Telefono non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
                }

            if(!block)
            {
                runBlocking {
                    exist = existsInDB("Utenti", email)
                }

                if(exist)
                {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Registrazione Fallita")
                    builder.setMessage("La tua registrazione non è avvenuta con successo, potrebbe esistere già un account con questa mail, in caso Accedi!")
                    builder.setPositiveButton("OK",null)
                    val dialog = builder.create()
                    dialog.show()
                }
                else{
                    db.collection("Utenti").document(email).set(
                        mapOf
                            (
                            "Nome" to name,
                            "Cognome" to surname,
                            "Telefono" to phone,
                            "Password" to hashString(password),
                            "DDN" to dateOfBirth
                        )
                    ).addOnSuccessListener {
                        rawJSON(email, "" +
                                "Gentile Cliente $name $surname, \n" +
                                "Siamo lieti di darle il benvenuto alla nostra applicazione. La sua registrazione è stata completata con successo. \n" +
                                "Di seguito troverà le credenziali necessarie per accedere al suo account:\n" +
                                "Email: $email \n" +
                                "Password: $password \n" +
                                "Grazie per aver scelto di unirsi a noi. Per qualsiasi domanda o assistenza, non esiti a contattarci. \n" +
                                "Cordiali saluti, \n" +
                                "EventLink"
                        )
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Registrazione Avvenuta")
                        builder.setMessage("La tua registrazione è avvenuta con successo!\nControlla la mail per ottenere la password.")
                        builder.setPositiveButton("OK"){_, _ ->
                            finish()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }.addOnFailureListener{
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Registrazione Fallita")
                        builder.setMessage("La tua registrazione non è avvenuta con successo, potresti avere già un account, in caso Accedi!")
                        builder.setPositiveButton("OK", null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }
    }
    private fun generateRandomPassword(): String {
        val length  = 12
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
    private suspend fun existsInDB(collectionName: String, documentId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val document= db.collection(collectionName).document(documentId).get().await()
            document!=null && document.exists()
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun rawJSON(email : String, text : String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("text", text)
        val jsonObjectString = jsonObject.toString()
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://us-central1-eventlinkv2.cloudfunctions.net/handlePostRequest")
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")
            httpsURLConnection.setRequestProperty("Accept", "application/json")
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            outputStreamWriter.write(jsonObjectString)
            outputStreamWriter.flush()
            val responseCode = httpsURLConnection.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }
                withContext(Dispatchers.Main) {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.d("Pretty Printed JSON :", prettyJson)
                }
            } else {
                Log.e("HTTPSURLCONNECTION_ERROR", responseCode.toString())
            }
        }
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

            val emailField = findViewById<EditText>(R.id.editTextEmailLogin)
            val passwordField = findViewById<EditText>(R.id.editTextPasswordLogin)
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            var auth: Boolean
            auth = false
            var block = false

            if (email.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Email non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            } else if (password.isEmpty()) {
                block = true
                AlertDialog.Builder(this)
                    .setTitle("Campo Vuoto")
                    .setMessage("Il campo Password non può essere vuoto.")
                    .setPositiveButton("OK", null)
                    .show()
            }

            if(!block){
                runBlocking {
                    auth = passwordCheck(email, password)
                }

                if(auth) {
                    val intent = Intent(this@PaginaLogin, PaginaProfilo::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)

                }
                else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Accesso Negato")
                    builder.setMessage("Email o Password errati!")
                    builder.setPositiveButton("OK", null)
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }

        val settingsViewLog = findViewById<LinearLayout>(R.id.ImpostazioniLoginComparsa)
        settingsViewLog.visibility = View.GONE

        val showHideSettingsLog = findViewById<ImageButton>(R.id.button_menu_log)
        showHideSettingsLog.setOnClickListener {
            if (settingsViewLog.visibility == View.VISIBLE) {
                settingsViewLog.visibility = View.GONE
            } else {
                settingsViewLog.visibility = View.VISIBLE
            }
        }

        val backButton = findViewById<ImageButton>(R.id.tornaInDietroLog)
        backButton.setOnClickListener{
            finish()
        }

        val settingsButtonLog = findViewById<Button>(R.id.impostazioniLogin)
        val contactsButtonLog = findViewById<Button>(R.id.contattiLogin)
        val helpButtonLog = findViewById<Button>(R.id.aiutoLogin)

        settingsButtonLog.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaImpostazioni::class.java)
            startActivity(intent)
        }
        contactsButtonLog.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaContatti::class.java)
            startActivity(intent)
        }
        helpButtonLog.setOnClickListener {
            val intent = Intent(this@PaginaLogin, PaginaAiuto::class.java)
            startActivity(intent)
        }
    }
    private suspend fun passwordCheck(documentId: String, password: String): Boolean {
        return try {
            val documentSnapshot = db.collection("Utenti").document(documentId).get().await()
            val documentData = documentSnapshot.data
            val documentPassword = documentData?.get("Password")
            documentPassword == hashString(password)
        }catch (e: Exception){
            false
        }
    }
}
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
fun hashString(input: String): String {
    val bytes = input.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}
class MyClusterItem(
    @JvmField val pos: LatLng,
    @JvmField val title: String,
    @JvmField val desc: String,
    val icon: BitmapDescriptor,
    val tag: String
) : ClusterItem{
    override fun getPosition(): LatLng {
        return pos
    }
    override fun getTitle(): String {
        return title
    }
    override fun getSnippet(): String {
        return desc
    }
}
class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MyClusterItem>
) : DefaultClusterRenderer<MyClusterItem>(context,map,clusterManager){
    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        markerOptions.icon(item.icon)
    }
    override fun onClusterItemRendered(clusterItem: MyClusterItem, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem.tag
    }
}
