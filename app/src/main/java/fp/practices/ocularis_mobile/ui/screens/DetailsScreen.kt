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
import androidx.compose.material.icons.filled.Search
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
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.viewmodel.DetailsViewModel
import java.time.LocalDate

@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
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
                roles = roles,
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
    roles: Set<String>,
    message: String?,
    onCreate: (DetailsDTO) -> Unit,
    onUpdate: (Int, DetailsDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onFilterByAppointment: (Int) -> Unit,
    onReload: () -> Unit
) {
    val canRead = RoleAccess.canReadDetails(roles)
    val canManage = RoleAccess.canManageDetails(roles)
    var currentAction by remember { mutableStateOf(DetailAction.LIST) }

    if (!canRead) {
        PermissionRequiredPanel()
        return
    }

    if (!canManage && currentAction != DetailAction.LIST && currentAction != DetailAction.RELOAD) {
        currentAction = DetailAction.LIST
    }

    Row(modifier = Modifier.fillMaxSize()) {
        DetailSideNav(
            currentAction = currentAction,
            canManage = canManage,
            onActionSelected = { action ->
                if (!canManage && action != DetailAction.LIST && action != DetailAction.RELOAD) {
                    return@DetailSideNav
                }
                if (action == DetailAction.RELOAD) {
                    onReload()
                    currentAction = DetailAction.LIST
                } else {
                    currentAction = action
                }
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            DetailsCrudPanel(
                currentAction = currentAction,
                details = details,
                onCreate = onCreate,
                onUpdate = onUpdate,
                onDelete = onDelete,
                onFilterByAppointment = onFilterByAppointment,
                onReload = onReload,
                message = message,
                onActionDone = { currentAction = DetailAction.LIST }
            )
        }
    }
}

@Composable
private fun DetailSideNav(
    currentAction: DetailAction,
    canManage: Boolean,
    onActionSelected: (DetailAction) -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            selected = currentAction == DetailAction.LIST,
            onClick = { onActionSelected(DetailAction.LIST) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lista") },
            label = { Text("Lista") }
        )
        if (canManage) {
            NavigationRailItem(
                selected = currentAction == DetailAction.CREATE,
                onClick = { onActionSelected(DetailAction.CREATE) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                label = { Text("Crear") }
            )
            NavigationRailItem(
                selected = currentAction == DetailAction.UPDATE,
                onClick = { onActionSelected(DetailAction.UPDATE) },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Actualizar") },
                label = { Text("Actualizar") }
            )
            NavigationRailItem(
                selected = currentAction == DetailAction.DELETE,
                onClick = { onActionSelected(DetailAction.DELETE) },
                icon = { Icon(Icons.Default.Delete, contentDescription = "Eliminar") },
                label = { Text("Eliminar") }
            )
            NavigationRailItem(
                selected = currentAction == DetailAction.FILTER_APPOINTMENT,
                onClick = { onActionSelected(DetailAction.FILTER_APPOINTMENT) },
                icon = { Icon(Icons.Default.Search, contentDescription = "Filtrar") },
                label = { Text("Por cita") }
            )
        }
        NavigationRailItem(
            selected = currentAction == DetailAction.RELOAD,
            onClick = { onActionSelected(DetailAction.RELOAD) },
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
private fun DetailsCrudPanel(
    currentAction: DetailAction,
    details: List<DetailsDTO>,
    onCreate: (DetailsDTO) -> Unit,
    onUpdate: (Int, DetailsDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onFilterByAppointment: (Int) -> Unit,
    onReload: () -> Unit,
    message: String?,
    onActionDone: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var appointmentId by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var followup by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Detalles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        message?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
        localError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        when (currentAction) {
            DetailAction.LIST -> {
                Spacer(modifier = Modifier.height(12.dp))
                DetailsList(details)
            }
            DetailAction.CREATE, DetailAction.UPDATE -> {
                DetailFormFields(
                    id = id,
                    onIdChange = { id = it },
                    appointmentId = appointmentId,
                    onAppointmentIdChange = { appointmentId = it },
                    diagnosis = diagnosis,
                    onDiagnosisChange = { diagnosis = it },
                    treatment = treatment,
                    onTreatmentChange = { treatment = it },
                    prescription = prescription,
                    onPrescriptionChange = { prescription = it },
                    notes = notes,
                    onNotesChange = { notes = it },
                    followup = followup,
                    onFollowupChange = { followup = it },
                    showId = currentAction == DetailAction.UPDATE
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    val appId = appointmentId.toIntOrNull()
                    if (appId == null) {
                        localError = "Id de cita inválido"
                        return@ElevatedButton
                    }
                    val dto = DetailsDTO(
                        id = id.toIntOrNull(),
                        appointment = AppointmentDTO(id = appId, dateTime = null, patient = null, doctor = null, reason = null, status = null),
                        diagnosis = diagnosis.ifBlank { null },
                        prescription = prescription.ifBlank { null },
                        notes = notes.ifBlank { null },
                        treatment = treatment.ifBlank { null },
                        followup = followup.ifBlank { null }
                    )
                    if (currentAction == DetailAction.UPDATE) {
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
                }) { Text(if (currentAction == DetailAction.UPDATE) "Actualizar" else "Crear") }
            }
            DetailAction.DELETE -> {
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
            DetailAction.FILTER_APPOINTMENT -> {
                OutlinedTextField(
                    value = appointmentId,
                    onValueChange = { appointmentId = it },
                    label = { Text("Id Cita") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    val appId = appointmentId.toIntOrNull()
                    if (appId == null) {
                        localError = "Id de cita inválido"
                        return@ElevatedButton
                    }
                    onFilterByAppointment(appId)
                }) { Text("Filtrar") }
            }
            DetailAction.RELOAD -> {
                onReload()
                onActionDone()
            }
        }
    }
}

@Composable
private fun DetailFormFields(
    id: String,
    onIdChange: (String) -> Unit,
    appointmentId: String,
    onAppointmentIdChange: (String) -> Unit,
    diagnosis: String,
    onDiagnosisChange: (String) -> Unit,
    treatment: String,
    onTreatmentChange: (String) -> Unit,
    prescription: String,
    onPrescriptionChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    followup: String,
    onFollowupChange: (String) -> Unit,
    showId: Boolean
) {
    if (showId) {
        OutlinedTextField(value = id, onValueChange = onIdChange, label = { Text("Id") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
    OutlinedTextField(value = appointmentId, onValueChange = onAppointmentIdChange, label = { Text("Id Cita") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = diagnosis, onValueChange = onDiagnosisChange, label = { Text("Diagnóstico") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = treatment, onValueChange = onTreatmentChange, label = { Text("Tratamiento") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = prescription, onValueChange = onPrescriptionChange, label = { Text("Prescripción") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = notes, onValueChange = onNotesChange, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = followup, onValueChange = onFollowupChange, label = { Text("Seguimiento") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
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

private enum class DetailAction { LIST, CREATE, UPDATE, DELETE, FILTER_APPOINTMENT, RELOAD }

@Preview(showBackground = true)
@Composable
fun DetailItemPreview() {
    Ocularis_MobileTheme {
        DetailItem(
            detail = DetailsDTO(
                id = 1,
                appointment = AppointmentDTO(
                    id = 1,
                    dateTime = LocalDate.now().toString(),
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

