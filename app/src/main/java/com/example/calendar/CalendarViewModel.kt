package com.example.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendar.RoomDatabase.CalendarDao
import com.example.calendar.RoomDatabase.CalendarEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class CalendarViewModel(
    private val calendarDao: CalendarDao
) : ViewModel() {

    val calendars: StateFlow<List<CalendarEntity>> =
        calendarDao.getAllCalendars()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}