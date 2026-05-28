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
import fp.practices.ocularis_mobile.data.model.DashboardStats
import fp.practices.ocularis_mobile.data.model.DashboardVisualContent
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantGreen
import fp.practices.ocularis_mobile.ui.theme.VibrantOrange
import fp.practices.ocularis_mobile.viewmodel.DashboardViewModel
import fp.practices.ocularis_mobile.viewmodel.DashboardVisualViewModel

@Composable
fun AdminScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
    viewModel: DashboardViewModel = viewModel()
) {
    val stats by viewModel.stats.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val visualsViewModel: DashboardVisualViewModel = viewModel()
    val visualContent by visualsViewModel.visualContent.observeAsState()
    val visualsLoading by visualsViewModel.isLoading.observeAsState(false)
    val visualsError by visualsViewModel.error.observeAsState(null)

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
            error != null -> ErrorPanel(message = error ?: "Error", onRetry = viewModel::loadStats)
            else -> DashboardContent(
                stats = stats,
                roles = roles,
                visualContent = visualContent,
                visualsLoading = visualsLoading,
                visualsError = visualsError,
                onReload = viewModel::loadStats,
                onReloadVisuals = visualsViewModel::loadVisualContent
            )
        }
    }
}

@Composable
private fun DashboardContent(
    stats: DashboardStats?,
    roles: Set<String>,
    visualContent: DashboardVisualContent?,
    visualsLoading: Boolean,
    visualsError: String?,
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

        item { DashboardStatsSummary(stats = stats, roles = roles) }

        if (visualsLoading && visualContent == null) {
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

        val visualsResolved = visualContent ?: DashboardVisualContent.default()

        item { ContactSection(visualsResolved.contact) }

        when {
            isAdmin -> {
                item { FinancingSection(visualsResolved.financingPlans) }
                item { OperationsSection(visualsResolved.operations) }
            }
            isDoctor -> {
                item { OperationsSection(visualsResolved.operations) }
            }
            isPatient -> {
                item { PatientTipsSection(visualsResolved.tips) }
            }
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
private fun PatientTipsSection(tips: List<String>) {
    Card(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Recomendaciones rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            tips.forEach { tip ->
                Text("• $tip", color = MediumText)
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
private fun DashboardStatsSummary(stats: DashboardStats?, roles: Set<String>) {
    val isAdmin = roles.contains("ADMIN")
    val isDoctor = roles.contains("DOCTOR")
    val isPatient = roles.contains("PATIENT")

    when {
        isAdmin -> AdminSummary(stats)
        isDoctor -> DoctorSummary(stats)
        isPatient -> PatientSummary(stats)
        else -> PatientSummary(stats)
    }
}

@Composable
private fun AdminSummary(stats: DashboardStats?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(title = "Pacientes", value = stats?.patients ?: 0, color = VibrantGreen)
        StatCard(title = "Doctores", value = stats?.doctors ?: 0, color = VibrantOrange)
        StatCard(title = "Citas", value = stats?.appointments ?: 0, color = VibrantBlue)
    }
}

@Composable
private fun DoctorSummary(stats: DashboardStats?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(title = "Pacientes", value = stats?.patients ?: 0, color = VibrantGreen)
        StatCard(title = "Citas", value = stats?.appointments ?: 0, color = VibrantBlue)
    }
}

@Composable
private fun PatientSummary(stats: DashboardStats?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCard(title = "Mis citas", value = stats?.appointments ?: 0, color = VibrantBlue)
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

