package com.example.calendar

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.RoomDatabase.DateSelectionViewModel
import com.example.calendar.RoomDatabase.EventViewModel
import com.example.calendar.RoomDatabase.colosave.loadThemeColor
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class SelectedDateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SelectedDatesScreen(
                onBack = { finish() },
                onDone = { selectedDates ->  // List<LocalDate> મળશે
                    val formattedDates = selectedDates.map {
                        it.format(DateTimeFormatter.ofPattern("d/M/yyyy (EEE)"))
                    }

                    val resultIntent = Intent()
                    resultIntent.putStringArrayListExtra("selected_dates", ArrayList(formattedDates))
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            )
        }
    }
}

private val GridBorderColor = Color(0xFFF0F0F0)
private val TextGray = Color(0xFFBCBCBC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedDatesScreen(
    onBack: () -> Unit,
    onDone: (List<LocalDate>) -> Unit  // ← નવો callback
) {
    val context = LocalContext.current
    val themeColor = remember { loadThemeColor(context) }
    var selectedDates by remember { mutableStateOf(setOf<LocalDate>()) }

    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.White, topBar = {
        TopAppBar(
            title = {}, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = themeColor
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
    }, bottomBar = {
        BottomDoneButton(
            themeColor = themeColor, enabled = selectedDates.isNotEmpty()
        ) {
            if (selectedDates.isNotEmpty()) {
                onDone(selectedDates.toList())
            }
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "You can select the dates to add.",
                color = themeColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            SwipeableCalendar(
                themeColor = themeColor, selectedDates = selectedDates, onDateClick = { date ->
                    selectedDates = if (selectedDates.contains(date)) {
                        selectedDates - date
                    } else {
                        selectedDates + date
                    }
                })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableCalendar(
    themeColor: Color, selectedDates: Set<LocalDate>, onDateClick: (LocalDate) -> Unit
) {
    val initialPage = 5000
    val pagerState = rememberPagerState(initialPage = initialPage) { 10000 }

    val currentMonth = remember(pagerState.currentPage) {
        val offset = pagerState.currentPage - initialPage
        YearMonth.now().plusMonths(offset.toLong())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        Text(
            text = "$monthName ${currentMonth.year}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        HorizontalDivider(color = GridBorderColor, thickness = 1.dp)
        WeekDaysHeader()
        HorizontalDivider(color = GridBorderColor, thickness = 1.dp)

        HorizontalPager(
            state = pagerState, modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top
        ) { page ->
            val monthForPage = remember(page) {
                val offset = page - initialPage
                YearMonth.now().plusMonths(offset.toLong())
            }

            CalendarMonthGrid(
                yearMonth = monthForPage,
                themeColor = themeColor,
                selectedDates = selectedDates,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
fun WeekDaysHeader() {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        days.forEachIndexed { index, day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (index == 6) Color.Red else Color.Gray
            )
        }
    }
}

@Composable
fun CalendarMonthGrid(
    yearMonth: YearMonth,
    themeColor: Color,
    selectedDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInGrid = remember(yearMonth) { getCalendarDays(yearMonth) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GridBorderColor),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        for (row in 0 until 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp), // Fixed height for a uniform grid
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                for (col in 0 until 7) {
                    val date = daysInGrid[row * 7 + col]
                    val isCurrentMonth = date.month == yearMonth.month

                    CalendarCell(
                        date = date,
                        themeColor = themeColor,
                        isSelected = selectedDates.contains(date),
                        isCurrentMonth = isCurrentMonth,
                        isSunday = date.dayOfWeek == DayOfWeek.SUNDAY,
                        onClick = { onDateClick(date) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarCell(
    date: LocalDate,
    themeColor: Color,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    isSunday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Light background for selected state (15% opacity)
    val backgroundColor = if (isSelected) themeColor.copy(alpha = 0.15f) else Color.White
    val textColor = when {
        !isCurrentMonth -> TextGray
        isSunday -> Color.Red
        else -> Color.Black
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable { onClick() }) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 8.dp, top = 6.dp)
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .align(Alignment.BottomCenter)
                    .background(themeColor)
            )
        }
    }
}

@Composable
fun BottomDoneButton(
    themeColor: Color, enabled: Boolean = true, onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = themeColor,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 45.dp, start = 15.dp, end = 15.dp),
    ) {
        Text(
            text = "Done", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
        )
    }
}

fun getCalendarDays(yearMonth: YearMonth): List<LocalDate> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val offset = firstDayOfWeek - 1
    val startDate = firstDayOfMonth.minusDays(offset.toLong())

    return (0 until 42).map { startDate.plusDays(it.toLong()) }
}