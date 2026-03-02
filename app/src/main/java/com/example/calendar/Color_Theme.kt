package com.example.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.RoomDatabase.colosave

class Color_Theme : ComponentActivity() {
    val HeaderPinkCoral = Color(0xFFFF6B6B)
    val HeaderBrightPink = Color(0xFFFF69B4)
    val HeaderOrange = Color(255, 182, 193)
    val HeaderMintGreen = Color(0xFFAED581)
    val HeaderTeal = Color(0xFF80CBC4)
    val EventRedDrinking = Color(0xFFF44336)
    val EventBlueBusiness = Color(0xFF1976D2)
    val EventOrangeMeeting = Color(0xFFFF9800)
    val EventGreenLunch = Color(0xFF4CAF50)
    val black = Color(0xFF000000)

    var selectedTopColor by mutableStateOf(Color.Black)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = { topcolor() }) { innerPadding ->
                UI()
            }
        }
    }

    data class ThemeItem(
        val imageRes: Int,
        val themeColor: Color
    )

    @Composable
    fun UI() {

        val themeList = listOf(
            ThemeItem(R.drawable.image1, EventBlueBusiness),
            ThemeItem(R.drawable.image2, EventGreenLunch),
            ThemeItem(R.drawable.image3, EventOrangeMeeting),
            ThemeItem(R.drawable.image4, EventRedDrinking),
            ThemeItem(R.drawable.image5, HeaderPinkCoral),
            ThemeItem(R.drawable.image6, HeaderMintGreen),
            ThemeItem(R.drawable.image11, HeaderTeal),
            ThemeItem(R.drawable.image8, HeaderBrightPink),
            ThemeItem(R.drawable.image9, HeaderOrange),
            ThemeItem(R.drawable.image18, Color.Black)
        )

        val pagerState = rememberPagerState(
            pageCount = { themeList.size }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->

                    val item = themeList[page]

                    Image(
                        painter = painterResource(item.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(0.dp))

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(bottom = 15.dp)
            ) {
                repeat(themeList.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 6.dp else 5.dp)
                            .background(
                                color = if (isSelected) Color.Black else Color.LightGray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            val currentColor = themeList[pagerState.currentPage].themeColor

            LaunchedEffect(currentColor) {
                selectedTopColor = currentColor
            }

            val animatedColor by animateColorAsState(
                targetValue = currentColor,
                label = "buttonColor"
            )
            Button(
                onClick = {
                    colosave.saveThemeColor(this@Color_Theme, selectedTopColor)
                    finish()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = 20.dp)
            )
            {
                Text("Set", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topcolor() {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Theme colour settings",
                        fontSize = 18.sp,
                        color = selectedTopColor,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    finish()
                })
                {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = null,
                        tint = selectedTopColor
                    )
                }
            })
    }
}