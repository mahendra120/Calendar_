package com.example.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.calendar.RoomDatabase.CalendarDatabase
import com.example.calendar.RoomDatabase.RegionEntity
import kotlinx.coroutines.launch

class HolidaysActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                topbar()
            }) { innerPadding ->
                holidaypage(innerPadding)
            }
        }
    }

    data class HolidayRegion(
        val name: String,
        val code: String
    )

    val holidayRegions = listOf(
        HolidayRegion("United Kingdom", "UK"),
        HolidayRegion("All", "ALL"),
        HolidayRegion("Afghanistan", "AF"),
        HolidayRegion("Albania", "AL"),
        HolidayRegion("Algeria", "DZ"),
        HolidayRegion("Andorra", "AD"),
        HolidayRegion("Angola", "AO"),
        HolidayRegion("Antigua and Barbuda", "AG"),
        HolidayRegion("Argentina", "AR"),
        HolidayRegion("Armenia", "AM"),
        HolidayRegion("Australia", "AU"),
        HolidayRegion("Austria", "AT"),
        HolidayRegion("Azerbaijan", "AZ"),
        HolidayRegion("Bahamas", "BS"),
        HolidayRegion("Bahrain", "BH"),
        HolidayRegion("Bangladesh", "BD"),
        HolidayRegion("Barbados", "BB"),
        HolidayRegion("Belarus", "BY"),
        HolidayRegion("Belgium", "BE"),
        HolidayRegion("Belize", "BZ"),
        HolidayRegion("Benin", "BJ"),
        HolidayRegion("Bhutan", "BT"),
        HolidayRegion("Bolivia", "BO"),
        HolidayRegion("Bosnia and Herzegovina", "BA"),
        HolidayRegion("Botswana", "BW"),
        HolidayRegion("Brazil", "BR"),
        HolidayRegion("Brunei", "BN"),
        HolidayRegion("Bulgaria", "BG"),
        HolidayRegion("Burkina Faso", "BF"),
        HolidayRegion("Burundi", "BI"),
        HolidayRegion("Cambodia", "KH"),
        HolidayRegion("Cameroon", "CM"),
        HolidayRegion("Canada", "CA"),
        HolidayRegion("Cape Verde", "CV"),
        HolidayRegion("Central African Republic", "CF"),
        HolidayRegion("Chad", "TD"),
        HolidayRegion("Chile", "CL"),
        HolidayRegion("China", "CN"),
        HolidayRegion("Colombia", "CO"),
        HolidayRegion("Comoros", "KM"),
        HolidayRegion("Congo", "CG"),
        HolidayRegion("Costa Rica", "CR"),
        HolidayRegion("Croatia", "HR"),
        HolidayRegion("Cuba", "CU"),
        HolidayRegion("Cyprus", "CY"),
        HolidayRegion("Czech Republic", "CZ"),
        HolidayRegion("Denmark", "DK"),
        HolidayRegion("Djibouti", "DJ"),
        HolidayRegion("Dominica", "DM"),
        HolidayRegion("Dominican Republic", "DO"),
        HolidayRegion("Ecuador", "EC"),
        HolidayRegion("Egypt", "EG"),
        HolidayRegion("El Salvador", "SV"),
        HolidayRegion("Equatorial Guinea", "GQ"),
        HolidayRegion("Eritrea", "ER"),
        HolidayRegion("Estonia", "EE"),
        HolidayRegion("Eswatini", "SZ"),
        HolidayRegion("Ethiopia", "ET"),
        HolidayRegion("Fiji", "FJ"),
        HolidayRegion("Finland", "FI"),
        HolidayRegion("France", "FR"),
        HolidayRegion("Gabon", "GA"),
        HolidayRegion("Gambia", "GM"),
        HolidayRegion("Georgia", "GE"),
        HolidayRegion("Germany", "DE"),
        HolidayRegion("Ghana", "GH"),
        HolidayRegion("Greece", "GR"),
        HolidayRegion("Grenada", "GD"),
        HolidayRegion("Guatemala", "GT"),
        HolidayRegion("Guinea", "GN"),
        HolidayRegion("Guinea-Bissau", "GW"),
        HolidayRegion("Guyana", "GY"),
        HolidayRegion("Haiti", "HT"),
        HolidayRegion("Honduras", "HN"),
        HolidayRegion("Hungary", "HU"),
        HolidayRegion("Iceland", "IS"),
        HolidayRegion("India", "IN"),
        HolidayRegion("Indonesia", "ID"),
        HolidayRegion("Iran", "IR"),
        HolidayRegion("Iraq", "IQ"),
        HolidayRegion("Ireland", "IE"),
        HolidayRegion("Israel", "IL"),
        HolidayRegion("Italy", "IT"),
        HolidayRegion("Jamaica", "JM"),
        HolidayRegion("Japan", "JP"),
        HolidayRegion("Jordan", "JO"),
        HolidayRegion("Kazakhstan", "KZ"),
        HolidayRegion("Kenya", "KE"),
        HolidayRegion("Kiribati", "KI"),
        HolidayRegion("Kuwait", "KW"),
        HolidayRegion("Kyrgyzstan", "KG"),
        HolidayRegion("Laos", "LA"),
        HolidayRegion("Latvia", "LV"),
        HolidayRegion("Lebanon", "LB"),
        HolidayRegion("Lesotho", "LS"),
        HolidayRegion("Liberia", "LR"),
        HolidayRegion("Libya", "LY"),
        HolidayRegion("Liechtenstein", "LI"),
        HolidayRegion("Lithuania", "LT"),
        HolidayRegion("Luxembourg", "LU"),
        HolidayRegion("Madagascar", "MG"),
        HolidayRegion("Malawi", "MW"),
        HolidayRegion("Malaysia", "MY"),
        HolidayRegion("Maldives", "MV"),
        HolidayRegion("Mali", "ML"),
        HolidayRegion("Malta", "MT"),
        HolidayRegion("Marshall Islands", "MH"),
        HolidayRegion("Mauritania", "MR"),
        HolidayRegion("Mauritius", "MU"),
        HolidayRegion("Mexico", "MX"),
        HolidayRegion("Micronesia", "FM"),
        HolidayRegion("Moldova", "MD"),
        HolidayRegion("Monaco", "MC"),
        HolidayRegion("Mongolia", "MN"),
        HolidayRegion("Montenegro", "ME"),
        HolidayRegion("Morocco", "MA"),
        HolidayRegion("Mozambique", "MZ"),
        HolidayRegion("Myanmar", "MM"),
        HolidayRegion("Namibia", "NA"),
        HolidayRegion("Nepal", "NP"),
        HolidayRegion("Netherlands", "NL"),
        HolidayRegion("New Zealand", "NZ"),
        HolidayRegion("Nicaragua", "NI"),
        HolidayRegion("Niger", "NE"),
        HolidayRegion("Nigeria", "NG"),
        HolidayRegion("North Korea", "KP"),
        HolidayRegion("North Macedonia", "MK"),
        HolidayRegion("Norway", "NO"),
        HolidayRegion("Oman", "OM"),
        HolidayRegion("Pakistan", "PK"),
        HolidayRegion("Panama", "PA"),
        HolidayRegion("Papua New Guinea", "PG"),
        HolidayRegion("Paraguay", "PY"),
        HolidayRegion("Peru", "PE"),
        HolidayRegion("Philippines", "PH"),
        HolidayRegion("Poland", "PL"),
        HolidayRegion("Portugal", "PT"),
        HolidayRegion("Qatar", "QA"),
        HolidayRegion("Romania", "RO"),
        HolidayRegion("Russia", "RU"),
        HolidayRegion("Rwanda", "RW"),
        HolidayRegion("Saudi Arabia", "SA"),
        HolidayRegion("Senegal", "SN"),
        HolidayRegion("Serbia", "RS"),
        HolidayRegion("Singapore", "SG"),
        HolidayRegion("Slovakia", "SK"),
        HolidayRegion("Slovenia", "SI"),
        HolidayRegion("South Africa", "ZA"),
        HolidayRegion("South Korea", "KR"),
        HolidayRegion("Spain", "ES"),
        HolidayRegion("Sri Lanka", "LK"),
        HolidayRegion("Sudan", "SD"),
        HolidayRegion("Sweden", "SE"),
        HolidayRegion("Switzerland", "CH"),
        HolidayRegion("Syria", "SY"),
        HolidayRegion("Thailand", "TH"),
        HolidayRegion("Turkey", "TR"),
        HolidayRegion("Ukraine", "UA"),
        HolidayRegion("United Arab Emirates", "AE"),
        HolidayRegion("United Kingdom", "GB"),
        HolidayRegion("United States", "US"),
        HolidayRegion("Vietnam", "VN"),
        HolidayRegion("Yemen", "YE"),
        HolidayRegion("Zambia", "ZM"),
        HolidayRegion("Zimbabwe", "ZW")
    )

    private fun saveRegion(region: HolidayRegion) {
        val db = CalendarDatabase.getDatabase(this)

        lifecycleScope.launch {
            db.regionDao().saveRegion(
                RegionEntity(
                    code = region.code,
                    name = region.name
                )
            )
        }
    }


    @Composable
    fun holidaypage(innerPadding: PaddingValues) {

        var searchText by remember { mutableStateOf("") }

        val filteredList: List<HolidayRegion> = holidayRegions.filter {
            it.name.contains(searchText, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(244, 244, 244))
        ) {

            item {
                Text(
                    text = "Please choose a region for the holiday.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = {
                            Text(
                                text = "Enter a holiday region",
                                color = Color.LightGray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }


            item {
                Text(
                    text = "Suggested",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            items(filteredList) { region ->
                RegionRow(region) {
                    saveRegion(region)
                    finish()
                }
            }
        }
    }

    @Composable
    fun RegionRow(region: HolidayRegion, onClick: () -> Unit) {

        val bgColor =
            if (region.name.equals("All", ignoreCase = true))
                Color(0xFFEBEBEB)
            else
                Color.White


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = region.name,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Divider(thickness = 0.5.dp, color = Color.LightGray)
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topbar() {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Add regional holldays", fontSize = 19.sp)
                }
            }, navigationIcon = {
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

