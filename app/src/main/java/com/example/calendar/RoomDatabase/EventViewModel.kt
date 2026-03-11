package com.example.calendar.RoomDatabase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CalendarDatabase.getDatabase(application)

    private val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy (EEE)")

    // 👇 Now suspend (no internal launch) – caller will await this
    suspend fun saveEvent(
        title: String?,
        date: String,
        startstime: String?,
        endtime: String?,
        color: Int,
        location: String?,
        url: String?,
        note: String?
    ) {
        val calendarId = db.calendarDao().getOrCreateCalendarId()
        db.eventDao().insertEvent(
            EventEntity(
                title = title,
                date = date,
                startstime = startstime,
                endtime = endtime,
                calendarId = calendarId,
                color = color,
                location = location,
                url = url,
                note = note
            )
        )
    }

    // 👇 Suspend
    suspend fun updateEvent(event: EventEntity) {
        db.eventDao().updateEvent(event)
    }

    // 👇 Suspend
    suspend fun deleteEvent(eventId: Int) {
        db.eventDao().deleteEventById(eventId)
    }

    // Events flow remains same (no change needed)
    val eventsByDate: Flow<Map<LocalDate, List<EventEntity>>> =
        db.eventDao().getAllEvents().map { events ->
            events
                .mapNotNull { entity ->
                    try {
                        val parsedDate = LocalDate.parse(entity.date, dateFormatter)
                        parsedDate to entity
                    } catch (e: DateTimeParseException) {
                        null
                    }
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second }
                )
        }
}