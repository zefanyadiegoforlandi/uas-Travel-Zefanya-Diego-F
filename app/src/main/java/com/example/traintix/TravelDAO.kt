package com.example.traintix

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TravelDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(travel: TravelDB)

    @Update
    fun update(travel: TravelDB)

    @Delete
    fun delete(travel: TravelDB)

    @get:Query("SELECT * from travel ORDER BY id ASC")
    val allTravels: LiveData<List<TravelDB>>
}