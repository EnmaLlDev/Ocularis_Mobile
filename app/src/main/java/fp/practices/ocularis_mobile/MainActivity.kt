package fp.practices.ocularis_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fp.practices.ocularis_mobile.data.network.RetrofitClient
import fp.practices.ocularis_mobile.ui.screens.DetailsScreen
import fp.practices.ocularis_mobile.ui.screens.DoctorsScreen
import fp.practices.ocularis_mobile.ui.screens.HomeScreen
import fp.practices.ocularis_mobile.ui.screens.LoginScreen
import fp.practices.ocularis_mobile.ui.screens.PatientsScreen
import fp.practices.ocularis_mobile.ui.screens.AppointmentsScreen
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(this)
        enableEdgeToEdge()
        setContent {
            Ocularis_MobileTheme {
                AppNavigation()
            }
        }
    }
}

private enum class TopSection {
    HOME,
    PATIENTS,
    DOCTORS,
    APPOINTMENTS,
    DETAILS
}

@Composable
private fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(authState.isAuthenticated, authState.isCheckingSession) {
        if (authState.isCheckingSession) return@LaunchedEffect

        val target = if (authState.isAuthenticated) "app" else "login"
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    if (authState.isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) "app" else "login"
    ) {
        composable("login") {
            LoginScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                onLogin = authViewModel::login,
                onClearError = authViewModel::clearError
            )
        }

        composable("app") {
            AppShell(
                roles = authState.roles,
                username = authState.userInfo?.username,
                onLogout = authViewModel::logout
            )
        }
    }
}

@Composable
private fun AppShell(
    roles: Set<String>,
    username: String?,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(TopSection.HOME) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavigationRail(modifier = Modifier.width(96.dp)) {
                NavigationRailItem(
                    selected = selectedSection == TopSection.HOME,
                    onClick = { selectedSection = TopSection.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationRailItem(
                    selected = selectedSection == TopSection.PATIENTS,
                    onClick = { selectedSection = TopSection.PATIENTS },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Pacientes") },
                    label = { Text("Pacientes") }
                )
                NavigationRailItem(
                    selected = selectedSection == TopSection.DOCTORS,
                    onClick = { selectedSection = TopSection.DOCTORS },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Doctores") },
                    label = { Text("Doctores") }
                )
                NavigationRailItem(
                    selected = selectedSection == TopSection.APPOINTMENTS,
                    onClick = { selectedSection = TopSection.APPOINTMENTS },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Citas") },
                    label = { Text("Citas") }
                )
                NavigationRailItem(
                    selected = selectedSection == TopSection.DETAILS,
                    onClick = { selectedSection = TopSection.DETAILS },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Detalles") },
                    label = { Text("Detalles") }
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    NavigationRailItem(
                        selected = false,
                        onClick = onLogout,
                        icon = { Icon(Icons.Default.Delete, contentDescription = "Cerrar sesión") },
                        label = { Text("Salir") }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                val roleText = roles.sorted().joinToString(separator = ", ").ifBlank { "sin rol" }
                Text(
                    text = "Usuario: ${username ?: "-"} | Rol: $roleText",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when (selectedSection) {
                    TopSection.HOME -> HomeScreen(modifier = Modifier.fillMaxSize())
                    TopSection.PATIENTS -> PatientsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                    TopSection.DOCTORS -> DoctorsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                    TopSection.APPOINTMENTS -> AppointmentsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                    TopSection.DETAILS -> DetailsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                }
            }
        }
    }
}
