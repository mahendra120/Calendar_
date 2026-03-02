package com.example.calendar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.RoomDatabase.EventEntity
import com.example.calendar.RoomDatabase.EventViewModel
import com.example.calendar.RoomDatabase.colosave.loadThemeColor
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {
    var month_week_day_cheng by mutableStateOf(0)
    var selectedColor by mutableStateOf(Color.White)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var hasCalendarPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_CALENDAR
                    ) ==
                            PackageManager.PERMISSION_GRANTED
                )
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasCalendarPermission = granted
            }
            if (!hasCalendarPermission) {
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                }
            }

            val eventViewModel: EventViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    // We only need to pass the Application (this is what EventViewModel expects)
                    return EventViewModel(this@MainActivity.application) as T
                }
            })

            val eventsMap by eventViewModel.eventsByDate.collectAsState(initial = emptyMap())
            val today = remember { LocalDate.now() }
            val currentMonth = remember { YearMonth.now() }

            val scope = rememberCoroutineScope()
            var selectedDate by remember { mutableStateOf(LocalDate.now()) }

            val calendarState = rememberCalendarState(
                startMonth = currentMonth.minusMonths(12),
                endMonth = currentMonth.plusMonths(12),
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = DayOfWeek.MONDAY
            )


            if (hasCalendarPermission) {
                Scaffold(
                    containerColor = Color(255, 255, 255),
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MonthHeader(
                            month = calendarState.firstVisibleMonth.yearMonth,
                            selectedDate = selectedDate,
                            month_week_day_cheng = month_week_day_cheng
                        )
                    },
                    bottomBar = {
                        var selectedTab by remember {
                            mutableStateOf(BottomTab.MONTH)
                        }
                        BottomBar(selected = selectedTab, onTodayClick = {
                            scope.launch {
                                calendarState.animateScrollToMonth(
                                    YearMonth.from(today)
                                )
                            }
                        }, onMonthClick = {
                            selectedTab = BottomTab.MONTH
                            month_week_day_cheng = 0
                        }, onWeekClick = {
                            selectedTab = BottomTab.WEEK
                            month_week_day_cheng = 1
//                            selectedDate = LocalDate.now()
                        }, onDayClick = {
                            selectedTab = BottomTab.DAY
                            month_week_day_cheng = 2
                        })
                    })
                { innerPadding ->
                    val selectedDayEvents = eventsMap[selectedDate] ?: emptyList()
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        when (month_week_day_cheng) {
                            0 -> CalendarScreen(
                                calendarState = calendarState,
                                selectedDate = selectedDate,
                                onDateSelected = { newDate ->
                                    selectedDate = newDate
                                },
                                eventsMap = eventsMap,
                                eventViewModel = eventViewModel
                            )

                            1 -> WeekCalendar(
                                eventsMap = eventsMap,
                                selectedDate = selectedDate,
                                selectedDayEvents = selectedDayEvents,
                                modifier = Modifier.fillMaxSize()
                            )

                            2 -> Day(
                                modifier = Modifier.fillMaxSize(),
                                day = selectedDate,
                                selectedDayEvents = selectedDayEvents,
                            )
                        }
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Calendar permission required")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CALENDAR) }) {
                        Text("Grant permission")
                    }
                }
            }
        }
    }


    data class CalendarHoliday(
        val date: LocalDate, val title: String
    )

    @Composable
    fun CalendarPermissionScreen(
        calendarState: CalendarState,
        eventsMap: Map<LocalDate, List<EventEntity>>,
        eventViewModel: EventViewModel
    ) {
        val context = LocalContext.current
        var permissionGranted by remember { mutableStateOf(false) }
        var permissionDenied by remember { mutableStateOf(false) }

        var selectedDate by remember { mutableStateOf(LocalDate.now()) }

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                permissionGranted = granted
                permissionDenied = !granted
            }

        LaunchedEffect(Unit) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) permissionGranted = true
            else launcher.launch(Manifest.permission.READ_CALENDAR)
        }

        when {
            permissionGranted -> {
                Column(modifier = Modifier.fillMaxSize()) {

                    CalendarScreen(
                        calendarState = calendarState,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        eventsMap = eventsMap,
                        eventViewModel = eventViewModel
                    )

                    var selectedTab by remember {
                        mutableStateOf(BottomTab.DAY)
                    }

                    BottomBar(
                        selected = selectedTab, onTodayClick = {
                            selectedDate = LocalDate.now()
                        })
                }
            }

            permissionDenied -> PermissionDeniedUI {
                launcher.launch(Manifest.permission.READ_CALENDAR)
            }

            else -> LoadingUI()
        }
    }

    val indiaHolidayMap: Map<LocalDate, String> = mapOf(
        LocalDate.of(2025, 1, 26) to "Republic Day",
        LocalDate.of(2025, 8, 15) to "Independence Day",
        LocalDate.of(2025, 10, 2) to "Gandhi Jayanti",
        LocalDate.of(2025, 1, 14) to "Makar Sankranti / Pongal",
        LocalDate.of(2025, 2, 26) to "Maha Shivaratri",
        LocalDate.of(2025, 3, 14) to "Holi",
        LocalDate.of(2025, 3, 30) to "Ram Navami / Ugadi / Gudi Padwa",
        LocalDate.of(
            2025, 3, 31
        ) to "Eid-ul-Fitr (Id-ul-Fitr)",          // common date; moon sighting dependent
        LocalDate.of(2025, 4, 10) to "Mahavir Jayanti",
        LocalDate.of(2025, 4, 14) to "Dr. B.R. Ambedkar Jayanti",
        LocalDate.of(2025, 4, 18) to "Good Friday",
        LocalDate.of(2025, 5, 12) to "Buddha Purnima",
        LocalDate.of(2025, 6, 7) to "Eid-ul-Adha (Bakrid / Id-ul-Zuha)", // approximate
        LocalDate.of(2025, 7, 6) to "Muharram",                          // approximate
        LocalDate.of(2025, 8, 9) to "Raksha Bandhan",
        LocalDate.of(2025, 8, 16) to "Janmashtami (Krishna Janmashtami)",
        LocalDate.of(2025, 9, 5) to "Ganesh Chaturthi / Vinayaka Chaturthi",
        LocalDate.of(2025, 10, 20) to "Diwali (Deepavali / Laxmi Puja)",
        LocalDate.of(2025, 10, 23) to "Bhai Dooj",
        LocalDate.of(2025, 11, 5) to "Guru Nanak Jayanti",
        LocalDate.of(2025, 12, 25) to "Christmas Day",
        LocalDate.of(2025, 1, 1) to "New Year's Day",
        LocalDate.of(2025, 2, 2) to "Vasant Panchami / Saraswati Puja",
        LocalDate.of(2025, 5, 1) to "International Labour Day (May Day)",
        LocalDate.of(2025, 10, 31) to "Sardar Vallabhbhai Patel Jayanti"
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CalendarScreen(
        calendarState: CalendarState,
        selectedDate: LocalDate?,
        onDateSelected: (LocalDate) -> Unit,
        eventsMap: Map<LocalDate, List<EventEntity>>,
        eventViewModel: EventViewModel
    ) {
        val context = LocalContext.current
        val holidays = remember(context) {
            getIndiaHolidays(
                context, LocalDate.now().minusMonths(12), LocalDate.now().plusMonths(12)
            )
        }
        val holidayMap = remember(holidays) {
            holidays.associateBy { it.date }
        }
        val fixedIndiaHolidayMap = indiaHolidayMap.mapValues {
            CalendarHoliday(it.key, it.value)
        }
        val combinedHolidayMap = remember(fixedIndiaHolidayMap, holidayMap) {
            fixedIndiaHolidayMap + holidayMap
        }
        var expanded by remember { mutableStateOf(false) }

        val height = remember { Animatable(0.dp, Dp.VectorConverter) }

        var selectedHoliday by remember { mutableStateOf<CalendarHoliday?>(null) }

        val selectedDayEvents = selectedDate?.let { eventsMap[it] } ?: emptyList()

        LaunchedEffect(expanded) {
            height.animateTo(
                if (expanded) 250.dp else 0.dp, tween(400, easing = FastOutSlowInEasing)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            DaysOfWeekRow()
            HorizontalCalendar(
                state = calendarState,
                dayContent = { day ->
                    val holiday = combinedHolidayMap[day.date]
                    val dayEvents = eventsMap[day.date] ?: emptyList()
                    DayCell(
                        day = day,
                        isSelected = day.date == selectedDate,
                        holiday = holiday,
                        events = dayEvents,
                        selectedDate = selectedDate,
                        onClick = {
                            if (day.position != DayPosition.MonthDate) return@DayCell
                            onDateSelected(day.date)
                            if (day.date == selectedDate) {
                                expanded = !expanded
                            } else {
                                selectedHoliday = holiday
                                expanded = true
                            }
                        })
                })
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.value)
                    .background(Color(245, 245, 245))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = selectedDate?.format(
                                DateTimeFormatter.ofPattern("d/M/yyyy (EEE)")
                            ) ?: "No date selected",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp
                            else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (selectedHoliday != null) {
                            item {
                                Card(
                                    onClick = {

                                        val intent =
                                            Intent(this@MainActivity, EventActivity::class.java)
                                        intent.putExtra("title", selectedHoliday!!.title)
                                        intent.putExtra(
                                            "date", selectedHoliday!!.date.format(
                                                DateTimeFormatter.ofPattern(
                                                    "d/M/yyyy (EEE)"
                                                )
                                            )
                                        )
                                        intent.putExtra("true", "true")
                                        startActivity(intent)

                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .animateItem(
                                            fadeInSpec = tween(400), placementSpec = tween(500)
                                        ),
                                    shape = RectangleShape,
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(0.5.dp, Color.LightGray)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(top = 10.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "All-day",
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(7.dp)
                                                .padding(vertical = 5.dp)
                                                .fillMaxHeight()
                                                .background(Color(0xFF1A73E8))
                                        )
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = selectedHoliday!!.title,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = selectedHoliday!!.date.format(
                                                    DateTimeFormatter.ofPattern("d MMM yyyy")
                                                ), color = Color.Black, fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedDayEvents.isNotEmpty()) {
                            items(selectedDayEvents) { event ->
                                var showMenuForThisEvent by remember(event.id) {
                                    mutableStateOf(
                                        false
                                    )
                                }
                                var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
                                var anchorBounds by remember { mutableStateOf<Rect?>(null) }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onGloballyPositioned { coordinates ->
                                            anchorBounds = coordinates.boundsInWindow()
                                        }) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(60.dp)
                                            .combinedClickable(onClick = {
                                                Log.d(
                                                    "CalendarScreen",
                                                    "Clicked eventId = ${event.id}"
                                                )
                                                val intent = Intent(
                                                    this@MainActivity, EventActivity::class.java
                                                )
                                                intent.putExtra("title", event.title)
                                                intent.putExtra(
                                                    "date", event.date.format(
                                                        DateTimeFormatter.ofPattern("d/M/yyyy (EEE)")
                                                    )
                                                )
                                                intent.putExtra("eventId", event.id)
                                                intent.putExtra("color", event.color)
                                                intent.putExtra("startstime", event.startstime)
                                                intent.putExtra("endtime", event.endtime)
                                                startActivity(intent)
                                                Log.d(
                                                    "LLLLLLLLLL",
                                                    "CalendarScreen: ${event.startstime} , ${event.endtime}"
                                                )
                                            }, onLongClick = {
                                                showMenuForThisEvent = true
                                            }),
                                        shape = RectangleShape,
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(0.5.dp, Color.LightGray)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(top = 10.dp),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    event.startstime ?: ""
                                                )
                                                Spacer(modifier = Modifier.padding(top = 5.dp))
                                                Text(
                                                    event.endtime ?: ""
                                                )
                                            }
                                            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(7.dp)
                                                    .padding(vertical = 5.dp)
                                                    .fillMaxHeight()
                                                    .background(Color(event.color))
                                            )
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = event.title ?: "kkk",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = event.date.format(
                                                        DateTimeFormatter.ofPattern(
                                                            "d MMM yyyy"
                                                        )
                                                    ), color = Color.Black, fontSize = 12.sp
                                                )
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = showMenuForThisEvent,
                                            onDismissRequest = { showMenuForThisEvent = false },
                                            modifier = Modifier
                                                .width(150.dp)
                                                .background(
                                                    Color.White, RoundedCornerShape(4.dp)
                                                )
                                                .shadow(4.dp),
                                            offset = pressOffset
                                        ) {
                                            DropdownMenuItem(text = { Text("Move") }, onClick = {
                                                showMenuForThisEvent = false
                                            })
                                            DropdownMenuItem(text = { Text("Copy") }, onClick = {
                                                showMenuForThisEvent = false
                                            })
                                            DropdownMenuItem(
                                                text = { Text("Duplicate") },
                                                onClick = {
                                                    showMenuForThisEvent = false
                                                })
                                            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                                eventViewModel.deleteEvent(event.id)
                                            })
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    val intent =
                                        Intent(this@MainActivity, EventActivity::class.java)
                                    intent.putExtra(
                                        "date",
                                        selectedDate?.format(DateTimeFormatter.ofPattern("d/M/yyyy (EEE)"))
                                    )
                                    startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = BorderStroke(0.5.dp, Color.LightGray)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                    Text(
                                        text = " Create new event",
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.padding(vertical = 5.dp))
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun DayCell(
        day: CalendarDay,
        isSelected: Boolean,
        holiday: CalendarHoliday?,
        events: List<EventEntity>,
        selectedDate: LocalDate?,
        onClick: () -> Unit
    ) {

        var selectedColor by remember {
            mutableStateOf(loadThemeColor(this@MainActivity))
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    selectedColor = loadThemeColor(this@MainActivity)
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val darkColor = darkenColor(selectedColor)

        val isCurrentMonth = day.position == DayPosition.MonthDate
        val isToday = day.date == LocalDate.now()

        val backgroundColor = when {
            isToday && isCurrentMonth -> Color(darkColor.toArgb())
            !isCurrentMonth -> Color(235, 235, 235)
            else -> Color.White
        }


        val borderColor = if (isSelected) Color(selectedColor.toArgb()) else Color(200, 200, 200)

        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.12f else 1f, animationSpec = tween(220)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(backgroundColor)
                .border(.4.dp, borderColor)
                .clickable(enabled = isCurrentMonth) { onClick() }) {
            Column(modifier = Modifier.padding(0.dp)) {

                val isSunday = day.date.dayOfWeek == DayOfWeek.SUNDAY

                Text(
                    text = day.date.dayOfMonth.toString(),
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Clip,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusable()
                        .basicMarquee()
                        .padding(start = 3.dp, top = 2.dp),
                    fontSize = 13.sp,
                    color = if (isSunday) Color.Red else Color.Black
                )
                if (holiday != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(Color(46, 135, 176))
                    ) {
                        Text(
                            text = holiday.title,
                            fontSize = 9.sp,
                            color = Color.White,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusable()
                                .basicMarquee()
                                .padding(horizontal = 2.dp),
                            maxLines = 1
                        )
                    }
                }

                events.take(6).forEach { event ->
                    Log.d(
                        "11111111111",
                        "DayCell: ${event.title} on ${event.date} with color ${event.color} ${event.startstime} ${event.endtime}"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(Color(event.color))
                    ) {
                        Text(
                            text = event.title ?: "",
                            fontSize = 9.sp,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusable()
                                .basicMarquee()
                                .padding(horizontal = 2.dp),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun PermissionDeniedUI(onRetry: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Calendar permission is required ❌")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("Grant Permission")
            }
        }
    }

    @Composable
    fun LoadingUI() {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }


    @Composable
    fun DaysOfWeekRow() {
        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(224, 224, 224)),
                    contentAlignment = Alignment.Center
                ) {
                    if (it.name.take(3) == "SUN") {
                        Text(
                            text = it.name.take(3),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(255, 0, 0)
                        )
                    } else {
                        Text(
                            text = it.name.take(3),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0, 0, 0)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MonthHeader(
        month: YearMonth,
        selectedDate: LocalDate,
        month_week_day_cheng: Int
    ) {
        val context = LocalContext.current

        val headerText = when (month_week_day_cheng) {
            0 -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            1 -> selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM"))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = headerText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = null)
            }

            IconButton(onClick = {
                val intent = Intent(context, SettingPage::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        }
    }

    @Composable
    fun BottomBar(
        selected: BottomTab,
        onTodayClick: () -> Unit,
        onMonthClick: () -> Unit = {},
        onWeekClick: () -> Unit = {},
        onDayClick: () -> Unit = {},
        onAddClick: () -> Unit = {}
    ) {

        BottomAppBar(
            containerColor = Color.White, tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                BottomItem(
                    icon = Icons.Default.ArrowBack,
                    label = "Today",
                    selected = false,
                    onClick = onTodayClick
                )

                BottomItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "Month",
                    selected = selected == BottomTab.MONTH,
                    onClick = onMonthClick
                )

                BottomItem(
                    icon = Icons.Default.ViewWeek,
                    label = "Week",
                    selected = selected == BottomTab.WEEK,
                    onClick = onWeekClick
                )

                BottomItem(
                    icon = Icons.Default.ViewDay,
                    label = "Day",
                    selected = selected == BottomTab.DAY,
                    onClick = onDayClick
                )
            }
        }
    }

    fun getIndiaHolidays(
        context: Context, start: LocalDate, end: LocalDate
    ): List<CalendarHoliday> {
        val holidays = mutableListOf<CalendarHoliday>()
        val projection = arrayOf(
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.ALL_DAY
        )
        val startMillis = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis =
            end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val selection = """
            ${CalendarContract.Events.DTSTART} >= ? AND
            ${CalendarContract.Events.DTSTART} < ? AND
            ${CalendarContract.Events.ALL_DAY} = 1
        """.trimIndent()
        val selectionArgs = arrayOf(
            startMillis.toString(), endMillis.toString()
        )
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val millis = cursor.getLong(0)
                val title = cursor.getString(1)
                val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                holidays.add(CalendarHoliday(date, title))
            }
        }

        return holidays
    }

    @Composable
    fun BottomItem(
        icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit
    ) {
        var selectedColor by remember {
            mutableStateOf(loadThemeColor(this@MainActivity))
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    selectedColor = loadThemeColor(this@MainActivity)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val bgColor = if (selected) selectedColor.copy(alpha = 0.15f) else Color.Transparent
        val contentColor = if (selected) selectedColor else Color.Gray

        Surface(
            onClick = onClick, color = bgColor, shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = label, fontSize = 11.sp, color = contentColor
                )
            }
        }
    }

    enum class BottomTab {
        MONTH, WEEK, DAY
    }

    fun darkenColor(color: Color, factor: Float = 0.1f): Color {
        return Color(
            ColorUtils.blendARGB(
                color.toArgb(), android.graphics.Color.WHITE, 1f - factor
            )
        )
    }
}
    
