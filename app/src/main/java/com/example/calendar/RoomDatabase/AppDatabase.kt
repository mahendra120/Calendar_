package com.example.calendar.RoomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CalendarEntity::class, EventEntity::class, RegionEntity::class],
    version = 6,
    exportSchema = false   // good to add unless you really need schema files
)
abstract class CalendarDatabase : RoomDatabase() {

    abstract fun calendarDao(): CalendarDao
    abstract fun eventDao(): EventDao
    abstract fun regionDao(): RegionDao

    companion object {
        @Volatile
        private var INSTANCE: CalendarDatabase? = null

        fun getDatabase(context: Context): CalendarDatabase {   // ← Change to Context here
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,          // ← now compiles & is safe
                    CalendarDatabase::class.java,
                    "calendar_db"
                )
                    .fallbackToDestructiveMigration()        // ← only for development!
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}