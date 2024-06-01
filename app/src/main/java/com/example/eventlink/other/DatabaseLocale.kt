@file:OptIn(InternalCoroutinesApi::class)

package com.example.eventlink.other

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized


@Database(entities = [EventoLocale::class], version = 3)
abstract class DatabaseLocale : RoomDatabase(){
    abstract  fun DAOEventoLocale():DAOEventoLocale

    companion object{
        @Volatile
        private var INSTANCE : DatabaseLocale? = null

        fun getInstance(context: Context): DatabaseLocale{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseLocale::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                INSTANCE= instance
                instance
            }
        }


        fun clearAllTables(){
            INSTANCE?.clearAllTables()
        }

    }
}