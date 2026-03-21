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
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.model.StateAppoinment
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.viewmodel.AppointmentsViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun AppointmentsScreen(
    modifier: Modifier = Modifier,
    viewModel: AppointmentsViewModel = viewModel()
) {
    val appointments by viewModel.appointments.observeAsState(emptyList())
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
            else -> AppointmentsContent(
                appointments = appointments,
                message = message,
                onCreate = viewModel::createAppointment,
                onUpdate = viewModel::updateAppointment,
                onDelete = viewModel::deleteAppointment,
                onReload = viewModel::loadAppointments
            )
        }
    }
}

@Composable
private fun AppointmentsContent(
    appointments: List<AppointmentDTO>,
    message: String?,
    onCreate: (AppointmentDTO) -> Unit,
    onUpdate: (Int, AppointmentDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onReload: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppointmentCrudPanel(
            onCreate = onCreate,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onReload = onReload,
            message = message
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppointmentsList(appointments)
    }
}

@Composable
private fun AppointmentCrudPanel(
    onCreate: (AppointmentDTO) -> Unit,
    onUpdate: (Int, AppointmentDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onReload: () -> Unit,
    message: String?
) {
    var id by remember { mutableStateOf("") }
    var patientId by remember { mutableStateOf("") }
    var doctorId by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Panel Citas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (message != null) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
        if (localError != null) {
            Text(text = localError ?: "", color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id (actualizar/borrar)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = patientId, onValueChange = { patientId = it }, label = { Text("Id Paciente") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = doctorId, onValueChange = { doctorId = it }, label = { Text("Id Doctor") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Fecha/Hora (ISO)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Estado (SCHEDULED/...)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                localError = null
                val patient = patientId.toIntOrNull()
                val doctor = doctorId.toIntOrNull()
                if (patient == null || doctor == null) {
                    localError = "Ids de paciente/doctor inválidos"
                    return@ElevatedButton
                }
                val statusEnum = status.takeIf { it.isNotBlank() }?.let {
                    runCatching { StateAppoinment.valueOf(it.trim().uppercase()) }.getOrNull()
                }
                val dto = AppointmentDTO(
                    id = null,
                    dateTime = dateTime.ifBlank { null },
                    patient = PatientDTO(id = patient, dni = null, firstName = "", secondName = null, lastName = "", secondLastName = null, email = null, phone = null, birthDate = null, address = null),
                    doctor = DoctorDTO(id = doctor, firstName = "", secondName = null, lastName = "", secondLastName = null, dni = null, email = null, phone = null, licenseNumber = null, specialty = null),
                    reason = reason.ifBlank { null },
                    status = statusEnum
                )
                onCreate(dto)
            }) { Text("Crear") }

            ElevatedButton(onClick = {
                localError = null
                val targetId = id.toIntOrNull()
                val patient = patientId.toIntOrNull()
                val doctor = doctorId.toIntOrNull()
                if (targetId == null || patient == null || doctor == null) {
                    localError = "Ids inválidos"
                    return@ElevatedButton
                }
                val statusEnum = status.takeIf { it.isNotBlank() }?.let {
                    runCatching { StateAppoinment.valueOf(it.trim().uppercase()) }.getOrNull()
                }
                val dto = AppointmentDTO(
                    id = targetId,
                    dateTime = dateTime.ifBlank { null },
                    patient = PatientDTO(id = patient, dni = null, firstName = "", secondName = null, lastName = "", secondLastName = null, email = null, phone = null, birthDate = null, address = null),
                    doctor = DoctorDTO(id = doctor, firstName = "", secondName = null, lastName = "", secondLastName = null, dni = null, email = null, phone = null, licenseNumber = null, specialty = null),
                    reason = reason.ifBlank { null },
                    status = statusEnum
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
                onReload()
            }) { Text("Recargar") }
        }
    }
}

@Composable
fun AppointmentsList(appointments: List<AppointmentDTO>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appointments) { appointment ->
            AppointmentItem(appointment)
        }
    }
}

@Composable
fun AppointmentItem(appointment: AppointmentDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = appointment.reason ?: "Sin motivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = appointment.dateTime?.toString() ?: "Fecha no disponible",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Paciente: ${appointment.patient?.firstName ?: "N/D"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Doctor: ${appointment.doctor?.firstName ?: "N/D"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "Estado: ${appointment.status?.name ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentItemPreview() {
    Ocularis_MobileTheme {
        AppointmentItem(
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
                    specialty = "Oftalmología"
                ),
                reason = "Chequeo",
                status = null
            )
        )
    }
}
