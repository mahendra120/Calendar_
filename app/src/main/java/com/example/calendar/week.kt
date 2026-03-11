package com.example.calendar

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.RoomDatabase.EventEntity
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekCalendar(
    eventsMap: Map<LocalDate, List<EventEntity>> = emptyMap(),
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    selectedDayEvents: List<EventEntity>
) {
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    fun parseEventTimeString(timeStr: String?): LocalTime? {
        if (timeStr.isNullOrBlank()) return null
        val t0 = timeStr.trim().replace(Regex("(?i)\\s*(am|pm)\$"), " $1")
        val candidates = listOf(
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH)
        )
        for (fmt in candidates) {
            try {
                return LocalTime.parse(t0, fmt)
            } catch (_: DateTimeParseException) {
            }
        }
        // last resort: try parsing hour only
        try {
            val hourOnly = t0.split(":")[0].filter { it.isDigit() }
            if (hourOnly.isNotBlank()) {
                val h = hourOnly.toInt()
                return LocalTime.of((h % 24), 0)
            }
        } catch (_: Exception) {
        }
        return null
    }

    val eventsByDay: Map<LocalDate, List<Event>> = weekDays.associateWith { date ->
        // eventsMap already keyed by LocalDate (important). If not, fix upstream.
        (eventsMap[date] ?: emptyList()).mapNotNull { entity ->
            val start = parseEventTimeString(entity.startstime)
            val end = parseEventTimeString(entity.endtime)
            if (start == null || end == null) {
                // skip malformed time entries (or you may provide defaults)
                Log.d(
                    "WEEK_DEBUG",
                    "Skipping event (bad time): ${entity.title} ${entity.startstime} ${entity.endtime}"
                )
                null
            } else {
                Event(
                    title = entity.title ?: "",
                    startTime = start,
                    endTime = end,
                    color = entity.color,
                    note = entity.note ?: "",
                    location = entity.location ?: "",
                    url = entity.url ?: "",
                )
            }
        }
    }
    LaunchedEffect(eventsMap, selectedDate) {
        Log.d("WEEK_DEBUG", "selectedDate=$selectedDate, weekDays=$weekDays")
        weekDays.forEach { d ->
            Log.d("WEEK_DEBUG", "$d -> ${eventsByDay[d]?.size ?: 0} events")
        }
    }

    val hourHeight = 48.dp
    val timeColumnWidth = 68.dp
    val dayColumnWidth = 78.dp
    val density = LocalDensity.current
    val nowForHighlight by rememberUpdatedState(newValue = LocalTime.now())
    Column(modifier = modifier.fillMaxSize()) {
        // Header row (days)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .background(Color(0xFFF5F5F5))
        ) {
            Spacer(modifier = Modifier.width(timeColumnWidth))
            weekDays.forEach { date ->
                val isSelected = date == selectedDate

                Box(
                    modifier = Modifier
                        .width(dayColumnWidth)
                        .height(56.dp)
                        .border(0.5.dp, Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.getDefault()
                            ),
                            fontSize = 12.sp,
                            color = if (date.dayOfWeek == DayOfWeek.SUNDAY) Color.Red else Color.Gray
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF1976D2) else Color.Black
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            TimeColumn(
                hourHeight = hourHeight,
                modifier = Modifier
                    .width(timeColumnWidth)
                    .verticalScroll(verticalScroll)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .verticalScroll(verticalScroll)
            ) {
                weekDays.forEach { date ->
                    DayColumn(
                        date = date,
                        events = eventsByDay[date] ?: emptyList(),
                        hourHeight = hourHeight,
                        dayWidth = dayColumnWidth,
                        density = density
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(hourHeight: Dp, modifier: Modifier = Modifier) {
    val hours = (0..23).toList()
    Column(modifier = modifier.background(Color.White)) {
        hours.forEach { hour ->
            val displayHour = if (hour % 12 == 0) 12 else hour % 12
            val amPm = if (hour < 12) "AM" else "PM"
            Box(
                modifier = Modifier
                    .height(hourHeight)
                    .fillMaxWidth()
                    .border(0.3.dp, Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$displayHour $amPm", fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun DayColumn(
    date: LocalDate,
    events: List<Event>,
    hourHeight: Dp,
    dayWidth: Dp,
    density: Density
) {
    val totalHeight = hourHeight * 24
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(dayWidth)
            .height(totalHeight)
            .background(
                if (date == LocalDate.now())
                    Color(0xFFFFDA9C).copy(alpha = 0.15f)
                else Color.Transparent
            )
            .border(0.5.dp, Color.LightGray.copy(alpha = 0.6f))
            .pointerInput(Unit) {
                detectTapGestures { offset ->

                    val hourHeightPx = with(density) { hourHeight.toPx() }
                    val hourIndex = (offset.y / hourHeightPx).toInt()

                    val startHour = hourIndex
                    val endHour = hourIndex + 1

                    val startTime = LocalTime.of(startHour, 0)
                    val endTime = LocalTime.of(endHour % 24, 0)

                    val formatter = DateTimeFormatter.ofPattern("hh:00 a")

                    val intent = Intent(context, EventActivity::class.java)

                    intent.putExtra("startstime", startTime.format(formatter))
                    intent.putExtra("endtime", endTime.format(formatter))

                    intent.putExtra(
                        "date",
                        date.format(DateTimeFormatter.ofPattern("d/M/yyyy (EEE)"))
                    )

                    context.startActivity(intent)
                }
            }
    ) {
        Column {
            repeat(24) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight)
                        .border(0.5.dp, Color.LightGray.copy(alpha = 0.25f))
                )
            }
        }

        if (date == LocalDate.now()) {
            val now = LocalTime.now()
            val minutesFromMidnight = now.hour * 60 + now.minute
            val offsetY = with(density) { (minutesFromMidnight * hourHeight.toPx() / 60f).toDp() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY)
                    .height(2.5.dp)
                    .background(Color.Red)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (-5).dp)
                        .background(Color.Red, CircleShape)
                )
            }
        }

        events.forEach { event ->
            val startMinutes = event.startTime.hour * 60 + event.startTime.minute
            var durationMinutes =
                Duration.between(event.startTime, event.endTime).toMinutes().toInt()
            if (durationMinutes <= 0) durationMinutes = 30 // minimum 30 minutes if end <= start

            val topOffset = with(density) { (startMinutes * hourHeight.toPx() / 60f).toDp() }
            val eventHeight = with(density) { (durationMinutes * hourHeight.toPx() / 60f).toDp() }
            val context = LocalContext.current

            Card(
                onClick = {
                    val intent = Intent(context, EventActivity::class.java)
                    intent.putExtra("title", event.title)
                    intent.putExtra("startstime", event.startTime)
                    intent.putExtra("endtime", event.endTime)
                    intent.putExtra("note", event.note)
                    intent.putExtra("url", event.url)
                    intent.putExtra("location", event.location)
                    intent.putExtra("date", date.toString())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = topOffset)
                    .padding(start = 5.dp, end = 5.dp, top = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(event.color)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        text = event.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class Event(
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val color: Int,
    val note: String,
    val location: String,
    val url: String,
)