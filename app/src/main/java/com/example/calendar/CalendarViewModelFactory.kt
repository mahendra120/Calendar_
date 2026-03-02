package com.example.calendar

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calendar.RoomDatabase.CalendarDatabase
import com.example.calendar.RoomDatabase.EventViewModel

class EventViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(application) as T
        }
        // Added support for CalendarViewModel
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(CalendarDatabase.getDatabase(application).calendarDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}