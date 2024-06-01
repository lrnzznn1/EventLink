package com.example.eventlink.other
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DAOEventoLocale {

    @Insert
    suspend fun insert(eventoLocale: EventoLocale)

    @Delete
    suspend fun delete(eventoLocale: EventoLocale)

    @Query("SELECT * FROM EventoLocale")
    suspend fun getAllEvent(): List<EventoLocale>

    @Query("SELECT COUNT(*) FROM EventoLocale WHERE ID_Evento = :id")
    suspend fun doesEventExist(id: String): Boolean
}
