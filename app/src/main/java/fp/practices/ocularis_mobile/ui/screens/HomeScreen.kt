package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.DashboardStats
import fp.practices.ocularis_mobile.viewmodel.DashboardViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val stats by viewModel.stats.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> ErrorPanel(message = error ?: "Error", onRetry = viewModel::loadStats)
            else -> DashboardContent(stats = stats, onReload = viewModel::loadStats)
        }
    }
}

@Composable
private fun DashboardContent(stats: DashboardStats?, onReload: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Panel clínico",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(onClick = onReload) {
                    Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actualizar")
                }
            }
        }
        item {
            StatsRow(stats = stats)
        }
        item {
            ContactSection()
        }
        item {
            FinancingSection()
        }
        item {
            OperationsSection()
        }
    }
}

@Composable
private fun StatsRow(stats: DashboardStats?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(title = "Pacientes", value = stats?.patients ?: 0)
        StatCard(title = "Doctores", value = stats?.doctors ?: 0)
        StatCard(title = "Citas", value = stats?.appointments ?: 0)
    }
}

@Composable
private fun RowScope.StatCard(title: String, value: Int) {
    Card(
        modifier = Modifier.weight(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ContactSection() {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Contacto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Clínica Ocularis", style = MaterialTheme.typography.bodyMedium)
            Text("Tel: +34 900 123 456", style = MaterialTheme.typography.bodyMedium)
            Text("Email: contacto@ocularis.com", style = MaterialTheme.typography.bodyMedium)
            Text("Dirección: Av. Salud Visual 123, Madrid", style = MaterialTheme.typography.bodyMedium)
            Text("Horario: Lun-Vie 9:00-19:00", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FinancingSection() {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Planes de financiamiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("• Plan Esencial: consultas y revisiones básicas en 3 cuotas sin intereses.")
            Text("• Plan Premium: cirugía + controles postoperatorios hasta 12 meses financiados.")
            Text("• Plan Familiar: descuentos por grupo y pagos fraccionados hasta 6 cuotas.")
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
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Operaciones oculares", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                operations.forEach { op ->
                    Text("• $op", style = MaterialTheme.typography.bodyMedium)
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
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

