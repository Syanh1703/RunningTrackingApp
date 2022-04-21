package com.example.runningtrackingapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Run::class], version = 1)//Get the data from Run
@TypeConverters(Converters::class)//Remind Room to use TypeConverter
abstract class RunningDatabase:RoomDatabase() {
    abstract fun getRunDao():RunDAO
}