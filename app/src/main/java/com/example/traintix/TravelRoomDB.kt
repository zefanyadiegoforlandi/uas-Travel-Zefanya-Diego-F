package com.example.traintix

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TravelDB::class], version = 1, exportSchema = false)
abstract class TravelRoomDB : RoomDatabase() {

    abstract fun travelDao() : TravelDAO?

    companion object {
        @Volatile
        private var INSTANCE: TravelRoomDB? = null

        fun getDatabase(context : Context) : TravelRoomDB ? {
            if (INSTANCE == null){
                synchronized(TravelRoomDB::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext, TravelRoomDB::class.java,
                        "travel_database"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}