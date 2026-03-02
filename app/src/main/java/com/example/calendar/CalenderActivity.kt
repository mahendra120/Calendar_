package com.example.calendar

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.RoomDatabase.CalendarColorPrefs
import com.example.calendar.RoomDatabase.CalendarDatabase
import com.example.calendar.RoomDatabase.CalendarDisplayPrefs
import com.example.calendar.RoomDatabase.CalendarPrefs
import com.example.calendar.RoomDatabase.EventViewModel
import com.example.calendar.RoomDatabase.Prefs
import com.example.calendar.RoomDatabase.colosave.loadThemeColor
import com.example.calendar.RoomDatabase.localAccountPrefs
import com.example.calendar.ui.theme.CalendarTheme

class CalenderActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = CalendarDatabase.getDatabase(this)
        val dao = db.calendarDao()



        setContent {

            val context = LocalContext.current

            var selectedColor by remember {
                mutableStateOf(Color(CalendarColorPrefs.load(context)))
            }

            LaunchedEffect(Unit) {
                selectedColor = Color(CalendarColorPrefs.load(context))
            }

            CalendarTheme {

                val viewModel: CalendarViewModel = viewModel(
                    factory = EventViewModelFactory(application)
                )

                val regionViewModel: RegionViewModel = viewModel(
                    factory = RegionViewModelFactory(
                        CalendarDatabase.getDatabase(this).regionDao()
                    )
                )

                Scaffold(
                    topBar = { Topbar() }
                ) { padding ->
                    CalendarScreen(
                        padding = padding,
                        viewModel = viewModel,
                        regionViewModel = regionViewModel,
                        onCreateCalendar = {
                            startActivity(
                                Intent(this, newcalender::class.java)
                            )
                        }
                    )
                }
            }
        }
    }

    enum class SelectionType {
        NONE,
        LOCAL,
        REGIONAL
    }


    @Composable
    fun CalendarScreen(
        padding: PaddingValues,
        viewModel: CalendarViewModel,
        regionViewModel: RegionViewModel,
        onCreateCalendar: () -> Unit
    ) {

        val calendars by viewModel.calendars.collectAsState(initial = emptyList())
        val regions by regionViewModel.regions.collectAsState(initial = emptyList())

        var selectedType by rememberSaveable {
            mutableStateOf(SelectionType.NONE)
        }

        var selectedCalendarId by rememberSaveable {
            mutableStateOf<Int?>(null)
        }

        var selectedRegionCodes by rememberSaveable {
            mutableStateOf<Set<String>>(emptySet())
        }

        val context = this@CalenderActivity

        LaunchedEffect(Unit) {

            val savedCalendars = CalendarPrefs.loadSelectedCalendars(context)
            val savedRegions = Prefs.loadSelectedRegions(context)
            val isLocal = localAccountPrefs.loadLocalAccount(context)

            when {
                isLocal -> {
                    selectedType = SelectionType.NONE
                    selectedCalendarId = null
                    selectedRegionCodes = emptySet()
                }

                savedCalendars.isNotEmpty() -> {
                    selectedType = SelectionType.LOCAL
                    selectedCalendarId = savedCalendars.first()
                    selectedRegionCodes = emptySet()
                }

                savedRegions.isNotEmpty() -> {
                    selectedType = SelectionType.REGIONAL
                    selectedCalendarId = null
                    selectedRegionCodes = savedRegions
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(244, 244, 244))
                .padding(padding)
        ) {

            item {
                SectionTitle("local account")
                SimpleRow(
                    text = "Local Account",
                    showDone = selectedType == SelectionType.NONE
                )
                {
                    selectedType = SelectionType.NONE

                    selectedCalendarId = null
                    selectedRegionCodes = emptySet()

                    CalendarPrefs.saveSelectedCalendars(this@CalenderActivity, emptySet())
                    Prefs.saveSelectedRegions(this@CalenderActivity, emptySet())
                    localAccountPrefs.saveLocalAccount(this@CalenderActivity, true)

                    CalendarDisplayPrefs.save(this@CalenderActivity, "Local Account")
                    CalendarColorPrefs.save(
                        this@CalenderActivity,
                        color = 0xFFADD8E6.toInt()
                    )
                    finish()
                }

                Spacer(Modifier.height(16.dp))
                SectionTitle("LOCAL")
            }

            items(calendars) { calendar ->
                CalendarRow(
                    name = calendar.name,
                    color = calendar.color,
                    showSelected =
                        selectedType == SelectionType.LOCAL &&
                                selectedCalendarId == calendar.id
                )
                {
                    selectedType = SelectionType.LOCAL
                    selectedCalendarId = calendar.id
                    selectedRegionCodes = emptySet()

                    CalendarPrefs.saveSelectedCalendars(
                        this@CalenderActivity,
                        setOf(calendar.id)
                    )

                    CalendarColorPrefs.save(
                        this@CalenderActivity,
                        calendar.color
                    )

                    setResult(
                        RESULT_OK,
                        Intent().putExtra("color", calendar.color)
                    )

                    Prefs.saveSelectedRegions(this@CalenderActivity, emptySet())
                    localAccountPrefs.saveLocalAccount(this@CalenderActivity, false)

                    CalendarDisplayPrefs.save(
                        this@CalenderActivity,
                        calendar.name
                    )
                    finish()
                }
            }


            item {
                ArrowRow("Create new calendar", onCreateCalendar)
                Spacer(Modifier.height(16.dp))
                SectionTitle("Regional Holidays")
            }

            items(regions) { region ->
                RegionalHolidayRow(
                    name = region.name,
                    showSelected =
                        selectedType == SelectionType.REGIONAL &&
                                selectedRegionCodes.contains(region.code)
                )
                {
                    selectedType = SelectionType.REGIONAL
                    selectedCalendarId = null
                    selectedRegionCodes = setOf(region.code)

                    CalendarPrefs.saveSelectedCalendars(this@CalenderActivity, emptySet())
                    Prefs.saveSelectedRegions(
                        this@CalenderActivity,
                        selectedRegionCodes
                    )
                    localAccountPrefs.saveLocalAccount(this@CalenderActivity, false)

                    CalendarDisplayPrefs.save(
                        this@CalenderActivity,
                        region.name
                    )
                    finish()
                }
            }

            item {
                ArrowRow("Add regional holidays", {
                    val intent = Intent(this@CalenderActivity, HolidaysActivity::class.java)
                    startActivity(intent)
                })
                Spacer(Modifier.height(50.dp))
            }
        }
    }

    @Composable
    fun SectionTitle(text: String) {
        Spacer(modifier = Modifier.padding(6.dp))
        Text(
            text,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 13.dp, bottom = 6.dp),
            color = Color.Black
        )
    }

    @Composable
    fun SimpleRow(text: String, showDone: Boolean = false, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(Color.White)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text, fontWeight = FontWeight.Bold, color = Color.Black)
                if (showDone) {
                    Icon(Icons.Default.Done, null, tint = Color(0xFF2C8011))
                }
            }
        }
    }

    @Composable
    fun CalendarRow(name: String, color: Int, showSelected: Boolean, onclick: () -> Unit = {}) {
        Button(
            onClick = onclick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            border = BorderStroke(0.1.dp, Color.LightGray),
            colors = ButtonDefaults.buttonColors(Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(22.dp),
                    colors = CardDefaults.cardColors(Color(color))
                ) {}
                Spacer(Modifier.width(12.dp))
                Text(name, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.weight(1f))
                if (showSelected) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        tint = Color(0xFF2C8011)
                    )
                }
            }
        }
    }

    @Composable
    fun ArrowRow(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            border = BorderStroke(0.1.dp, Color.LightGray),
            colors = ButtonDefaults.buttonColors(Color.White)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text, fontWeight = FontWeight.Bold, color = Color.Black)
                Icon(
                    Icons.Default.ArrowForwardIos,
                    null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    @Composable
    fun RegionalHolidayRow(
        name: String,
        showSelected: Boolean,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(modifier = Modifier.size(22.dp)) {}
                Spacer(modifier = Modifier.width(10.dp))
                Text(name, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.weight(1f))

                if (showSelected) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        tint = Color(0xFF2C8011)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Topbar() {
        var themecolor by remember {
            mutableStateOf(loadThemeColor(this@CalenderActivity))
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    themecolor = loadThemeColor(this@CalenderActivity)
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

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
                    Text(
                        text = "Calendar",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { finish() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = themecolor
                    )
                }
            },
        )
    }


//    override fun onResume() {
//        super.onResume()
//        selectedColor = Color(CalendarColorPrefs.load(this))
//    }


}
