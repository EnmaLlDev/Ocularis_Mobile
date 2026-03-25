package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.viewmodel.AppointmentsViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun AppointmentsScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
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
                roles = roles,
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
    roles: Set<String>,
    message: String?,
    onCreate: (AppointmentDTO) -> Unit,
    onUpdate: (Int, AppointmentDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onReload: () -> Unit
) {
    val canRead = RoleAccess.canReadAppointments(roles)
    val canManage = RoleAccess.canManageAppointments(roles)
    var currentAction by remember { mutableStateOf(AppointmentAction.LIST) }

    if (!canRead) {
        PermissionRequiredPanel()
        return
    }

    if (!canManage && currentAction != AppointmentAction.LIST && currentAction != AppointmentAction.RELOAD) {
        currentAction = AppointmentAction.LIST
    }

    Row(modifier = Modifier.fillMaxSize()) {
        AppointmentSideNav(
            currentAction = currentAction,
            canManage = canManage,
            onActionSelected = { action ->
                if (!canManage && action != AppointmentAction.LIST && action != AppointmentAction.RELOAD) {
                    return@AppointmentSideNav
                }
                if (action == AppointmentAction.RELOAD) {
                    onReload()
                    currentAction = AppointmentAction.LIST
                } else {
                    currentAction = action
                }
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            AppointmentCrudPanel(
                currentAction = currentAction,
                appointments = appointments,
                onCreate = onCreate,
                onUpdate = onUpdate,
                onDelete = onDelete,
                onReload = onReload,
                message = message,
                onActionDone = { currentAction = AppointmentAction.LIST }
            )
        }
    }
}

@Composable
private fun AppointmentSideNav(
    currentAction: AppointmentAction,
    canManage: Boolean,
    onActionSelected: (AppointmentAction) -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            selected = currentAction == AppointmentAction.LIST,
            onClick = { onActionSelected(AppointmentAction.LIST) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lista") },
            label = { Text("Lista") }
        )
        if (canManage) {
            NavigationRailItem(
                selected = currentAction == AppointmentAction.CREATE,
                onClick = { onActionSelected(AppointmentAction.CREATE) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                label = { Text("Crear") }
            )
            NavigationRailItem(
                selected = currentAction == AppointmentAction.UPDATE,
                onClick = { onActionSelected(AppointmentAction.UPDATE) },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Actualizar") },
                label = { Text("Actualizar") }
            )
            NavigationRailItem(
                selected = currentAction == AppointmentAction.DELETE,
                onClick = { onActionSelected(AppointmentAction.DELETE) },
                icon = { Icon(Icons.Default.Delete, contentDescription = "Eliminar") },
                label = { Text("Eliminar") }
            )
        }
        NavigationRailItem(
            selected = currentAction == AppointmentAction.RELOAD,
            onClick = { onActionSelected(AppointmentAction.RELOAD) },
            icon = { Icon(Icons.Default.Refresh, contentDescription = "Recargar") },
            label = { Text("Recargar") }
        )
    }
}

@Composable
private fun PermissionRequiredPanel() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Permiso requerido para acceder a esta vista")
    }
}

@Composable
private fun AppointmentCrudPanel(
    currentAction: AppointmentAction,
    appointments: List<AppointmentDTO>,
    onCreate: (AppointmentDTO) -> Unit,
    onUpdate: (Int, AppointmentDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onReload: () -> Unit,
    message: String?,
    onActionDone: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var patientId by remember { mutableStateOf("") }
    var doctorId by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Citas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        message?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
        localError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        when (currentAction) {
            AppointmentAction.LIST -> {
                Spacer(modifier = Modifier.height(12.dp))
                AppointmentsList(appointments)
            }
            AppointmentAction.CREATE, AppointmentAction.UPDATE -> {
                AppointmentFormFields(
                    id = id,
                    onIdChange = { id = it },
                    patientId = patientId,
                    onPatientIdChange = { patientId = it },
                    doctorId = doctorId,
                    onDoctorIdChange = { doctorId = it },
                    dateTime = dateTime,
                    onDateTimeChange = { dateTime = it },
                    reason = reason,
                    onReasonChange = { reason = it },
                    status = status,
                    onStatusChange = { status = it },
                    showId = currentAction == AppointmentAction.UPDATE
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                        id = id.toIntOrNull(),
                        dateTime = dateTime.ifBlank { null },
                        patient = PatientDTO(id = patient, dni = null, firstName = "", secondName = null, lastName = "", secondLastName = null, email = null, phone = null, birthDate = null, address = null),
                        doctor = DoctorDTO(id = doctor, firstName = "", secondName = null, lastName = "", secondLastName = null, dni = null, email = null, phone = null, licenseNumber = null, specialty = null),
                        reason = reason.ifBlank { null },
                        status = statusEnum
                    )
                    if (currentAction == AppointmentAction.UPDATE) {
                        val targetId = dto.id
                        if (targetId == null) {
                            localError = "Id requerido para actualizar"
                            return@ElevatedButton
                        }
                        onUpdate(targetId, dto)
                    } else {
                        onCreate(dto.copy(id = null))
                    }
                    onActionDone()
                }) { Text(if (currentAction == AppointmentAction.UPDATE) "Actualizar" else "Crear") }
            }
            AppointmentAction.DELETE -> {
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("Id a eliminar") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    val targetId = id.toIntOrNull()
                    if (targetId == null) {
                        localError = "Id inválido"
                        return@ElevatedButton
                    }
                    onDelete(targetId)
                    onActionDone()
                }) { Text("Eliminar") }
            }
            AppointmentAction.RELOAD -> {
                onReload()
                onActionDone()
            }
        }
    }
}

@Composable
private fun AppointmentFormFields(
    id: String,
    onIdChange: (String) -> Unit,
    patientId: String,
    onPatientIdChange: (String) -> Unit,
    doctorId: String,
    onDoctorIdChange: (String) -> Unit,
    dateTime: String,
    onDateTimeChange: (String) -> Unit,
    reason: String,
    onReasonChange: (String) -> Unit,
    status: String,
    onStatusChange: (String) -> Unit,
    showId: Boolean
) {
    if (showId) {
        OutlinedTextField(value = id, onValueChange = onIdChange, label = { Text("Id") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
    OutlinedTextField(value = patientId, onValueChange = onPatientIdChange, label = { Text("Id Paciente") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = doctorId, onValueChange = onDoctorIdChange, label = { Text("Id Doctor") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = dateTime, onValueChange = onDateTimeChange, label = { Text("Fecha/Hora (ISO)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = reason, onValueChange = onReasonChange, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = status, onValueChange = onStatusChange, label = { Text("Estado (SCHEDULED/...)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
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

private enum class AppointmentAction { LIST, CREATE, UPDATE, DELETE, RELOAD }

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
