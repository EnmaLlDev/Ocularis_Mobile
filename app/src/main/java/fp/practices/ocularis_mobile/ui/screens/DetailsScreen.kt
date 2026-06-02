package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.DarkSurfaceVariant
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.viewmodel.DetailsViewModel

/**
 * Pantalla de gestión de detalles clínicos con operaciones CRUD.
 * Para DOCTOR muestra una vista centrada en sus citas con detalles expandibles.
 */
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
    val myAppointments by viewModel.myAppointments.observeAsState(emptyList())
    val appointmentDetailsMap by viewModel.appointmentDetailsMap.observeAsState(emptyMap())
    val isPatient = roles.contains("PATIENT")
    val isDoctor = roles.contains("DOCTOR")

    LaunchedEffect(Unit) {
        when {
            isDoctor -> viewModel.loadMyAppointmentsWithDetails()
            else -> viewModel.loadDetails(isPatient)
        }
    }

    val onReload: () -> Unit = {
        when {
            isDoctor -> viewModel.loadMyAppointmentsWithDetails()
            else -> viewModel.loadDetails(isPatient)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VibrantBlue)
            error != null -> {
                val is403 = error?.contains("403") == true
                val errorMessage = if (isPatient && is403) {
                    "No tienes permiso para ver todos los detalles."
                } else {
                    "Error: $error"
                }
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
            }
            else -> if (isDoctor) {
                DoctorDetailsView(
                    appointments = myAppointments,
                    appointmentDetailsMap = appointmentDetailsMap,
                    message = message,
                    onCreateDetail = { dto ->
                        viewModel.createDetail(dto)
                        dto.appointmentId?.let { viewModel.reloadDetailsForAppointment(it) }
                    },
                    onUpdateDetail = { id, dto ->
                        viewModel.updateDetail(id, dto)
                        dto.appointmentId?.let { viewModel.reloadDetailsForAppointment(it) }
                    },
                    onDeleteDetail = { id, appointmentId ->
                        viewModel.deleteDetail(id)
                        viewModel.reloadDetailsForAppointment(appointmentId)
                    },
                    onReload = onReload
                )
            } else {
                DetailsContent(
                    details = details,
                    roles = roles,
                    message = message,
                    onCreate = viewModel::createDetail,
                    onUpdate = viewModel::updateDetail,
                    onDelete = viewModel::deleteDetail,
                    onFilterByAppointment = viewModel::loadByAppointment,
                    onReload = onReload
                )
            }
        }
    }
}

// ─── Vista centrada en citas para el DOCTOR autenticado ───────────────────────

