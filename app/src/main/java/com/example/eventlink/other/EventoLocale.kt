package com.example.eventlink.other
import androidx.room.Entity
import androidx.room.PrimaryKey

// Definisce una classe di dati per rappresentare un evento locale nel database Room
@Entity(tableName="EventoLocale")
data class EventoLocale(
    // Chiave primaria della tabella EventoLocale, che identifica univocamente un evento
    @PrimaryKey var ID_Evento: String = "null"
)