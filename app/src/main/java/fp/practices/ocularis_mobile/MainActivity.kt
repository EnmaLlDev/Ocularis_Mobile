package fp.practices.ocularis_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fp.practices.ocularis_mobile.ui.screens.HomeScreen
import fp.practices.ocularis_mobile.ui.screens.AppointmentsScreen
import fp.practices.ocularis_mobile.ui.screens.DetailsScreen
import fp.practices.ocularis_mobile.ui.screens.DoctorsScreen
import fp.practices.ocularis_mobile.ui.screens.PatientsScreen
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ocularis_MobileTheme {
                var selectedTab by remember { mutableStateOf(0) }
                val tabs = listOf("Inicio", "Pacientes", "Doctores", "Citas", "Detalles")
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(innerPadding)) {
                        TabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { androidx.compose.material3.Text(title) }
                                )
                            }
                        }
                        when (selectedTab) {
                            0 -> HomeScreen(modifier = Modifier.fillMaxSize())
                            1 -> PatientsScreen(modifier = Modifier.fillMaxSize())
                            2 -> DoctorsScreen(modifier = Modifier.fillMaxSize())
                            3 -> AppointmentsScreen(modifier = Modifier.fillMaxSize())
                            4 -> DetailsScreen(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
