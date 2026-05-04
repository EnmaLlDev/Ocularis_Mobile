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
import fp.practices.ocularis_mobile.data.model.DashboardStats
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantGreen
import fp.practices.ocularis_mobile.ui.theme.VibrantOrange
import fp.practices.ocularis_mobile.viewmodel.DashboardViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
    viewModel: DashboardViewModel = viewModel()
) {
    val stats by viewModel.stats.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

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
            else -> DashboardContent(stats = stats, roles = roles, onReload = viewModel::loadStats)
        }
    }
}

@Composable
private fun DashboardContent(stats: DashboardStats?, roles: Set<String>, onReload: () -> Unit) {
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

        item {
            when {
                isAdmin -> AdminSummary(stats)
                isDoctor -> DoctorSummary(stats)
                isPatient -> PatientSummary(stats)
                else -> PatientSummary(stats)
            }
        }

        item { ContactSection() }

        when {
            isAdmin -> {
                item { FinancingSection() }
                item { OperationsSection() }
            }
            isDoctor -> {
                item { OperationsSection() }
            }
            isPatient -> {
                item { PatientTipsSection() }
            }
        }
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

@Composable
private fun ContactSection() {
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
            Text("Clínica Ocularis", style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Tel: +34 900 123 456", style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Email: contacto@ocularis.com", style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Dirección: Av. Salud Visual 123, Madrid", style = MaterialTheme.typography.bodyMedium, color = MediumText)
            Text("Horario: Lun-Vie 9:00-19:00", style = MaterialTheme.typography.bodyMedium, color = MediumText)
        }
    }
}

@Composable
private fun FinancingSection() {
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
            Text("• Plan Esencial: consultas y revisiones básicas en 3 cuotas sin intereses.", color = MediumText)
            Text("• Plan Premium: cirugía + controles postoperatorios hasta 12 meses financiados.", color = MediumText)
            Text("• Plan Familiar: descuentos por grupo y pagos fraccionados hasta 6 cuotas.", color = MediumText)
        }
    }
}

@Composable
private fun OperationsSection() {
    val operations = listOf(
        "Cirugía refractiva (LASIK/PRK)",
        "Cirugía de cataratas",
        "Implante de lentes intraoculares",
        "Cross-linking corneal",
        "Tratamiento de ojo seco avanzado",
        "Control de miopía en niños y adolescentes"
    )
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
private fun PatientTipsSection() {
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
            Text("• Revisa tus citas programadas con frecuencia.", color = MediumText)
            Text("• Si necesitas cambiar una cita, contacta a la clínica.", color = MediumText)
            Text("• Mantén tus datos de contacto actualizados.", color = MediumText)
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