@Composable
private fun DoctorDetailsView(
    appointments: List<AppointmentDTO>,
    appointmentDetailsMap: Map<Int, List<DetailsDTO>>,
    message: String?,
    onCreateDetail: (DetailsDTO) -> Unit,
    onUpdateDetail: (Int, DetailsDTO) -> Unit,
    onDeleteDetail: (Int, Int) -> Unit,
    onReload: () -> Unit
) {
    var expandedAppointmentId by remember { mutableStateOf<Int?>(null) }
    // appointmentId pre-rellenado cuando el doctor pulsa "Agregar detalle"
    var addingForAppointmentId by remember { mutableStateOf<Int?>(null) }
    // detalle en edición
    var editingDetail by remember { mutableStateOf<DetailsDTO?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mis citas", style = MaterialTheme.typography.titleMedium, color = LightText, fontWeight = FontWeight.Bold)
            IconButton(onClick = onReload) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = VibrantBlue)
            }
        }

        message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Formulario de nuevo detalle o edición
        if (addingForAppointmentId != null || editingDetail != null) {
            val targetAppointmentId = editingDetail?.appointmentId ?: addingForAppointmentId!!
            DetailFormCard(
                appointmentId = targetAppointmentId,
                existingDetail = editingDetail,
                onConfirm = { dto ->
                    if (editingDetail != null) {
                        editingDetail!!.id?.let { onUpdateDetail(it, dto) }
                    } else {
                        onCreateDetail(dto)
                    }
                    addingForAppointmentId = null
                    editingDetail = null
                },
                onCancel = {
                    addingForAppointmentId = null
                    editingDetail = null
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (appointments.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Text("No tienes citas registradas", color = MediumText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(appointments) { appt ->
                    val apptId = appt.id ?: return@items
                    val isExpanded = expandedAppointmentId == apptId
                    val details = appointmentDetailsMap[apptId] ?: emptyList()

                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    appt.patient?.let { patient ->
                                        Text(
                                            "${patient.firstName} ${patient.lastName}",
                                            color = LightText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(appt.dateTime ?: "Sin fecha", color = MediumText, style = MaterialTheme.typography.bodySmall)
                                    appt.reason?.let { Text("Motivo: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                                    appt.status?.let { Text("Estado: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                                }
                                IconButton(onClick = {
                                    expandedAppointmentId = if (isExpanded) null else apptId
                                }) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = null,
                                        tint = VibrantBlue
                                    )
                                }
                            }

                            if (isExpanded) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                if (details.isEmpty()) {
                                    Text("Sin detalles clínicos", color = MediumText, style = MaterialTheme.typography.bodySmall)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        details.forEach { detail ->
                                            DetailCardRow(
                                                detail = detail,
                                                onEdit = { editingDetail = it },
                                                onDelete = { onDeleteDetail(it, apptId) }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                ElevatedButton(
                                    onClick = { addingForAppointmentId = apptId },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text(" Agregar detalle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCardRow(
    detail: DetailsDTO,
    onEdit: (DetailsDTO) -> Unit,
    onDelete: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    detail.diagnosis?.let { Text("Diagnóstico: $it", color = LightText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold) }
                    detail.treatment?.let { Text("Tratamiento: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                    detail.prescription?.let { Text("Prescripción: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                    detail.notes?.let { Text("Notas: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                    detail.followUp?.let { Text("Seguimiento: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                }
                IconButton(onClick = { onEdit(detail) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = VibrantBlue)
                }
                detail.id?.let { id ->
                    IconButton(onClick = { onDelete(id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailFormCard(
    appointmentId: Int,
    existingDetail: DetailsDTO?,
    onConfirm: (DetailsDTO) -> Unit,
    onCancel: () -> Unit
) {
    var diagnosis by remember(existingDetail) { mutableStateOf(existingDetail?.diagnosis ?: "") }
    var treatment by remember(existingDetail) { mutableStateOf(existingDetail?.treatment ?: "") }
    var prescription by remember(existingDetail) { mutableStateOf(existingDetail?.prescription ?: "") }
    var notes by remember(existingDetail) { mutableStateOf(existingDetail?.notes ?: "") }
    var followUp by remember(existingDetail) { mutableStateOf(existingDetail?.followUp ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
            Text(
                if (existingDetail != null) "Editar detalle — Cita #$appointmentId" else "Nuevo detalle — Cita #$appointmentId",
                color = LightText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "Diagnóstico" to diagnosis,
                "Tratamiento" to treatment,
                "Prescripción" to prescription,
                "Notas" to notes,
                "Seguimiento (yyyy-MM-dd)" to followUp
            ).forEachIndexed { index, (label, value) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = { v ->
                        when (index) {
                            0 -> diagnosis = v
                            1 -> treatment = v
                            2 -> prescription = v
                            3 -> notes = v
                            4 -> followUp = v
                        }
                    },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(10.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantBlue,
                        unfocusedBorderColor = MediumText,
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(
                    onClick = {
                        onConfirm(
                            DetailsDTO(
                                id = existingDetail?.id,
                                appointmentId = appointmentId,
                                diagnosis = diagnosis.ifBlank { null },
                                treatment = treatment.ifBlank { null },
                                prescription = prescription.ifBlank { null },
                                notes = notes.ifBlank { null },
                                followUp = followUp.ifBlank { null }
                            )
                        )
                    }
                ) { Text(if (existingDetail != null) "Actualizar" else "Crear") }
                ElevatedButton(onClick = onCancel) { Text("Cancelar") }
            }
        }
    }
}

// ─── Vista plana para ADMIN / PATIENT ─────────────────────────────────────────

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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ActionChips(
            currentAction = currentAction,
            canManage = canManage,
            onSelect = { action ->
                if (!canManage && action != DetailAction.LIST && action != DetailAction.RELOAD) return@ActionChips
                if (action == DetailAction.RELOAD) {
                    onReload()
                    currentAction = DetailAction.LIST
                } else {
                    currentAction = action
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

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

@Composable
private fun ActionChips(
    currentAction: DetailAction,
    canManage: Boolean,
    onSelect: (DetailAction) -> Unit
) {
    val items = buildList {
        add(DetailAction.LIST)
        if (canManage) {
            add(DetailAction.CREATE)
            add(DetailAction.UPDATE)
            add(DetailAction.DELETE)
            add(DetailAction.FILTER_APPOINTMENT)
        }
        add(DetailAction.RELOAD)
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { action ->
            FilterChip(
                selected = currentAction == action,
                onClick = { onSelect(action) },
                label = { Text(action.label) }
            )
        }
    }
}

@Composable
private fun PermissionRequiredPanel() {
    Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
        Text("Permiso requerido para acceder a esta vista", color = LightText)
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
    var followUp by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    when (currentAction) {
        DetailAction.LIST -> {
            Spacer(modifier = Modifier.height(12.dp))
            DetailsList(details)
        }
        DetailAction.CREATE, DetailAction.UPDATE -> {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text("Detalles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightText)
                message?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
                localError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.height(8.dp))
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
                    followUp = followUp,
                    onFollowupChange = { followUp = it },
                    showId = currentAction == DetailAction.UPDATE
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(
                    onClick = {
                        localError = null
                        val appId = appointmentId.toIntOrNull()
                        if (appId == null) {
                            localError = "Id de cita inválido"
                            return@ElevatedButton
                        }
                        val dto = DetailsDTO(
                            id = id.toIntOrNull(),
                            appointmentId = appId,
                            diagnosis = diagnosis.ifBlank { null },
                            prescription = prescription.ifBlank { null },
                            notes = notes.ifBlank { null },
                            treatment = treatment.ifBlank { null },
                            followUp = followUp.ifBlank { null }
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
        }
        DetailAction.DELETE -> {
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("Id a eliminar") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibrantBlue,
                    unfocusedBorderColor = MediumText,
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            ElevatedButton(
                onClick = {
                    localError = null
                    val targetId = id.toIntOrNull()
                    if (targetId == null) {
                        localError = "Id inválido"
                        return@ElevatedButton
                    }
                    onDelete(targetId)
                    onActionDone()
                },
                colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                    containerColor = PrimaryBlue,
                    contentColor = LightText
                )
            ) { Text("Eliminar") }
        }
        DetailAction.FILTER_APPOINTMENT -> {
            OutlinedTextField(
                value = appointmentId,
                onValueChange = { appointmentId = it },
                label = { Text("Id Cita") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibrantBlue,
                    unfocusedBorderColor = MediumText,
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            ElevatedButton(
                onClick = {
                    localError = null
                    val appId = appointmentId.toIntOrNull()
                    if (appId == null) {
                        localError = "Id de cita inválido"
                        return@ElevatedButton
                    }
                    onFilterByAppointment(appId)
                },
                colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                    containerColor = PrimaryBlue,
                    contentColor = LightText
                )
            ) { Text("Filtrar") }
        }
        DetailAction.RELOAD -> {
            onReload()
            onActionDone()
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
    followUp: String,
    onFollowupChange: (String) -> Unit,
    showId: Boolean
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VibrantBlue,
        unfocusedBorderColor = MediumText,
        focusedTextColor = LightText,
        unfocusedTextColor = LightText
    )
    val fieldMod = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp))
    if (showId) OutlinedTextField(value = id, onValueChange = onIdChange, label = { Text("Id") }, modifier = fieldMod, colors = fieldColors)
    OutlinedTextField(value = appointmentId, onValueChange = onAppointmentIdChange, label = { Text("Id Cita") }, modifier = fieldMod, colors = fieldColors)
    OutlinedTextField(value = diagnosis, onValueChange = onDiagnosisChange, label = { Text("Diagnóstico") }, modifier = fieldMod, colors = fieldColors)
    OutlinedTextField(value = treatment, onValueChange = onTreatmentChange, label = { Text("Tratamiento") }, modifier = fieldMod, colors = fieldColors)
    OutlinedTextField(value = prescription, onValueChange = onPrescriptionChange, label = { Text("Prescripción") }, modifier = fieldMod, colors = fieldColors)
    OutlinedTextField(value = notes, onValueChange = onNotesChange, label = { Text("Notas") }, modifier = fieldMod, colors = fieldColors)
    OutlinedTextField(value = followUp, onValueChange = onFollowupChange, label = { Text("Seguimiento (yyyy-MM-dd)") }, modifier = fieldMod, colors = fieldColors)
}

@Composable
fun DetailsList(details: List<DetailsDTO>) {
    if (details.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            Text(text = "No hay detalles registrados", color = MediumText)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(details) { detail -> DetailItem(detail) }
    }
}

@Composable
fun DetailItem(detail: DetailsDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(detail.diagnosis ?: "Sin diagnostico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightText)
            Text(detail.treatment ?: "Sin tratamiento", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp), color = MediumText)
            Text(detail.prescription ?: "Sin prescripcion", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp), color = MediumText)
            Text(detail.notes ?: "Sin notas", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MediumText)
            Text("Cita: ${detail.appointmentId ?: "N/D"}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MediumText)
        }
    }
}

private enum class DetailAction(val label: String) {
    LIST("Lista"), CREATE("Crear"), UPDATE("Actualizar"), DELETE("Eliminar"), FILTER_APPOINTMENT("Filtrar"), RELOAD("Recargar")
}

@Preview(showBackground = true)
@Composable
fun DetailItemPreview() {
    Ocularis_MobileTheme {
        DetailItem(
            detail = DetailsDTO(
                id = 1, appointmentId = 1, diagnosis = "Miopia leve",
                prescription = "Gafas 1.25", notes = "Revisar en 6 meses",
                treatment = "Lentes diarios", followUp = "2024-12-01"
            )
        )
    }
}
