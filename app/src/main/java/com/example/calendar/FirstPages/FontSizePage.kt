package com.example.calendar.FirstPages

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.MainActivity
import com.example.calendar.R

class FontSizePage : ComponentActivity() {

    var horizontal_scrolling by mutableStateOf("Horizontal scrolling")

    var selectedTimeFormat by mutableStateOf("24-hour: 14:00")

    var blackText by mutableStateOf(false)

    var Link_Google_Calendar by mutableStateOf(false)
    var displayTimeOnLabel by mutableStateOf(false)
    var textSize by mutableStateOf(10)
    var screen by mutableStateOf(0)

    var selectedLabelFormat by mutableStateOf("Labels only")

    lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        if (prefs.getBoolean("setup_completed", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = { TopBar() }) { innerPadding ->
                UI(innerPadding)
            }
        }
    }

    data class CalendarEvent(
        val startDay: Int,
        val endDay: Int,
        val title: String,
        val time: String?,
        val color1: Color,
        val color2: Color,
        val top: Dp
    )


    val events = listOf(
        CalendarEvent(
            12, 12, "Weekly", null,
            Color(0xFFE91E63), Color(0xFFF48FB1), 30.dp
        ),

        CalendarEvent(
            14, 14, "Lunch", "12:00",
            Color(0xFFFFA726), Color(0xFFFFB74D), 30.dp
        ),

        CalendarEvent(
            15, 16, "Weekly Meeting", null,
            Color(0xFF66BB6A), Color(0xFF81C784), 30.dp
        ),

        CalendarEvent(
            15, 15, "Meeting", null,
            Color(0xFF42A5F5), Color(0xFF64B5F6), 60.dp
        ),

        CalendarEvent(
            15, 15, "Gym", "18:00",
            Color(0xFFB39DDB), Color(0xFFCE93D8), 90.dp
        ),
    )


    @Composable
    fun UI(innerPadding: PaddingValues) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Color(0xFFF1F1F1)
                )
        ) {
            Column {
                if (screen < 2) {
                    val dayWidth = 60.dp
                    Spacer(modifier = Modifier.padding(top = 25.dp))
                    Box {
                        Row {
                            for (day in 12..18) {
                                Box(
                                    modifier = Modifier.background(
                                        color = Color(
                                            255,
                                            255,
                                            255
                                        )
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(dayWidth)
                                            .height(120.dp)
                                            .border(0.5.dp, Color.LightGray)
                                            .background(Color(255, 255, 255))
                                    ) {
                                        Text(
                                            text = "$day",
                                            fontSize = textSize.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 3.dp, start = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        events.forEach { event ->
                            val showColor =
                                selectedLabelFormat == "Labels only" ||
                                        (selectedLabelFormat == "Labels and coloured letters" && event.time == null)
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = ((event.startDay - 12) * dayWidth.value).dp,
                                        y = event.top
                                    )
                                    .width(dayWidth * (event.endDay - event.startDay + 1))
                                    .height(22.dp)
                                    .background(
                                        if (showColor) {
                                            if (!blackText) event.color1 else event.color2
                                        } else {
                                            Color.Transparent
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val labelText =
                                    if (displayTimeOnLabel && event.time != null) {
                                        val formattedTime =
                                            formatTime(event.time, selectedTimeFormat)
                                        "$formattedTime ${event.title}"
                                    } else {
                                        event.title
                                    }
                                val textColor =
                                    if (showColor) {
                                        if (!blackText) Color.White else Color.Black
                                    } else {
                                        Color.Black
                                    }

                                Text(
                                    text = labelText,
                                    color = textColor,
                                    fontSize = textSize.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                        }

                    }
                }

                if (screen == 0) {
                    fontsize()
                } else if (screen == 1) {
                    DisplaySettings()
                } else if (screen == 2) {
                    calender_displaysettings()
                } else if (screen == 3) {
                    final_settings()
                }

                Button(
                    onClick = {
                        if (screen < 3) {
                            screen++
                        } else {
                            prefs.edit().putBoolean("setup_completed", true).apply()
                            startActivity(Intent(this@FontSizePage, MainActivity::class.java))
                            finish()
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(
                            3,
                            104,
                            157,
                            255
                        )
                    )
                )
                {
                    Text("Next", fontSize = 15.sp)
                }
            }
        }
    }

    @Composable
    fun final_settings() {
        Spacer(modifier = Modifier.padding(35.dp))
        Image(
            painter = painterResource(R.drawable.allow),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(15.dp))
        PermissionInstructions()
    }


    fun formatTime(time: String, format: String): String {
        // time example: "18:00"
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1]

        return if (format.startsWith("AM/PM")) {
            val amPm = if (hour >= 12) "PM" else "AM"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            "$hour12:$minute $amPm"
        } else {
            time // 24-hour 그대로
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar() {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(255, 255, 255)),
            title = {
                if (screen == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Font Size", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (screen == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp),
                    ) {
                        Text(
                            "Calender Display Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (screen == 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp),
                    ) {
                        Text(
                            "Month calendar display settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (screen == 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Final settings",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 10.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            navigationIcon = {
                if (screen == 1) {
                    IconButton(onClick = { screen = 0 }, modifier = Modifier.padding(start = 5.dp))
                    {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else if (screen == 2) {
                    IconButton(onClick = { screen = 1 }, modifier = Modifier.padding(start = 5.dp))
                    {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else if (screen == 3) {
                    IconButton(onClick = { screen = 2 }, modifier = Modifier.padding(start = 5.dp))
                    {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            })
    }

    @Composable
    fun DisplaySettings() {

        Column {
            Spacer(Modifier.padding(10.dp))
            SectionTitle("Label format")
            SettingSwitch(
                title = "Black text",
                checked = blackText,
                onCheckedChange = { blackText = it }
            )

            SettingSwitch(
                title = "Display time on label",
                checked = displayTimeOnLabel,
                onCheckedChange = { displayTimeOnLabel = it }
            )
            Spacer(Modifier.padding(17.dp))
            val labelFormats = listOf(
                "Labels only",
                "Labels and coloured letters"
            )
            labelFormats.forEach { name ->
                SelectableRow(
                    title = name,
                    selected = selectedLabelFormat == name,
                    onClick = { selectedLabelFormat = name }
                )
            }
            Spacer(Modifier.padding(6.dp))
            Text(
                "*Time-specific schedules are displayed as coloured text.",
                modifier = Modifier.padding(start = 15.dp),
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(Modifier.padding(10.dp))
            SectionTitle("Time format")

            val timeFormats = listOf(
                "24-hour: 14:00",
                "AM/PM: 2:00 PM"
            )
            timeFormats.forEach { name ->
                SelectableRow(
                    title = name,
                    selected = selectedTimeFormat == name,
                    onClick = { selectedTimeFormat = name }
                )
            }
            Spacer(modifier = Modifier.padding(25.dp))
        }
    }

    @Composable
    fun SettingSwitch(
        title: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title)

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFB0C4DE),
                        uncheckedTrackColor = Color(0xFFE0E0E0)
                    )
                )
            }
        }
    }

    @Composable
    fun SelectableRow(
        title: String,
        selected: Boolean,
        onClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(47.dp),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title)
                if (selected) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun SectionTitle(text: String) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp, bottom = 6.dp)
        )
    }

    @Composable
    fun calender_displaysettings() {
        Spacer(modifier = Modifier.padding(3.dp))
        Text(
            "Month calendar display settings",
            modifier = Modifier.padding(start = 15.dp),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.padding(3.dp))
        val scrolling = listOf(
            "Horizontal scrolling",
            "Vertical scrolling"
        )
        scrolling.forEach { name ->
            SelectableRow(
                title = name,
                selected = horizontal_scrolling == name,
                onClick = { horizontal_scrolling = name }
            )
        }
        Spacer(modifier = Modifier.padding(45.dp))
        SettingSwitch(
            title = "Link Google Calendar",
            checked = Link_Google_Calendar,
            onCheckedChange = { Link_Google_Calendar = it }
        )
        Spacer(modifier = Modifier.padding(45.dp))
        Card(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(47.dp),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Theme Color")

                Text("Simple Blue")
            }
        }
        Spacer(modifier = Modifier.padding(35.dp))
    }

    @Composable
    fun fontsize() {
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Text(
            "TextSize",
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        val textFontList = listOf(
            "X-Small" to 10,
            "Small" to 11,
            "Medium (Standard)" to 12,
            "Large" to 13,
            "X-Large" to 14
        )
        textFontList.forEach { (name, size) ->
            Card(
                onClick = {
                    textSize = size
                },
                modifier = Modifier
                    .height(46.dp)
                    .fillMaxWidth()
                    .background(Color(255, 255, 255))
                    .border(.1.dp, color = Color(0, 0, 0, 15)),
            )
            {
                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(0.1.dp, Color(0, 0, 0, 15))
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$name",
                        )
                        if (textSize == size) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = null,
                                tint = Color(0, 128, 0),
                                modifier = Modifier
                                    .padding(end = 15.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Text(
            "Font style",
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        val textFontList1 = listOf(
            "Bold" to 10,
            "Light" to 10
        )
        var selectedFont by remember { mutableStateOf("Light") }
        textFontList1.forEach { (name, size) ->
            Card(
                onClick = {
                    selectedFont = name
                },
                modifier = Modifier
                    .height(47.dp)
                    .fillMaxWidth()
                    .border(0.1.dp, Color(0, 0, 0, 15)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RectangleShape,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name)
                        if (selectedFont == name) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = null,
                                tint = Color(0, 128, 0),
                                modifier = Modifier
                                    .padding(end = 15.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.padding(top = 50.dp))
    }


    @Composable
    fun PermissionInstructions() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            InstructionItem(
                text = buildAnnotatedString {
                    append("Tap ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    ) {
                        append("[Allow]")
                    }
                    append(" when ")
                    append("[Access your calendar] ")
                    append("is shown on the next screen.")
                }
            )
            Spacer(modifier = Modifier.padding(10.dp))
            InstructionItem(
                text = AnnotatedString(
                    "This app requires access to the calendar functions; it cannot be used if you tap [Deny]."
                )
            )
            Spacer(modifier = Modifier.padding(10.dp))
            InstructionItem(
                text = AnnotatedString(
                    "The information in your calendar will not be linked or sent to third parties."
                )
            )
            Spacer(modifier = Modifier.padding(25.dp))
        }
    }

    @Composable
    fun InstructionItem(text: AnnotatedString) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.Top
        ) {

            // 🔵 Blue Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = Color(0xFF1A73E8),
                        shape = CircleShape
                    )
                    .padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // 📝 Text
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
        }
    }


//    fun saveSetupCompleted() {
//        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
//        prefs.edit().putBoolean("setup_completed", true).apply()
//    }

}