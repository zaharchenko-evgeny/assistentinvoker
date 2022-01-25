package ru.zaharchenko.assistentinvoker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.zaharchenko.assistentinvoker.model.BtDevice

@Database(entities = [BtDevice::class], version = 1, exportSchema = false)
public abstract class BtDeviceRoomDatabase : RoomDatabase() {

    abstract fun btDeviceDao(): BtDeviceDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: BtDeviceRoomDatabase? = null

        fun getDatabase(context: Context): BtDeviceRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BtDeviceRoomDatabase::class.java,
                    "bt_device_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}