package com.example.calendar

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.RoomDatabase.EventEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun Day(
    modifier: Modifier = Modifier,
    day: LocalDate,
    selectedDayEvents: List<EventEntity>,
) {
    val hours = remember { (0..23).toList() }
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        items(hours) { hour ->

            val eventsForThisHour = selectedDayEvents.filter { event ->
                parseHour(event.startstime) == hour
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val amPm = if (hour < 12) "AM" else "PM"
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$displayHour $amPm",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                val context = LocalContext.current

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            val intent = Intent(context, EventActivity::class.java)
                            val startHour = hour
                            val endHour = if (hour == 23) 0 else hour + 1
                            intent.putExtra(
                                "date",
                                day.format(DateTimeFormatter.ofPattern("d/M/yyyy (EEE)"))
                            )
                            intent.putExtra("startHour", startHour)
                            intent.putExtra("endHour", endHour)
                            intent.putExtra(
                                "timeRange",
                                "${formatHour(startHour)} to ${formatHour(endHour)}"
                            )
                            context.startActivity(intent)
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val midY = size.height / 2
                        drawLine(
                            color = Color.LightGray.copy(0.4f),
                            start = Offset(0f, midY),
                            end = Offset(size.width, midY),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color.LightGray.copy(0.6f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color.LightGray.copy(0.6f),
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        eventsForThisHour.forEach { event ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(0.dp)
                                    .background(Color(event.color).copy(.3f))
                                    .border(.1.dp, color = Color.Black)
                                    .clickable {
                                        val intent = Intent(context, EventActivity::class.java)
                                        intent.putExtra("eventId", event.id)
                                        intent.putExtra("title", event.title ?: "")
                                        intent.putExtra("startstime", event.startstime)
                                        intent.putExtra("endtime", event.endtime)
                                        intent.putExtra(
                                            "date",
                                            day.format(DateTimeFormatter.ofPattern("d/M/yyyy (EEE)"))
                                        )
                                        context.startActivity(intent)
                                    },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = event.title ?: "",
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${event.startstime} - ${event.endtime}",
                                        fontSize = 10.sp,
                                        color = Color.Black.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    val now = LocalTime.now()
                    if (now.hour == hour) {
                        val progress = now.minute / 60f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(progress)
                                .align(Alignment.TopCenter)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.BottomCenter)
                            ) {
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 2.dp.toPx()
                                )
                                drawCircle(
                                    color = Color.Red,
                                    radius = 5.dp.toPx(),
                                    center = Offset(0f, size.height)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseHour(time: String?): Int? {
    if (time.isNullOrBlank()) return null

    val t = time.trim().uppercase()

    return try {
        if (t.contains("AM") || t.contains("PM")) {
            val parts = t.split(" ")
            val hourPart = parts[0]
            val amPm = parts[1]

            val hourStr = if (hourPart.contains(":")) {
                hourPart.split(":")[0]
            } else {
                hourPart
            }
            val hour = hourStr.toInt()

            when {
                amPm == "AM" && hour == 12 -> 0
                amPm == "AM" -> hour
                amPm == "PM" && hour == 12 -> 12
                else -> hour + 12
            }
        }
        // Case: "14:00"
        else if (t.contains(":")) {
            t.split(":")[0].toInt()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun formatHour(hour: Int): String {
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    val amPm = if (hour < 12) "AM" else "PM"
    return String.format("%02d:00 %s", displayHour, amPm)
}