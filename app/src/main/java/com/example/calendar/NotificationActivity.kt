package com.example.calendar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.calendar.RoomDatabase.colosave.loadThemeColor

class NotificationActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SELECTED_REMINDER = "selected_reminder"
        const val EXTRA_REMINDER_TEXT = "reminder_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NotificationScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    val context = LocalContext.current
    val activity = context as? NotificationActivity
    var themeColor by remember { mutableStateOf(loadThemeColor(context)) }

    // Observe lifecycle to refresh theme color
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                themeColor = loadThemeColor(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NotificationTopBar(
                onBack = { activity?.finish() },
                themeColor = themeColor
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ReminderUI(
                themeColor = themeColor,
                onItemSelected = { selectedMinutes, displayText ->
                    val intent = Intent().apply {
                        putExtra(NotificationActivity.EXTRA_SELECTED_REMINDER, selectedMinutes)
                        putExtra(NotificationActivity.EXTRA_REMINDER_TEXT, displayText)
                    }
                    activity?.setResult(android.app.Activity.RESULT_OK, intent)
                    activity?.finish()
                }
            )
        }
    }
}

@Composable
fun ReminderUI(
    themeColor: Color,
    onItemSelected: (Int, String) -> Unit
) {
    val reminderOptions = remember {
        listOf(
            ReminderOption(1, "1 minute before", 1),
            ReminderOption(2, "3 minutes before", 3),
            ReminderOption(3, "5 minutes before", 5),
            ReminderOption(4, "10 minutes before", 10),
            ReminderOption(5, "15 minutes before", 15),
            ReminderOption(6, "20 minutes before", 20),
            ReminderOption(7, "30 minutes before", 30),
            ReminderOption(8, "45 minutes before", 45),
            ReminderOption(9, "1 hour before", 60),
            ReminderOption(10, "2 hours before", 120),
            ReminderOption(11, "3 hours before", 180),
            ReminderOption(12, "4 hours before", 240),
            ReminderOption(13, "5 hours before", 300),
            ReminderOption(14, "6 hours before", 360),
            ReminderOption(15, "7 hours before", 420),
            ReminderOption(16, "8 hours before", 480),
            ReminderOption(17, "9 hours before", 540),
            ReminderOption(18, "10 hours before", 600),
            ReminderOption(19, "11 hours before", 660),
            ReminderOption(20, "12 hours before", 720),
            ReminderOption(21, "13 hours before", 780),
            ReminderOption(22, "14 hours before", 840),
            ReminderOption(23, "15 hours before", 900),
            ReminderOption(24, "16 hours before", 960),
            ReminderOption(25, "17 hours before", 1020),
            ReminderOption(26, "18 hours before", 1080),
            ReminderOption(27, "19 hours before", 1140),
            ReminderOption(28, "20 hours before", 1200),
            ReminderOption(29, "1 day before", 1440)
        )
    }

    var selectedOptionId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E6E6))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Select one reminder time",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Text(
                text = "Event time",
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(
            items = reminderOptions,
            key = { it.id }
        ) { option ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .clickable {
                            selectedOptionId = option.id
                            onItemSelected(option.minutes, option.displayText)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.displayText,
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp
                    )

                    if (selectedOptionId == option.id) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = themeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // Empty circle for unselected items
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTopBar(
    onBack: () -> Unit,
    themeColor: Color
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Select Reminder",
                textAlign = TextAlign.Start,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = themeColor
                )
            }
        },
        actions = {
            Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        )
    )
}

data class ReminderOption(
    val id: Int,
    val displayText: String,
    val minutes: Int
)