package com.example.runningtrackingapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter //To tell Room that this is the type converter it needs
    fun fromBitmap(bitmap:Bitmap):ByteArray{//Convert the bitmap value to byte array to store in the database
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray):Bitmap //Convert the byte array value to bitmap
    {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

}