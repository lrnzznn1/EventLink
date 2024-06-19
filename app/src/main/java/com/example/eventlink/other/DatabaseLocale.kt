@file:OptIn(InternalCoroutinesApi::class)

package com.example.eventlink.other

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

// Definisce il database Room con la tabella EventoLocale e la versione del database
@Database(entities = [EventoLocale::class], version = 3)
abstract class DatabaseLocale : RoomDatabase(){

    // Definisce il DAO per accedere ai dati di EventoLocale
    abstract  fun DAOEventoLocale():DAOEventoLocale

    companion object{
        // Istanza volatile del database, garantisce visibilità delle modifiche tra thread
        @Volatile
        private var INSTANCE : DatabaseLocale? = null

        // Ottiene l'istanza del database, creando il database se non esiste già
        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): DatabaseLocale{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseLocale::class.java,
                    "app_database")
                    // Permette migrazioni distruttive
                    .fallbackToDestructiveMigration()
                    // Permette query nel thread principale
                    .allowMainThreadQueries().build()
                INSTANCE= instance
                instance
            }
        }

        // Cancella tutte le tabelle nel database
        fun clearAllTables(){
            INSTANCE?.clearAllTables()
        }
    }
}