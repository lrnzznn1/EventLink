package com.example.eventlink.other
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="EventoLocale")
data class EventoLocale(
    @PrimaryKey var ID_Evento: String = "null"
)