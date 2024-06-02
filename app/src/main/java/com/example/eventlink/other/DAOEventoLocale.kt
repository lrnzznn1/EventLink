package com.example.eventlink.other
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// Interfaccia per le operazioni CRUD su EventoLocale
@Dao
interface DAOEventoLocale {

    // Inserisce un nuovo evento locale nel database
    @Insert
    suspend fun insert(eventoLocale: EventoLocale)

    // Elimina un evento locale dal database
    @Delete
    suspend fun delete(eventoLocale: EventoLocale)

    // Ottiene tutti gli eventi locali presenti nel database
    @Query("SELECT * FROM EventoLocale")
    suspend fun getAllEvent(): List<EventoLocale>

    // Verifica se un evento con l'ID specificato esiste nel database
    @Query("SELECT COUNT(*) FROM EventoLocale WHERE ID_Evento = :id")
    suspend fun doesEventExist(id: String): Boolean
}