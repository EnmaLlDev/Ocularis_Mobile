package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.ContactInfo
import fp.practices.ocularis_mobile.data.model.Dashboard
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantGreen
import fp.practices.ocularis_mobile.ui.theme.VibrantOrange
import fp.practices.ocularis_mobile.viewmodel.DashboardViewModel

/**
 * Pantalla del panel de administración/doctor/paciente con estadísticas y contenido visual.
 * @param roles roles del usuario autenticado
 * @param viewModel ViewModel del dashboard
 */
@Composable
fun AdminScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
    viewModel: DashboardViewModel = viewModel()
) {
    val dashboard by viewModel.dashboard.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val visualsLoading by viewModel.visualsLoading.observeAsState(false)
    val visualsError by viewModel.visualsError.observeAsState(null)
    val patientData by viewModel.patientData.observeAsState(null)

    LaunchedEffect(roles) {
        if (roles.isNotEmpty()) {
            viewModel.loadStatsForRoles(roles)
            viewModel.loadVisualContent()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = VibrantBlue
            )
            error != null -> ErrorPanel(message = error ?: "Error", onRetry = { viewModel.loadStatsForRoles(roles) })
            else -> DashboardContent(
                dashboard = dashboard,
                roles = roles,
                visualsLoading = visualsLoading,
                visualsError = visualsError,
                patientData = patientData,
                onReload = { viewModel.loadStatsForRoles(roles) },
                onReloadVisuals = viewModel::loadVisualContent
            )
        }
    }
}

/**
 * Contenido principal del dashboard adaptado al rol del usuario.
 */
@Composable
private fun DashboardContent(
    dashboard: Dashboard?,
    roles: Set<String>,
    visualsLoading: Boolean,
    visualsError: String?,
    patientData: PatientDTO?,
    onReload: () -> Unit,
    onReloadVisuals: () -> Unit
) {
    val isAdmin = roles.contains("ADMIN")
    val isDoctor = roles.contains("DOCTOR")
    val isPatient = roles.contains("PATIENT")

    val title = when {
        isAdmin -> "Panel clínico"
        isDoctor -> "Agenda profesional"
        isPatient -> "Mi espacio"
        else -> "Inicio"
    }

    val subtitle = when {
        isAdmin -> "Acceso completo a la clínica"
        isDoctor -> "Enfocado en tu actividad médica"
        isPatient -> "Acceso rápido a tu información"
        else -> "Bienvenido"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LightText,
                        fontSize = 20.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumText
                    )
                }
                ElevatedButton(
                    onClick = onReload,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = PrimaryBlue,
                        contentColor = LightText
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actualizar")
                }
            }
        }

        item { DashboardStatsSummary(dashboard = dashboard, roles = roles) }

        if (visualsLoading && dashboard == null) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = VibrantBlue)
                }
            }
        }

        visualsError?.let { errorMessage ->
            item {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                ElevatedButton(
                    onClick = onReloadVisuals,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = PrimaryBlue,
                        contentColor = LightText
                    )
                ) {
                    Text("Reintentar")
                }
            }
        }

        val visualsResolved = dashboard ?: Dashboard.default()

        when {
            isAdmin -> {
                item { ContactSection(visualsResolved.contact) }
                item { FinancingSection(visualsResolved.financingPlans) }
                item { OperationsSection(visualsResolved.operations) }
            }
            isDoctor -> {
                item { ContactSection(visualsResolved.contact) }
                item { OperationsSection(visualsResolved.operations) }
            }
            isPatient -> {
                if (patientData != null) {
                    item { PatientInfoSection(patientData) }
                } else {
                    item {
                        Text(
                            text = "No se pudo cargar tu información personal.",
                            color = MediumText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientInfoSection(patient: PatientDTO) {
    Card(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Mis datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            val fullName = listOfNotNull(patient.firstName, patient.secondName, patient.lastName, patient.secondLastName)
                .joinToString(" ")
            Text(fullName, style = MaterialTheme.typography.bodyMedium, color = MediumText)
            patient.dni?.let { Text("DNI: $it", style = MaterialTheme.typography.bodyMedium, color = MediumText) }
            patient.email?.let { Text("Email: $it", style = MaterialTheme.typography.bodyMedium, color = MediumText) }
            patient.phone?.let { Text("Teléfono: $it", style = MaterialTheme.typography.bodyMedium, color = MediumText) }
            patient.address?.let { Text("Dirección: $it", style = MaterialTheme.typography.bodyMedium, color = MediumText) }
            patient.birthDate?.let { Text("Fecha de nacimiento: $it", style = MaterialTheme.typography.bodyMedium, color = MediumText) }
        }
    }
}

@Composable
private fun ContactSection(contact: ContactInfo) {
    Card(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Contacto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Text(contact.name, style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Tel: ${contact.phone}", style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Email: ${contact.email}", style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Dirección: ${contact.address}", style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Horario: ${contact.hours}", style = MaterialTheme.typography.bodyMedium, color = MediumText)
        }
    }
}

@Composable
private fun FinancingSection(plans: List<String>) {
    Card(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Planes de financiamiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            plans.forEach { plan ->
                Text("• $plan", color = MediumText)
            }
        }
    }
}

@Composable
private fun OperationsSection(operations: List<String>) {
    Card(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Operaciones oculares",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                operations.forEach { op ->
                    Text("• $op", style = MaterialTheme.typography.bodyMedium, color = MediumText)
                }
            }
        }
    }
}


@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Reintentar", color = LightText)
        }
    }
}

@Composable
private fun DashboardStatsSummary(dashboard: Dashboard?, roles: Set<String>) {
    val isAdmin = roles.contains("ADMIN")
    val isDoctor = roles.contains("DOCTOR")
    val isPatient = roles.contains("PATIENT")

    when {
        isAdmin -> AdminSummary(dashboard)
        isDoctor -> DoctorSummary(dashboard)
        isPatient -> PatientSummary(dashboard)
        else -> PatientSummary(dashboard)
    }
}

@Composable
private fun AdminSummary(dashboard: Dashboard?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(title = "Pacientes", value = dashboard?.patients ?: 0, color = VibrantGreen)
        StatCard(title = "Doctores", value = dashboard?.doctors ?: 0, color = VibrantOrange)
        StatCard(title = "Citas", value = dashboard?.appointments ?: 0, color = VibrantBlue)
    }
}

@Composable
private fun DoctorSummary(dashboard: Dashboard?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(title = "Pacientes", value = dashboard?.patients ?: 0, color = VibrantGreen)
        StatCard(title = "Citas", value = dashboard?.appointments ?: 0, color = VibrantBlue)
    }
}

@Composable
private fun PatientSummary(dashboard: Dashboard?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCard(title = "Mis citas", value = dashboard?.appointments ?: 0, color = VibrantBlue)
    }
}

@Composable
private fun RowScope.StatCard(title: String, value: Int, color: Color) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = LightText,
                fontSize = 24.sp
            )
        }
    }
}