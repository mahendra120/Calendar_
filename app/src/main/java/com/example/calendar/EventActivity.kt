package com.example.calendar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.calendar.RoomDatabase.CalendarColorPrefs
import com.example.calendar.RoomDatabase.CalendarDatabase
import com.example.calendar.RoomDatabase.CalendarDisplayPrefs
import com.example.calendar.RoomDatabase.EventEntity
import com.example.calendar.RoomDatabase.EventViewModel
import com.example.calendar.RoomDatabase.colosave.loadThemeColor
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormats {
    val DISPLAY_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    val ISO_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE  // "yyyy-MM-dd"
}

class EventActivity : ComponentActivity() {

    private var _multiSelectedDates = mutableStateListOf<String>()
    val multiSelectedDates: List<String> get() = _multiSelectedDates

    private val multiDayLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val selectedDates =
                result.data?.getStringArrayListExtra("selected_dates") ?: arrayListOf()

            if (selectedDates.isNotEmpty()) {
                _multiSelectedDates.clear()
                _multiSelectedDates.addAll(selectedDates)
            }
        }
    }

    var title by mutableStateOf("")
    var dialogShowFirst by mutableStateOf(false)
    var dialogShowSecond by mutableStateOf(false)
    var allDay by mutableStateOf(false)




    var location by mutableStateOf("")
    var url by mutableStateOf("")
    var note by mutableStateOf("")




    var selectedCalendarText by mutableStateOf("")

    val currentTime = LocalTime.now()
    val oneHourLater = currentTime.plusHours(1)

    var formattedTime by mutableStateOf(
        currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    )

    var formattedTime1 by mutableStateOf(
        oneHourLater.format(DateTimeFormatter.ofPattern("hh:mm a"))
    )

    private lateinit var viewModel: EventViewModel

    var selectedColor by mutableStateOf(Color.White)

    var selectedate by mutableStateOf("")
    var isReadOnly by mutableStateOf(false)

    private var calendarId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = CalendarDatabase.getDatabase(this@EventActivity)
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventViewModel(this@EventActivity.application) as T
                }
            }
        )[EventViewModel::class.java]

        lifecycleScope.launch {
            calendarId = db.calendarDao().getAnyCalendarId() ?: -1
        }

        selectedCalendarText = CalendarDisplayPrefs.load(this)
        selectedColor = Color(CalendarColorPrefs.load(this))

        title = intent.getStringExtra("title") ?: ""
        formattedTime = intent.getStringExtra("startstime") ?: formattedTime
        formattedTime1 = intent.getStringExtra("endtime") ?: formattedTime1

        selectedate = intent.getStringExtra("date")
            ?: LocalDate.now().format(DateFormats.DISPLAY_FORMAT)

        isReadOnly = intent.getStringExtra("readOnly") != null

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = { Topbar() }) { innerPadding ->
                UI()
            }
        }
    }

    @Composable
    fun UI() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(240, 240, 240))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp)
            ) {
                item {
                    uipats()
                }
            }
        }
    }

    @Composable
    @Preview(showBackground = true)
    fun uipats() {
        var themeColor by remember {
            mutableStateOf(loadThemeColor(this@EventActivity))
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    themeColor = loadThemeColor(this@EventActivity)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        DisposableEffect(Unit) {
            val lifecycleOwner = this@EventActivity as ComponentActivity
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    this@EventActivity.selectedCalendarText =
                        CalendarDisplayPrefs.load(this@EventActivity)
                    this@EventActivity.selectedColor =
                        Color(CalendarColorPrefs.load(this@EventActivity))
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Spacer(modifier = Modifier.padding(10.dp))

        Button(
            onClick = {
                val intent = Intent(this@EventActivity, CalenderActivity::class.java)
                startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Calendar",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.size(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = selectedColor
                        )
                    ) {}
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        selectedCalendarText,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(24.dp))
        TitleInputRow()
        Spacer(modifier = Modifier.padding(24.dp))
        EventDateTimeUI()
        Spacer(modifier = Modifier.padding(24.dp))
        multipleday()
        Spacer(modifier = Modifier.padding(24.dp))
        LocationSection()
        Spacer(modifier = Modifier.padding(24.dp))
        Button(
            onClick = {
                if (calendarId == -1) {
                    Log.e("EVENT_SAVE", "Invalid calendarId, event not saved")
                    return@Button
                }
                if (title.isEmpty()) {
                    dialogShowFirst = true
                } else {
                    val datesToSave = if (multiSelectedDates.isNotEmpty()) {
                        multiSelectedDates
                    } else {
                        listOf(selectedate)
                    }

                    val eventId = intent.getIntExtra("eventId", -1)
                    val isEdit = eventId != -1
                    Log.d("EDIT_CHECK", "eventId = $eventId , isEdit = $isEdit")

                    lifecycleScope.launch {
                        datesToSave.forEach { dateStr ->
                            if (isEdit) {
                                viewModel.updateEvent(
                                    EventEntity(
                                        id = eventId,
                                        title = title,
                                        date = dateStr,  // Use each date
                                        startstime = formattedTime,
                                        endtime = formattedTime1,
                                        calendarId = calendarId,
                                        color = selectedColor.toArgb()
                                    )
                                )
                            } else {
                                viewModel.saveEvent(
                                    title = title,
                                    date = dateStr,  // Use each date
                                    startstime = formattedTime,
                                    endtime = formattedTime1,
                                    color = selectedColor.toArgb()
                                )
                            }
                        }
                        finish()
                    }

                    Log.d(
                        "ooooooooo",
                        "Bottom Save: $title , $formattedTime $formattedTime1, color ${selectedColor.toArgb()}"
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.padding(50.dp))
    }

    @Composable
    fun LocationSection() {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            TextField(
                value = location,
                onValueChange = { location = it },
                placeholder = {
                    Text("Location", color = Color.LightGray, fontSize = 13.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

            TextField(
                value = url,
                onValueChange = { url = it },
                placeholder = {
                    Text("URL", color = Color.LightGray, fontSize = 13.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

            TextField(
                value = note,
                onValueChange = { note = it },
                placeholder = {
                    Text(
                        "Note",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                singleLine = false,
                maxLines = 6,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

        }
    }

    @Composable
    fun TitleInputRow() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            text = "Title", color = Color.LightGray
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                )
            }

            Divider(
                thickness = 1.dp, color = Color(0xFFE0E0E0)
            )
        }
    }

    @Composable
    fun EventDateTimeUI() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All-day event", modifier = Modifier.weight(1f), fontSize = 16.sp
                )

                Switch(
                    checked = allDay, onCheckedChange = { allDay = it })
            }

            val startHour = intent.getIntExtra("startHour", -1)
            val endHour = intent.getIntExtra("endHour", -1)
            val timeRange = intent.getStringExtra("timeRange")

            var localSelectedDate by remember {
                mutableStateOf(
                    intent.getStringExtra("date")
                        ?: selectedate
                )
            }

            LaunchedEffect(localSelectedDate) {
                selectedate = localSelectedDate
            }

            Log.d("EVENT", "Time = $timeRange")

            if (startHour != -1 && endHour != -1) {
                formattedTime = formatHour(startHour)
                formattedTime1 = formatHour(endHour)
            }

            Divider(modifier = Modifier.height(0.5.dp))

            DateRow(
                title = "Starts",
                date = localSelectedDate,
                time = formattedTime,
                showTime = !allDay,
                onTimeSelected = { newTime -> formattedTime = newTime },
                onDateSelected = { newDate ->
                    selectedate = newDate
                }
            )

            Divider(modifier = Modifier.height(0.5.dp))

            DateRow(
                title = "Ends",
                date = localSelectedDate,
                time = formattedTime1,
                showTime = !allDay,
                onTimeSelected = { newTime -> formattedTime1 = newTime },
                onDateSelected = { newDate -> selectedate = newDate }
            )
        }
    }

    private fun formatHour(hour: Int): String {
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val amPm = if (hour < 12) "AM" else "PM"
        return String.format("%02d:00 %s", displayHour, amPm)
    }

    @Composable
    fun multipleday() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            notification(
                firsttext = "Multiple days",
                secend = "",
                time = if (multiSelectedDates.isNotEmpty()) "${multiSelectedDates.size} selected" else "none",
            ) {
                val intent = Intent(this@EventActivity, SelectedDateActivity::class.java)
                multiDayLauncher.launch(intent)
            }
            Divider(modifier = Modifier.height(0.5.dp))
            notification(
                firsttext = "Notification",
                secend = "",
                time = "none"
            ) {

            }
        }
    }

    @Composable
    fun notification(firsttext: String, secend: String, time: String? = null, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RectangleShape,
            modifier = Modifier.height(50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$firsttext: $secend",
                    modifier = Modifier.padding(horizontal = 0.dp),
                    color = Color(0, 0, 0),
                    fontSize = 15.sp
                )
                if (time == null) {
                    Text(
                        text = "none",
                        modifier = Modifier.padding(horizontal = 0.dp),
                        color = Color(0, 0, 0),
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        text = time,
                        modifier = Modifier.padding(horizontal = 0.dp),
                        color = Color(0, 0, 0),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DateRow(
        title: String,
        date: String,
        time: String,
        showTime: Boolean,
        onTimeSelected: (String) -> Unit,
        onDateSelected: (String) -> Unit
    ) {

        var showTimePicker by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }

        val timePattern = Regex("""^\s*\d{1,2}:\d{2}\s*(AM|PM|am|pm)?\s*$""")
        val parsedTime = try {
            if (timePattern.matches(time)) {
                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
                LocalTime.parse(time.uppercase(Locale.ENGLISH), formatter)
            } else {
                LocalTime.now()
            }
        } catch (e: Exception) {
            LocalTime.now()
        }

        val timePickerState = rememberTimePickerState(
            initialHour = parsedTime.hour,
            initialMinute = parsedTime.minute,
            is24Hour = false
        )

        val datePickerState = rememberDatePickerState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = selectedate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        showDatePicker = true
                    }
                )

                if (showTime) {
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = time,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            showTimePicker = true
                        }
                    )
                }
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val hourOfDay = timePickerState.hour
                        val displayHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                        val amPm = if (hourOfDay < 12) "AM" else "PM"
                        val formatted =
                            String.format("%02d:%02d %s", displayHour, timePickerState.minute, amPm)
                        onTimeSelected(formatted)
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Select time") },
                text = {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFFF3E5F5),
                            selectorColor = Color(0xFFFF9800),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color(0xFF6200EE),
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color(0xFF6200EE),
                            timeSelectorSelectedContentColor = Color(0xFF6200EE),
                            timeSelectorUnselectedContentColor = Color.Gray
                        )
                    )
                }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("d/M/yyyy (EEE)"))

                            onDateSelected(newDate)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = true
                )
            }
        }

    }


    @SuppressLint("ContextCastToActivity")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Topbar() {
        var themeColor by remember {
            mutableStateOf(loadThemeColor(this@EventActivity))
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    themeColor = loadThemeColor(this@EventActivity)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        val topbar = intent.getStringExtra("true")
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (topbar == null) {
                        Text(
                            text = "New event",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "Read Only",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 30.dp),
                            color = Color.Black
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (title.isEmpty()) {
                        dialogShowSecond = true
                    } else {
                        finish()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = themeColor
                    )
                }
            },
            actions = {

                val eventId = intent.getIntExtra("eventId", -1)
                val isEdit = eventId != -1

                if (topbar == null) {

                    TextButton(
                        onClick = {

                            if (calendarId == -1) {
                                Log.e("EVENT_SAVE", "Invalid calendarId, event not saved")
                                return@TextButton
                            }

                            if (title.isEmpty()) {
                                dialogShowFirst = true
                                return@TextButton
                            }

                            val datesToSave = if (multiSelectedDates.isNotEmpty()) {
                                multiSelectedDates
                            } else {
                                listOf(selectedate)
                            }

                            lifecycleScope.launch {
                                datesToSave.forEach { dateStr ->
                                    if (isEdit) {
                                        viewModel.updateEvent(
                                            EventEntity(
                                                id = eventId,
                                                title = title,
                                                date = dateStr,  // Use each date
                                                startstime = formattedTime,
                                                endtime = formattedTime1,
                                                calendarId = calendarId,
                                                color = selectedColor.toArgb()
                                            )
                                        )
                                    } else {
                                        viewModel.saveEvent(
                                            title = title,
                                            date = dateStr,
                                            startstime = formattedTime,
                                            endtime = formattedTime1,
                                            color = selectedColor.toArgb()
                                        )
                                    }
                                }
                                finish()
                            }

                            Log.d(
                                "ooooooooo",
                                "Topbar: $title , $formattedTime $formattedTime1,color ${selectedColor.toArgb()}"
                            )
                        }
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = themeColor
                        )
                    }

                }

            }
        )
        if (dialogShowSecond) {
            Dialog(
                onDismissRequest = {
                    dialogShowSecond = false
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    shape = RectangleShape,
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.padding(22.dp))

                        Text(
                            text = "Discard changes to this event?",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    finish()
                                }
                            ) {
                                Text(
                                    text = "Discard",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    dialogShowSecond = false
                                }
                            ) {
                                Text(
                                    text = "Keep editing",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

        }
        if (dialogShowFirst) {
            Dialog(
                onDismissRequest = {
                    dialogShowFirst = false
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 24.dp),
                    shape = RectangleShape,
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.padding(vertical = 20.dp))
                        Text(
                            text = "Add title",
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentSize(Alignment.Center)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { dialogShowFirst = false }
                            ) {
                                Text(
                                    text = "OK",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        selectedColor = Color(CalendarColorPrefs.load(this))
        selectedCalendarText = CalendarDisplayPrefs.load(this)
    }

}