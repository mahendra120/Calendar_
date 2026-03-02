//package com.example.calendar
//
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.calendar.RoomDatabase.CalendarDatabase
//import kotlinx.coroutines.launch
//
//class EventViewModel(private val db: CalendarDatabase) : ViewModel() {
//
//    fun insertEvent(event: EventEntity) {
//        viewModelScope.launch {
//            db.eventDao().insertEvent(event)
//        }
//    }
//
//    fun deleteEvent(id: Int) {
//        viewModelScope.launch {
//            db.eventDao().deleteEventById(id)
//        }
//    }
//
//    val eventsByDate = db.eventDao().getEventsGroupedByDate()
//}