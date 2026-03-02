package com.example.calendar

import com.example.calendar.RoomDatabase.CalendarDatabase
import com.example.calendar.RoomDatabase.CalendarEntity

suspend fun ensureDefaultCalendar(db: CalendarDatabase): Int {
    val existingId = db.calendarDao().getAnyCalendarId()
    if (existingId != null) return existingId

    return db.calendarDao().insertCalendar(
        CalendarEntity(
            name = "My Calendar",
            color = 0xFFFF9800.toInt()
        )
    ).toInt()
}