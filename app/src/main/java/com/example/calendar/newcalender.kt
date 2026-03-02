package com.example.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.calendar.RoomDatabase.CalendarDatabase
import com.example.calendar.RoomDatabase.CalendarEntity
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class newcalender : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val activity = context as ComponentActivity

            var calendarName by remember { mutableStateOf("") }
            var calendarColor by remember { mutableStateOf(Color.Red) }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Topbar(
                        onBack = { activity.finish() },
                        onDoneClick = {
                            if (calendarName.isBlank()) calendarName = "Untitled Calendar"
                            scope.launch(Dispatchers.IO) {
                                val db = CalendarDatabase.getDatabase(context)
                                db.calendarDao().insertCalendar(
                                    CalendarEntity(
                                        name = calendarName,
                                        color = calendarColor.toArgb()
                                    )
                                )
                            }
                            activity.finish()
                        }
                    )
                }
            ) { innerPadding ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(240, 240, 240))
                        .padding(innerPadding)
                ) {
                    ColorCode(
                        calendarName = calendarName,
                        onNameChange = { calendarName = it },
                        calendarColor = calendarColor,
                        onColorSelected = { calendarColor = it }
                    )
                }
            }
        }


    }

    @Composable
    fun ColorCode(
        calendarName: String,
        onNameChange: (String) -> Unit,
        calendarColor: Color,
        onColorSelected: (Color) -> Unit
    ) {
        Column {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Display example", fontSize = 15.sp, modifier = Modifier.padding(start = 10.dp))
            Row {
                for (i in 11..17) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .background(Color.White)
                            .border(0.5.dp, Color.LightGray)
                    ) {
                        Text(
                            "$i",
                            color = if (i == 11) calendarColor else Color.Black,
                            modifier = Modifier.padding(6.dp),
                            fontSize = 12.sp
                        )

                        if (i == 12 || i == 16) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 22.dp)
                                    .background(calendarColor)
                            ) {
                                Text("Example", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Calendar name", fontSize = 15.sp, modifier = Modifier.padding(start = 10.dp))
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = calendarName,
                onValueChange = onNameChange,
                placeholder = {
                    Text(
                        text = "Enter the Title", color = Color.LightGray
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .shadow(
                        elevation = 10.dp,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
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

            Spacer(modifier = Modifier.padding(5.dp))
            Text("Color", fontSize = 15.sp, modifier = Modifier.padding(start = 10.dp))
            CalendarColorPicker(
                selectedColor = calendarColor,
                onColorSelected = onColorSelected
            )
        }
    }


    data class ColorCategory(
        val title: String,
        val colors: List<Color>
    )

    val colorCategories = listOf(
        ColorCategory(
            "Default",
            listOf(
                Color(0xFFE53935),
                Color(0xFFF06292),
                Color(0xFFFFA726),
                Color(0xFF66BB6A),
                Color(0xFF42A5F5),
                Color(0xFFBA68C8)
            )
        ),
        ColorCategory(
            "Simple",
            listOf(
                Color(0xFFFF5252),
                Color(0xFFFFD54F),
                Color(0xFF9CCC65),
                Color(0xFF80DEEA),
                Color(0xFF5C6BC0),
                Color(0xFFAB47BC)
            )
        ),
        ColorCategory(
            "Fancy",
            listOf(
                Color(0xFFFF8A80),
                Color(0xFFFF80AB),
                Color(0xFFFFCC80),
                Color(0xFF90CAF9),
                Color(0xFF69F0AE),
                Color(0xFFE1BEE7)
            )
        ),
        ColorCategory(
            "Pastel",
            listOf(
                Color(0xFFFFCDD2),
                Color(0xFFF8BBD0),
                Color(0xFFFFE0B2),
                Color(0xFFB2DFDB),
                Color(0xFFBBDEFB),
                Color(0xFFC5CAE9)
            )
        ),
        ColorCategory(
            "Cool",
            listOf(
                Color(0xFF264653),
                Color(0xFF457B9D),
                Color(0xFFADB5BD),
                Color(0xFF6C757D),
                Color(0xFF8D99AE),
                Color(0xFFC0C0C0)
            )
        ),
        ColorCategory(
            "Chic",
            listOf(
                Color(0xFF9E2A2B),
                Color(0xFFB08968),
                Color(0xFF6D597A),
                Color(0xFF6C757D),
                Color(0xFF344E41),
                Color(0xFF000000)
            )
        )
    )


    @Composable
    fun CalendarColorPicker(
        selectedColor: Color,
        onColorSelected: (Color) -> Unit
    ) {
        var showPicker by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {

            colorCategories.forEachIndexed { index, category ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(category.title, modifier = Modifier.width(70.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        category.colors.forEach { color ->
                            ColorCircle(
                                color = color,
                                isSelected = color == selectedColor
                            ) { onColorSelected(color) }
                        }
                    }
                }

                if (index != colorCategories.lastIndex) {
                    Divider(thickness = 0.5.dp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Custom", modifier = Modifier.width(70.dp))

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color.Red, Color.Yellow, Color.Green,
                                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                )
                            ),
                            CircleShape
                        )
                        .clickable { showPicker = true }
                )
            }
        }

        ColorPickerDialog(
            show = showPicker,
            onDismiss = { showPicker = false },
            onColorSelected = onColorSelected
        )
    }


    @Composable
    fun ColorCircle(
        color: Color,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Topbar(
        onBack: () -> Unit,
        onDoneClick: () -> Unit
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            title = {
                Text(
                    "Create new calendar",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, null)
                }
            },
            actions = {
                TextButton(onClick = onDoneClick) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }


    @Composable
    fun ColorPickerDialog(
        show: Boolean,
        onDismiss: () -> Unit,
        onColorSelected: (Color) -> Unit
    ) {
        if (show) {
            Dialog(onDismissRequest = onDismiss) {

                val controller = rememberColorPickerController()

                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Colour",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🎨 COLOR WHEEL
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            HsvColorPicker(
                                modifier = Modifier
                                    .size(250.dp),
                                controller = controller
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // 🌈 SLIDER
                        AlphaSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            controller = controller
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }

                            TextButton(
                                onClick = {
                                    onColorSelected(controller.selectedColor.value)
                                    onDismiss()
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }

}

