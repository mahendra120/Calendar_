package com.example.calendar.RoomDatabase

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = CalendarEntity::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("calendarId")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String?,
    val date: String,
    val startstime: String?,
    val endtime: String?,
    val calendarId: Int,
    val color: Int = 0
)


@Entity(tableName = "calendar")
data class CalendarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: Int
)

@Entity(tableName = "regions")
data class RegionEntity(
    @PrimaryKey
    val code: String,   // "IN", "US", "UK"
    val name: String    // "India"
)


