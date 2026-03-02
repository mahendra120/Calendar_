package com.example.calendar

import android.app.Application
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.RoomDatabase.CalendarDatabase
import com.example.calendar.RoomDatabase.CalendarPrefs
import com.example.calendar.RoomDatabase.EventViewModel
import com.example.calendar.RoomDatabase.Prefs
import com.example.calendar.RoomDatabase.localAccountPrefs

class DisplaySettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = CalendarDatabase.getDatabase(this)
        val dao = db.calendarDao()
        setContent {
            val viewModel: CalendarViewModel = viewModel(
                factory = EventViewModelFactory(application)
            )

            val regionViewModel: RegionViewModel = viewModel(
                factory = RegionViewModelFactory(
                    CalendarDatabase.getDatabase(this).regionDao()
                )
            )
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = { Topbar() }) { padding ->
                Displaysetting(
                    padding = padding,
                    viewModel = viewModel,
                    regionViewModel = regionViewModel,
                )
            }
        }
    }

    @Composable
    fun Displaysetting(
        padding: PaddingValues,
        viewModel: CalendarViewModel,
        regionViewModel: RegionViewModel,
    ) {
        val context = LocalContext.current
        val calendars by viewModel.calendars.collectAsState(initial = emptyList())
        val regions by regionViewModel.regions.collectAsState(initial = emptyList())


            var showDone by remember {
                mutableStateOf(localAccountPrefs.loadLocalAccount(context))
            }


        var selectedCalendarIds by rememberSaveable {
            mutableStateOf<Set<Int>>(emptySet())
        }

        var selectedRegionCodes by rememberSaveable {
            mutableStateOf<Set<String>>(emptySet())
        }

        LaunchedEffect(Unit) {
            selectedCalendarIds =
                CalendarPrefs.loadSelectedCalendars(context)

            selectedRegionCodes =
                Prefs.loadSelectedRegions(context)
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(244, 244, 244))
                .padding(padding)
        ) {

            item {

                Spacer(modifier = Modifier.height(15.dp))
                SectionTitle("local account")
                SimpleRow("local account", showDone = showDone) {
                    showDone = !showDone

                    if (showDone) {
                        // clear others
                        selectedCalendarIds = emptySet()
                        selectedRegionCodes = emptySet()

                        CalendarPrefs.saveSelectedCalendars(context, emptySet())
                        Prefs.saveSelectedRegions(context, emptySet())
                    }

                    localAccountPrefs.saveLocalAccount(context, showDone)
                }

                Spacer(Modifier.height(16.dp))
                SectionTitle("LOCAL")
            }

            items(
                items = calendars,
                key = { it.id }) { calendar ->

                val isSelected = selectedCalendarIds.contains(calendar.id)

                CalendarRow(
                    name = calendar.name,
                    color = calendar.color,
                    showSelected = isSelected
                ) {

                    selectedCalendarIds =

                        if (isSelected) {
                            selectedCalendarIds - calendar.id
                        } else {
                            selectedCalendarIds + calendar.id
                        }

                    CalendarPrefs.saveSelectedCalendars(
                        context,
                        selectedCalendarIds
                    )
                }
            }



            item {
                Spacer(Modifier.height(13.dp))
                SectionTitle("Regional Holidays")
            }

            items(regions) { region ->

                val isSelected = selectedRegionCodes.contains(region.code)

                RegionalHolidayRow(
                    name = region.name,
                    showSelected = isSelected
                ) {

                    val updatedSet =
                        if (isSelected) {
                            selectedRegionCodes - region.code
                        } else {
                            selectedRegionCodes + region.code
                        }
                    selectedRegionCodes = updatedSet
                    Prefs.saveSelectedRegions(context, updatedSet)
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }

    @Composable
    fun CalendarRow(
        name: String,
        color: Int,
        showSelected: Boolean,
        onClick: () -> Unit
    ) {
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Card(
                    modifier = Modifier.size(22.dp),
                    colors = CardDefaults.cardColors(Color(color)),
                ) {}

                Spacer(Modifier.width(12.dp))

                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.weight(1f))

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
    fun SimpleRow(text: String, showDone: Boolean, onClick: () -> Unit = {}) {
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
            border = BorderStroke(0.1.dp, Color.LightGray),
            colors = ButtonDefaults.buttonColors(Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Card(
                    modifier = Modifier.size(22.dp),
                    colors = CardDefaults.cardColors(Color(0xFFFFCDD2))
                ) {}

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Holiday in $name",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

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
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "Calendar",
                        fontSize = 19.sp,
                        modifier = Modifier.padding(end = 29.dp),
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
                        tint = Color(0xFF1A73E8)
                    )
                }
            }
        )
    }
}

