package fp.practices.ocularis_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fp.practices.ocularis_mobile.ui.screens.DetailsScreen
import fp.practices.ocularis_mobile.ui.screens.DoctorsScreen
import fp.practices.ocularis_mobile.ui.screens.HomeScreen
import fp.practices.ocularis_mobile.ui.screens.LoginScreen
import fp.practices.ocularis_mobile.ui.screens.PatientsScreen
import fp.practices.ocularis_mobile.ui.screens.AppointmentsScreen
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

private data class SectionItem(
    val section: TopSection,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(
    roles: Set<String>,
    username: String?,
    onLogout: () -> Unit
) {
    val isAdmin = roles.contains("ADMIN")
    val isDoctor = roles.contains("DOCTOR")
    val isPatient = roles.contains("PATIENT")
    var selectedSection by remember { mutableStateOf(TopSection.HOME) }
    val roleText = roles.sorted().joinToString(separator = ", ").ifBlank { "sin rol" }
    val sections = buildList {
        add(SectionItem(TopSection.HOME, "Inicio", Icons.Default.Home))
        when {
            isAdmin -> {
                add(SectionItem(TopSection.PATIENTS, "Pacientes", Icons.AutoMirrored.Filled.List))
                add(SectionItem(TopSection.DOCTORS, "Doctores", Icons.Default.Favorite))
                add(SectionItem(TopSection.APPOINTMENTS, "Citas", Icons.Default.Refresh))
                add(SectionItem(TopSection.DETAILS, "Detalles", Icons.Default.Edit))
            }
            isDoctor -> {
                add(SectionItem(TopSection.PATIENTS, "Pacientes", Icons.AutoMirrored.Filled.List))
                add(SectionItem(TopSection.APPOINTMENTS, "Agenda", Icons.Default.Refresh))
                add(SectionItem(TopSection.DETAILS, "Detalles", Icons.Default.Edit))
            }
            isPatient -> {
                add(SectionItem(TopSection.APPOINTMENTS, "Mis citas", Icons.Default.Refresh))
                add(SectionItem(TopSection.DETAILS, "Mi historial", Icons.Default.Edit))
            }
        }
    }

    LaunchedEffect(sections) {
        if (sections.none { it.section == selectedSection }) {
            selectedSection = sections.first().section
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
                title = {
                    Text(
                        text = "${username ?: "-"} · $roleText",
                        color = LightText
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                        Text("Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                sections.forEach { item ->
                    NavigationBarItem(
                        selected = selectedSection == item.section,
                        onClick = { selectedSection = item.section },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedSection) {
                TopSection.HOME -> HomeScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                TopSection.PATIENTS -> PatientsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                TopSection.DOCTORS -> DoctorsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                TopSection.APPOINTMENTS -> AppointmentsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
                TopSection.DETAILS -> DetailsScreen(modifier = Modifier.fillMaxSize(), roles = roles)
            }
        }
    }
}
