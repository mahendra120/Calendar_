package com.example.calendar.RoomDatabase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateSelectionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = CalendarDatabase.getDatabase(application)

    fun saveSelectedDates(
        dates: Set<LocalDate>,
        color: Int
    ) {
        viewModelScope.launch {

            val calendarId = db.calendarDao().getOrCreateCalendarId()

            dates.forEach { date ->
                db.eventDao().insertEvent(
                    EventEntity(
                        title = "Selected Date",
                        date = date.format(
                            DateTimeFormatter.ofPattern("d/M/yyyy (EEE)")
                        ),
                        startstime = null,
                        endtime = null,
                        calendarId = calendarId,
                        color = color
                    )
                )
            }
        }
    }
}