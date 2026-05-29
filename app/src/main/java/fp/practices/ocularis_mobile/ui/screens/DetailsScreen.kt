package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.viewmodel.DetailsViewModel
import java.time.LocalDate

/**
 * Pantalla de gestión de detalles clínicos con operaciones CRUD.
 * @param roles roles del usuario
 * @param viewModel ViewModel de detalles
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
    val isPatient = roles.contains("PATIENT")

    // Load details on first composition with patient flag
    LaunchedEffect(Unit) {
        viewModel.loadDetails(isPatient)
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
            error != null -> {
                val is403 = error?.contains("403") == true
                val errorMessage = if (isPatient && is403) {
                    "No tienes permiso para ver todos los detalles.\n(Se necesita endpoint /api/my-details)"
                } else {
                    "Error: $error"
                }
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    if (isPatient && is403) {
                        Text(
                            text = "Contacta al administrador",
                            color = MediumText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            else -> DetailsContent(
                details = details,
                roles = roles,
                message = message,
                onCreate = viewModel::createDetail,
                onUpdate = viewModel::updateDetail,
                onDelete = viewModel::deleteDetail,
                onFilterByAppointment = viewModel::loadByAppointment,
                onReload = { viewModel.loadDetails(isPatient) }
            )
        }
    }
}

/**
 * Contenido principal de detalles clínicos: lista y operaciones según permisos.
 */
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
private fun NavIcon(
    icon: @Composable () -> Unit,
    selected: Boolean,
    label: String,
    onClick: (() -> Unit)? = null
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.08f else 1f, animationSpec = tween(durationMillis = 180))
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
        IconButton(onClick = { onClick?.invoke() }, modifier = Modifier.scale(scale)) {
            Box(modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
            ) {
                icon()
            }
        }
        if (selected) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = VibrantBlue)
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
                },
                colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                    containerColor = PrimaryBlue,
                    contentColor = LightText
                )
            ) { Text(if (currentAction == DetailAction.UPDATE) "Actualizar" else "Crear") }
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
    if (showId) {
        OutlinedTextField(
            value = id,
            onValueChange = onIdChange,
            label = { Text("Id") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VibrantBlue,
                unfocusedBorderColor = MediumText,
                focusedTextColor = LightText,
                unfocusedTextColor = LightText
            )
        )
    }
    OutlinedTextField(
        value = appointmentId,
        onValueChange = onAppointmentIdChange,
        label = { Text("Id Cita") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VibrantBlue,
            unfocusedBorderColor = MediumText,
            focusedTextColor = LightText,
            unfocusedTextColor = LightText
        )
    )
    OutlinedTextField(
        value = diagnosis,
        onValueChange = onDiagnosisChange,
        label = { Text("Diagnóstico") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VibrantBlue,
            unfocusedBorderColor = MediumText,
            focusedTextColor = LightText,
            unfocusedTextColor = LightText
        )
    )
    OutlinedTextField(
        value = treatment,
        onValueChange = onTreatmentChange,
        label = { Text("Tratamiento") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VibrantBlue,
            unfocusedBorderColor = MediumText,
            focusedTextColor = LightText,
            unfocusedTextColor = LightText
        )
    )
    OutlinedTextField(
        value = prescription,
        onValueChange = onPrescriptionChange,
        label = { Text("Prescripción") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VibrantBlue,
            unfocusedBorderColor = MediumText,
            focusedTextColor = LightText,
            unfocusedTextColor = LightText
        )
    )
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Notas") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VibrantBlue,
            unfocusedBorderColor = MediumText,
            focusedTextColor = LightText,
            unfocusedTextColor = LightText
        )
    )
    OutlinedTextField(
        value = followUp,
        onValueChange = onFollowupChange,
        label = { Text("Seguimiento (yyyy-MM-dd)") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VibrantBlue,
            unfocusedBorderColor = MediumText,
            focusedTextColor = LightText,
            unfocusedTextColor = LightText
        )
    )
}

@Composable
fun DetailsList(details: List<DetailsDTO>) {
    LazyColumn(
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = detail.diagnosis ?: "Sin diagnostico",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Text(
                text = detail.treatment ?: "Sin tratamiento",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                color = MediumText
            )
            Text(
                text = detail.prescription ?: "Sin prescripcion",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
                color = MediumText
            )
            Text(
                text = detail.notes ?: "Sin notas",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
                color = MediumText
            )
            Text(
                text = "Cita: ${detail.appointmentId ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
                color = MediumText
            )
        }
    }
}

private enum class DetailAction(val label: String) { LIST("Lista"), CREATE("Crear"), UPDATE("Actualizar"), DELETE("Eliminar"), FILTER_APPOINTMENT("Filtrar"), RELOAD("Recargar") }

@Preview(showBackground = true)
@Composable
fun DetailItemPreview() {
    Ocularis_MobileTheme {
        DetailItem(
            detail = DetailsDTO(
                id = 1,
                appointmentId = 1,
                diagnosis = "Miopia leve",
                prescription = "Gafas 1.25",
                notes = "Revisar en 6 meses",
                treatment = "Lentes diarios",
                followUp = "2024-12-01"
            )
        )
    }
}
