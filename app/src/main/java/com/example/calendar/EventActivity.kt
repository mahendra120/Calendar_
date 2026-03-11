    package com.example.calendar
    
    import android.Manifest
    import android.annotation.SuppressLint
    import android.app.AlarmManager
    import android.content.Context
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.provider.Settings
    import android.util.Log
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.rememberLauncherForActivityResult
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
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.AccessTime
    import androidx.compose.material.icons.filled.ArrowBackIosNew
    import androidx.compose.material.icons.filled.ArrowForward
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material.icons.filled.LocationOn
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
    import androidx.core.content.ContextCompat
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
    import com.example.calendar.notification.scheduleNotification
    import kotlinx.coroutines.launch
    import java.time.Instant
    import java.time.LocalDate
    import java.time.LocalDateTime
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
    
    
        var reminderText by mutableStateOf("none")
        var reminderMinutes by mutableStateOf(0)
    
    
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
    
        private val notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Log.w("Notification", "User denied POST_NOTIFICATIONS")
                // Optional: show a toast or dialog explaining why it's needed
            }
        }
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
    
    
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
    
    
            viewModel = ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return EventViewModel(this@EventActivity.application) as T
                    }
                }
            )[EventViewModel::class.java]
    
    
            val db = CalendarDatabase.getDatabase(this@EventActivity)
            lifecycleScope.launch {
                calendarId = db.calendarDao().getAnyCalendarId() ?: -1
            }
    
            selectedCalendarText = CalendarDisplayPrefs.load(this)
            selectedColor = Color(CalendarColorPrefs.load(this))
    
            title = intent.getStringExtra("title") ?: ""
            formattedTime = intent.getStringExtra("startstime") ?: formattedTime
            formattedTime1 = intent.getStringExtra("endtime") ?: formattedTime1
    
            note = intent.getStringExtra("note") ?: ""
            url = intent.getStringExtra("url") ?: ""
            location = intent.getStringExtra("location") ?: ""
    
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
    
    
            TextButton(
                onClick = {
                    if (title.isEmpty()) {
                        dialogShowFirst = true
                        return@TextButton
                    }
    
                    val datesToSave = if (multiSelectedDates.isNotEmpty()) {
                        multiSelectedDates
                    } else {
                        listOf(selectedate)
                    }
    
                    val eventId = intent.getIntExtra("eventId", -1)
                    val isEdit = eventId != -1
    
                    lifecycleScope.launch {
                        try {
                            datesToSave.forEach { dateStr ->
                                if (isEdit) {
                                    // Await suspend update
                                    viewModel.updateEvent(
                                        EventEntity(
                                            id = eventId,
                                            title = title,
                                            date = dateStr,
                                            startstime = formattedTime,
                                            endtime = formattedTime1,
                                            calendarId = calendarId,  // Assume this is fetched earlier
                                            color = selectedColor.toArgb(),
                                            location = location,
                                            url = url,
                                            note = note
                                        )
                                    )
                                } else {
                                    // Await suspend save
                                    viewModel.saveEvent(
                                        title = title,
                                        date = dateStr,
                                        startstime = formattedTime,
                                        endtime = formattedTime1,
                                        color = selectedColor.toArgb(),
                                        location = location,
                                        url = url,
                                        note = note
                                    )
    
                                    // 👇 Notifications AFTER insert completes
                                    showTestNotificationNow(this@EventActivity)

                                    if (reminderText != "none" && reminderMinutes > 0) {
                                        scheduleEventNotification(
                                            context = this@EventActivity,
                                            date = dateStr,
                                            time = formattedTime1,  // end time use કરો (અથવા start time જો જોઈએ)
                                            title = title,
                                            reminderMinutesAfter = reminderMinutes   // ← positive value
                                        )
                                    }
                                }
                            }
                            // 👇 Finish ONLY AFTER all inserts complete
                            finish()
                        } catch (e: Exception) {
                            Log.e("EVENT_SAVE", "Error saving events: ${e.message}")
                            // Optional: Show toast/error dialog
                        }
                    }
    
                    Log.d("EVENT_SAVE", "Saving: $title, $formattedTime to $formattedTime1, color ${selectedColor.toArgb()}")
                },
    
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor
                )
            ) {
                Text(
                    text = "Save",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.padding(50.dp))
    
        }

        @Composable
        fun LocationSection() {
    
            var isUrlValid by remember { mutableStateOf(true) }
    
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
    
                TextField(
                    value = location,
                    onValueChange = { location = it.trimStart() },
                    placeholder = {
                        Text("Location", color = Color.LightGray, fontSize = 13.sp)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    trailingIcon = {
                        Row(
                            modifier = Modifier.padding(end = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
    
                            // ❌ Clear
                            if (location.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            location = ""
                                        }
                                )
                            }
    
                            Spacer(modifier = Modifier.width(12.dp))
    
                            // 📍 Open Maps / Search
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Open Location",
                                tint = Color(0xFF1A73E8),
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        if (location.isNotBlank()) {
                                            openLocation(this@EventActivity, location)
                                        }
                                    }
                            )
                        }
                    },
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
                    onValueChange = { url = it.trimStart() },
                    placeholder = {
                        Text("Search or type URL", color = Color.LightGray, fontSize = 13.sp)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    trailingIcon = {
                        Row(
                            modifier = Modifier.padding(end = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
    
                            // ❌ Clear icon
                            if (url.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            url = ""
                                        }
                                )
                            }
    
                            Spacer(modifier = Modifier.width(12.dp))
    
                            // ➡️ Search icon
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Search",
                                tint = Color(0xFF1A73E8), // Google blue
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        if (url.isNotBlank()) {
                                            openInBrowser(this@EventActivity, url)
                                        }
                                    }
                            )
                        }
                    },
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
    
        fun openLocation(context: Context, input: String) {
            val uri = Uri.parse(
                "https://www.google.com/maps/search/?api=1&query=${Uri.encode(input)}"
            )
    
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps") // optional
            context.startActivity(intent)
        }
    
        fun openInBrowser(context: Context, input: String) {
            val finalUrl = if (
                input.startsWith("http://") ||
                input.startsWith("https://")
            ) {
                input
            } else {
                "https://www.google.com/search?q=${Uri.encode(input)}"
            }
    
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            context.startActivity(intent)
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
    
            val multiDayLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedDates =
                        result.data?.getStringArrayListExtra("selected_dates") ?: arrayListOf()
                    if (selectedDates.isNotEmpty()) {
                        _multiSelectedDates.clear()
                        _multiSelectedDates.addAll(selectedDates)
                    }
                }
            }
    
            val notificationLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    reminderText =
                        data?.getStringExtra(NotificationActivity.EXTRA_REMINDER_TEXT) ?: "none"
                    reminderMinutes =
                        data?.getIntExtra(NotificationActivity.EXTRA_SELECTED_REMINDER, 0) ?: 0
    
                    Log.d("Notification", "Selected reminder: $reminderText, minutes: $reminderMinutes")
                }
            }
    
    
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
                    time = reminderText
                ) {
                    val intent = Intent(this@EventActivity, NotificationActivity::class.java)
                    notificationLauncher.launch(intent)
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
            )
            {
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
                                if (title.isEmpty()) {
                                    dialogShowFirst = true
                                    return@TextButton
                                }
    
                                val datesToSave = if (multiSelectedDates.isNotEmpty()) {
                                    multiSelectedDates
                                } else {
                                    listOf(selectedate)
                                }
    
                                val eventId = intent.getIntExtra("eventId", -1)
                                val isEdit = eventId != -1
    
                                lifecycleScope.launch {
                                    try {
                                        datesToSave.forEach { dateStr ->
                                            if (isEdit) {
                                                // Await suspend update
                                                viewModel.updateEvent(
                                                    EventEntity(
                                                        id = eventId,
                                                        title = title,
                                                        date = dateStr,
                                                        startstime = formattedTime,
                                                        endtime = formattedTime1,
                                                        calendarId = calendarId,  // Assume this is fetched earlier
                                                        color = selectedColor.toArgb(),
                                                        location = location,
                                                        url = url,
                                                        note = note
                                                    )
                                                )
                                            } else {
                                                // Await suspend save
                                                viewModel.saveEvent(
                                                    title = title,
                                                    date = dateStr,
                                                    startstime = formattedTime,
                                                    endtime = formattedTime1,
                                                    color = selectedColor.toArgb(),
                                                    location = location,
                                                    url = url,
                                                    note = note
                                                )
    
                                                // 👇 Notifications AFTER insert completes
                                                showTestNotificationNow(this@EventActivity)

                                                if (reminderText != "none" && reminderMinutes > 0) {
                                                    scheduleEventNotification(
                                                        context = this@EventActivity,
                                                        date = dateStr,
                                                        time = formattedTime1,  // end time use કરો (અથવા start time જો જોઈએ)
                                                        title = title,
                                                        reminderMinutesAfter = reminderMinutes   // ← positive value
                                                    )
                                                }
                                            }
                                        }
                                        // 👇 Finish ONLY AFTER all inserts complete
                                        finish()
                                    } catch (e: Exception) {
                                        Log.e("EVENT_SAVE", "Error saving events: ${e.message}")
                                        // Optional: Show toast/error dialog
                                    }
                                }
    
                                Log.d("EVENT_SAVE", "Saving: $title, $formattedTime to $formattedTime1, color ${selectedColor.toArgb()}")
                            },
    
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
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
                        shape = RoundedCornerShape(24.dp),
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
    
        fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
    
        fun requestExactAlarmPermission(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }

        fun scheduleEventNotification(
            context: Context,
            date: String,           // "d/M/yyyy (EEE)"
            time: String,           // "hh:mm a"
            title: String,
            reminderMinutesAfter: Int = 0
        ) {
            if (reminderMinutesAfter <= 0) {
                Log.d("Notification", "No after reminder or invalid - skipping")
                return
            }

            if (!canScheduleExactAlarms(context)) {
                requestExactAlarmPermission(context)
                Log.w("Notification", "Exact alarm permission denied - cannot schedule")
                return
            }

            val triggerTime = getTriggerTimeAfter(date, time, reminderMinutesAfter)
            if (triggerTime <= System.currentTimeMillis()) {
                Log.w("Notification", "After time already passed - skipping")
                return
            }

            val requestCode = (title + date + time + reminderMinutesAfter).hashCode()
            scheduleNotification(
                context = context,
                triggerTime = triggerTime,
                title = title,
                message = "Event follow-up: $title",
                requestCode = reminderMinutes
            )
            Log.d("Notification", "Scheduled AFTER $reminderMinutesAfter min at ${LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(triggerTime), ZoneId.systemDefault())}")
        }

        fun getTriggerTimeAfter(
            date: String,
            time: String,
            minutesAfter: Int
        ): Long {
            try {
                val cleanDate = date.replace(Regex("\\s*\\([^)]+\\)"), "")
                val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

                val localDate = LocalDate.parse(cleanDate, dateFormatter)
                val localTime = LocalTime.parse(time, timeFormatter)

                var eventDateTime = localDate.atTime(localTime)

                // 👇 મુખ્ય change: + minutesAfter
                eventDateTime = eventDateTime.plusMinutes(minutesAfter.toLong())

                return eventDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } catch (e: Exception) {
                Log.e("Notification", "Parse error: ${e.message}")
                return System.currentTimeMillis() + 60000  // fallback
            }
        }
    
        fun showTestNotificationNow(context: Context) {
            val triggerTime = System.currentTimeMillis() + 5000
    
            scheduleNotification(
                context = context,
                triggerTime = triggerTime,
                title = "$title",
                message = "saved successfully",
                requestCode = reminderMinutes
            )
        }
    
    }