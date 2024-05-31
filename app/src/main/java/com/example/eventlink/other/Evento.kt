package com.example.eventlink.other

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng

class Evento(
    var ID_Evento: String = "null",
    var Data : String = "null",
    var Descrizione : String = "null",
    var ID_Azienda : String = "null",
    var Immagine : String = "null",
    var Indirizzo : String = "null",
    var Max_Prenotazioni : String = "null",
    var Ora : String = "null",
    var Posizione : String = "null",
    var Prenotazione : String = "null",
    var Prezzo : String = "null",
    var Tipo : String = "null",
    var Titolo : String = "null",
    var Descrizione_Mappa : String = "null",
    var Immagine_Mappa : BitmapDescriptor,
    var Posizione_Mappa : LatLng
) {
    fun Evento(ID_Evento: String, Data : String, Descrizione : String, ID_Azienda : String, Immagine : String, Indirizzo : String, Max_Prenotazioni : String, Ora : String, Posizione : String, Prenotazione : String, Prezzo : String, Tipo : String, Titolo : String, Descrizione_Mappa: String, Immagine_Mappa: BitmapDescriptor, Posizione_Mappa: LatLng){
        this.ID_Evento = ID_Evento
        this.Data = Data
        this.Descrizione = Descrizione
        this.ID_Azienda = ID_Azienda
        this.Immagine = Immagine
        this.Indirizzo = Indirizzo
        this.Max_Prenotazioni = Max_Prenotazioni
        this.Ora = Ora
        this.Posizione = Posizione
        this.Prenotazione = Prenotazione
        this.Prezzo = Prezzo
        this.Tipo = Tipo
        this.Titolo = Titolo
        this.Descrizione_Mappa = Descrizione_Mappa
        this.Immagine_Mappa = Immagine_Mappa
        this.Posizione_Mappa = Posizione_Mappa
    }

    fun Evento(ID_Evento: String, Data : String, Descrizione : String, ID_Azienda : String, Immagine : String, Indirizzo : String, Max_Prenotazioni : String, Ora : String, Posizione : String, Prenotazione : String, Prezzo : String, Tipo : String, Titolo : String){
        this.ID_Evento = ID_Evento
        this.Data = Data
        this.Descrizione = Descrizione
        this.ID_Azienda = ID_Azienda
        this.Immagine = Immagine
        this.Indirizzo = Indirizzo
        this.Max_Prenotazioni = Max_Prenotazioni
        this.Ora = Ora
        this.Posizione = Posizione
        this.Prenotazione = Prenotazione
        this.Prezzo = Prezzo
        this.Tipo = Tipo
        this.Titolo = Titolo
    }
}