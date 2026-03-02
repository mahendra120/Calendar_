package com.example.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.calendar.RoomDatabase.CalendarColorPrefs
import com.example.calendar.RoomDatabase.CalendarDisplayPrefs
import com.example.calendar.RoomDatabase.EventEntity
import com.example.calendar.RoomDatabase.colosave.loadThemeColor
import com.example.calendar.ui.theme.CalendarTheme

class SettingPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettingScreen()
        }
    }


@Composable
fun SettingScreen() {

    val context = LocalContext.current

    var selectedColor by remember {
        mutableStateOf(loadThemeColor(this@SettingPage))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                selectedColor = loadThemeColor(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { topbar(selectedColor) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(Color(0, 0, 0))
        ) {
            UI(
                selectedColor = selectedColor,
                onRefreshColor = {
                    selectedColor = loadThemeColor(context)
                }
            )
        }
    }
}

    @Composable
    fun UI(
        selectedColor: Color,
        onRefreshColor: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(244, 244, 244))
        ) {
            Spacer(modifier = Modifier.padding(24.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = "Remove ads",
                        tint = selectedColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Remove ads",
                        color = Color(0, 0, 0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.padding(12.dp))
            Text(
                "Design",
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp),
                textAlign = TextAlign.Start,
                color = Color.LightGray
            )
            color_theme(selectedColor)
        }
    }

    @Composable
    fun color_theme(selectedColor: Color) {
        Spacer(modifier = Modifier.padding(2.dp))
        Button(
            onClick = {
                val intent = Intent(this@SettingPage, Color_Theme::class.java)
                startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = "Remove ads",
                    tint = selectedColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Theme Colour",
                    color = Color(0, 0, 0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Simple orange",
                    color = Color(0, 0, 0),
                    fontSize = 13.sp
                )
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topbar(electedColor: Color) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            title = {
                Text(
                    text = "Settings",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0, 0, 0),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { finish() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = electedColor
                    )
                }
            },
            actions = {
                TextButton(onClick = { /* TODO help */ }) {
                    Text(
                        text = "Help",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = electedColor
                    )
                }
            }
        )
    }

//    override fun onResume() {
//        super.onResume()
//        setContent {
//            var selectedColor by remember {
//                mutableStateOf(loadThemeColor(this))
//            }
//        }
//    }
}

