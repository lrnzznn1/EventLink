package com.example.eventlink.other
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface DAOEventoLocale {

    @Insert
    suspend fun insert(eventoLocale: EventoLocale)

    @Query("SELECT * FROM EventoLocale")
    suspend fun getAllEvent(): List<EventoLocale>
}
