package com.example.traintix

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel")
data class TravelDB(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "departure")
    val departure: String,

    @ColumnInfo(name = "destination")
    val destination : String,

    @ColumnInfo(name = "train")
    val train : String,

    @ColumnInfo(name = "price")
    val price : Int

)
