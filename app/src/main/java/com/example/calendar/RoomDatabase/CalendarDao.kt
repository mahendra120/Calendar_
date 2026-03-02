package com.example.calendar.RoomDatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCalendar(calendar: CalendarEntity): Long
    // 👆 id return કરશે

    @Query("SELECT * FROM calendar")
    fun getAllCalendars(): Flow<List<CalendarEntity>>

    @Query("SELECT id FROM calendar LIMIT 1")
    suspend fun getAnyCalendarId(): Int?


    suspend fun getOrCreateCalendarId(): Int {
        val id = getAnyCalendarId()
        if (id != null) return id

        val newId = insertCalendar(
            CalendarEntity(
                name = "Local Calendar",
                color = 0xFFFF9800.toInt()
            )
        )
        return newId.toInt()
    }


}


@Dao
interface RegionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRegion(region: RegionEntity)

    @Query("SELECT * FROM regions")
    fun getAllRegions(): Flow<List<RegionEntity>>

    @Query("DELETE FROM regions WHERE code = :code")
    suspend fun deleteRegion(code: String)
}

@Dao
interface EventDao {

    @Insert
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE calendarId = :calendarId")
    fun getEventsByCalendar(calendarId: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>


    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Int)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<EventEntity>)

}

