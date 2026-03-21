package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.viewmodel.DetailsViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = viewModel()
) {
    val details by viewModel.details.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    val message by viewModel.message.observeAsState(null)

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> DetailsContent(
                details = details,
                message = message,
                onCreate = viewModel::createDetail,
                onUpdate = viewModel::updateDetail,
                onDelete = viewModel::deleteDetail,
                onFilterByAppointment = viewModel::loadByAppointment,
                onReload = viewModel::loadDetails
            )
        }
    }
}

@Composable
private fun DetailsContent(
    details: List<DetailsDTO>,
    message: String?,
    onCreate: (DetailsDTO) -> Unit,
    onUpdate: (Int, DetailsDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onFilterByAppointment: (Int) -> Unit,
    onReload: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailsCrudPanel(
            onCreate = onCreate,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onFilterByAppointment = onFilterByAppointment,
            onReload = onReload,
            message = message
        )
        Spacer(modifier = Modifier.height(12.dp))
        DetailsList(details)
    }
}

@Composable
private fun DetailsCrudPanel(
    onCreate: (DetailsDTO) -> Unit,
    onUpdate: (Int, DetailsDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onFilterByAppointment: (Int) -> Unit,
    onReload: () -> Unit,
    message: String?
) {
    var id by remember { mutableStateOf("") }
    var appointmentId by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var followup by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Panel Detalles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (message != null) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
        if (localError != null) {
            Text(text = localError ?: "", color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id (actualizar/borrar)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = appointmentId, onValueChange = { appointmentId = it }, label = { Text("Id Cita") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it }, label = { Text("Diagnóstico") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = treatment, onValueChange = { treatment = it }, label = { Text("Tratamiento") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = prescription, onValueChange = { prescription = it }, label = { Text("Prescripción") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = followup, onValueChange = { followup = it }, label = { Text("Seguimiento") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                localError = null
                val appId = appointmentId.toIntOrNull()
                if (appId == null) {
                    localError = "Id de cita inválido"
                    return@ElevatedButton
                }
                val dto = DetailsDTO(
                    id = null,
                    appointment = AppointmentDTO(id = appId, dateTime = null, patient = null, doctor = null, reason = null, status = null),
                    diagnosis = diagnosis.ifBlank { null },
                    prescription = prescription.ifBlank { null },
                    notes = notes.ifBlank { null },
                    treatment = treatment.ifBlank { null },
                    followup = followup.ifBlank { null }
                )
                onCreate(dto)
            }) { Text("Crear") }

            ElevatedButton(onClick = {
                localError = null
                val targetId = id.toIntOrNull()
                val appId = appointmentId.toIntOrNull()
                if (targetId == null || appId == null) {
                    localError = "Ids inválidos"
                    return@ElevatedButton
                }
                val dto = DetailsDTO(
                    id = targetId,
                    appointment = AppointmentDTO(id = appId, dateTime = null, patient = null, doctor = null, reason = null, status = null),
                    diagnosis = diagnosis.ifBlank { null },
                    prescription = prescription.ifBlank { null },
                    notes = notes.ifBlank { null },
                    treatment = treatment.ifBlank { null },
                    followup = followup.ifBlank { null }
                )
                onUpdate(targetId, dto)
            }) { Text("Actualizar") }

            ElevatedButton(onClick = {
                localError = null
                val targetId = id.toIntOrNull()
                if (targetId == null) {
                    localError = "Id inválido"
                    return@ElevatedButton
                }
                onDelete(targetId)
            }) { Text("Eliminar") }

            ElevatedButton(onClick = {
                localError = null
                val appId = appointmentId.toIntOrNull()
                if (appId == null) {
                    localError = "Id de cita inválido"
                    return@ElevatedButton
                }
                onFilterByAppointment(appId)
            }) { Text("Filtrar por cita") }

            ElevatedButton(onClick = {
                localError = null
                onReload()
            }) { Text("Recargar") }
        }
    }
}

@Composable
fun DetailsList(details: List<DetailsDTO>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(details) { detail ->
            DetailItem(detail)
        }
    }
}

@Composable
fun DetailItem(detail: DetailsDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = detail.diagnosis ?: "Sin diagnostico",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail.treatment ?: "Sin tratamiento",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = detail.prescription ?: "Sin prescripcion",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = detail.notes ?: "Sin notas",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Cita: ${detail.appointment?.dateTime ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailItemPreview() {
    Ocularis_MobileTheme {
        DetailItem(
            detail = DetailsDTO(
                id = 1,
                appointment = AppointmentDTO(
                    id = 1,
                    dateTime = LocalDate.now() as String?,
                    patient = PatientDTO(
                        id = 1,
                        dni = "123",
                        firstName = "Jane",
                        secondName = null,
                        lastName = "Doe",
                        secondLastName = null,
                        email = null,
                        phone = null,
                        birthDate = null,
                        address = null
                    ),
                    doctor = DoctorDTO(
                        id = 1,
                        firstName = "Doc",
                        secondName = null,
                        lastName = "Brown",
                        secondLastName = null,
                        dni = null,
                        email = null,
                        phone = null,
                        licenseNumber = null,
                        specialty = "Oftalmologia"
                    ),
                    reason = "Chequeo",
                    status = null
                ),
                diagnosis = "Miopia leve",
                prescription = "Gafas 1.25",
                notes = "Revisar en 6 meses",
                treatment = "Lentes diarios",
                followup = "2024-12-01"
            )
        )
    }
}

