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

    fun saveEvent(
        title: String?,
        date: String,
        startstime: String?,
        endtime: String?,
        color: Int
    ) {
        viewModelScope.launch {
            val calendarId = db.calendarDao().getOrCreateCalendarId()
            db.eventDao().insertEvent(
                EventEntity(
                    title = title,
                    date = date,
                    startstime = startstime,
                    endtime = endtime,
                    calendarId = calendarId,
                    color = color
                )
            )
        }
    }

    fun deleteEvent(eventId: Int) {
        viewModelScope.launch {
            db.eventDao().deleteEventById(eventId)
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            db.eventDao().updateEvent(event)
        }
    }

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