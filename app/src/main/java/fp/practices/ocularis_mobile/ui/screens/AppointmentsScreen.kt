package fp.practices.ocularis_mobile.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.model.StateAppoinment
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.DarkSurfaceVariant
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.viewmodel.AppointmentsViewModel
import java.time.LocalDate

/**
 * Pantalla de gestión de citas médicas con operaciones CRUD.
 * @param roles roles del usuario
 * @param viewModel ViewModel de citas
 */
@Composable
fun AppointmentsScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
    doctorId: Long? = null,
    viewModel: AppointmentsViewModel = viewModel()
) {
    val appointments by viewModel.appointments.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    val message by viewModel.message.observeAsState(null)
    val isPatient = roles.contains("PATIENT")
    val isDoctor = roles.contains("DOCTOR")

    LaunchedEffect(isDoctor, isPatient, doctorId) {
        when {
            isDoctor -> viewModel.loadForDoctor(doctorId)
            else -> viewModel.loadAppointments(isPatient)
        }
    }

    val onReload: () -> Unit = {
        when {
            isDoctor -> viewModel.loadForDoctor(doctorId)
            else -> viewModel.loadAppointments(isPatient)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VibrantBlue)
            error != null -> {
                val is403 = error?.contains("403") == true
                Log.d("AppointmentsScreen", "Error: $error, isPatient: $isPatient, is403: $is403, roles: $roles")
                val errorMessage = if (isPatient && is403) {
                    "No tienes permiso para ver todas las citas.\n(Se necesita endpoint /api/my-appointments)"
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
            else -> AppointmentsContent(
                appointments = appointments,
                roles = roles,
                message = message,
                onCreate = viewModel::createAppointment,
                onUpdate = viewModel::updateAppointment,
                onDelete = viewModel::deleteAppointment,
                onReload = onReload
            )
        }
    }
}

/**
 * Contenido principal de citas: lista y operaciones CRUD según permisos.
 */
@Composable
private fun AppointmentsContent(appointments: List<AppointmentDTO>, roles: Set<String>, message: String?, onCreate: (AppointmentDTO) -> Unit, onUpdate: (Int, AppointmentDTO) -> Unit, onDelete: (Int) -> Unit, onReload: () -> Unit) {
    val canRead = RoleAccess.canReadAppointments(roles)
    val canManage = RoleAccess.canManageAppointments(roles)
    var currentAction by remember { mutableStateOf(AppointmentAction.LIST) }

    if (!canRead) { PermissionRequiredPanel(); return }
    if (!canManage && currentAction != AppointmentAction.LIST && currentAction != AppointmentAction.RELOAD) currentAction = AppointmentAction.LIST

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ActionChips(
            currentAction = currentAction,
            canManage = canManage,
            onSelect = { action ->
                if (!canManage && action != AppointmentAction.LIST && action != AppointmentAction.RELOAD) return@ActionChips
                if (action == AppointmentAction.RELOAD) {
                    onReload()
                    currentAction = AppointmentAction.LIST
                } else {
                    currentAction = action
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

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

@Composable
private fun ActionChips(
    currentAction: AppointmentAction,
    canManage: Boolean,
    onSelect: (AppointmentAction) -> Unit
) {
    val items = buildList {
        add(AppointmentAction.LIST)
        if (canManage) {
            add(AppointmentAction.CREATE)
            add(AppointmentAction.UPDATE)
            add(AppointmentAction.DELETE)
        }
        add(AppointmentAction.RELOAD)
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
private fun AppointmentCrudPanel(currentAction: AppointmentAction, appointments: List<AppointmentDTO>, onCreate: (AppointmentDTO) -> Unit, onUpdate: (Int, AppointmentDTO) -> Unit, onDelete: (Int) -> Unit, onReload: () -> Unit, message: String?, onActionDone: () -> Unit) {
    var id by remember { mutableStateOf("") }
    var patientId by remember { mutableStateOf("") }
    var doctorId by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val title = when (currentAction) {
        AppointmentAction.LIST -> "Citas"
        AppointmentAction.CREATE -> "Crear cita"
        AppointmentAction.UPDATE -> "Actualizar cita"
        AppointmentAction.DELETE -> "Eliminar cita"
        AppointmentAction.RELOAD -> "Recargar"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (currentAction) {
                    AppointmentAction.LIST -> AppointmentsList(appointments)
                    AppointmentAction.CREATE, AppointmentAction.UPDATE -> {
                        OutlinedTextField(
                            value = patientId,
                            onValueChange = { patientId = it },
                            label = { Text("Id Paciente") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = MediumText,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        OutlinedTextField(
                            value = doctorId,
                            onValueChange = { doctorId = it },
                            label = { Text("Id Doctor") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = MediumText,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        OutlinedTextField(
                            value = dateTime,
                            onValueChange = { dateTime = it },
                            label = { Text("Fecha (yyyy-MM-dd)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = MediumText,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Motivo") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = MediumText,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        OutlinedTextField(
                            value = status,
                            onValueChange = { status = it },
                            label = { Text("Estado (SCHEDULED/...) ") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = MediumText,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ElevatedButton(
                            onClick = {
                                localError = null
                                val patient = patientId.toIntOrNull() ?: run { localError = "Id de paciente inválido"; return@ElevatedButton }
                                val doctor = doctorId.toIntOrNull() ?: run { localError = "Id de doctor inválido"; return@ElevatedButton }
                                if (dateTime.isNotBlank() && !Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(dateTime.trim())) {
                                    localError = "Fecha inválida. Usa yyyy-MM-dd"
                                    return@ElevatedButton
                                }
                                val statusEnum = status.takeIf { it.isNotBlank() }?.let { runCatching { StateAppoinment.valueOf(it.trim().uppercase()) }.getOrNull() }
                                val dto = AppointmentDTO(
                                    id.toIntOrNull(),
                                    dateTime.ifBlank { null },
                                    PatientDTO(id = patient, dni = null, firstName = "", secondName = null, lastName = "", secondLastName = null, email = null, phone = null, birthDate = null, address = null),
                                    DoctorDTO(id = doctor, firstName = "", secondName = null, lastName = "", secondLastName = null, dni = null, email = null, phone = null, licenseNumber = null, specialty = null),
                                    reason.ifBlank { null },
                                    statusEnum
                                )
                                if (currentAction == AppointmentAction.UPDATE) {
                                    val targetId = dto.id ?: run { localError = "Id requerido para actualizar"; return@ElevatedButton }
                                    onUpdate(targetId, dto)
                                } else {
                                    onCreate(dto.copy(id = null))
                                }
                                onActionDone()
                            }) {
                            Text(if (currentAction == AppointmentAction.UPDATE) "Actualizar" else "Crear")
                        }
                    }
                    AppointmentAction.DELETE -> {
                        OutlinedTextField(
                            value = id,
                            onValueChange = { id = it },
                            label = { Text("Id a eliminar") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = MediumText,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        ElevatedButton(
                            onClick = {
                                localError = null
                                val targetId = id.toIntOrNull() ?: run { localError = "Id inválido"; return@ElevatedButton }
                                onDelete(targetId)
                                onActionDone()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(containerColor = PrimaryBlue, contentColor = LightText)
                        ) { Text("Eliminar") }
                    }
                    AppointmentAction.RELOAD -> { onReload(); onActionDone() }
                }
            }
}

@Composable
fun AppointmentsList(appointments: List<AppointmentDTO>) {
    if (appointments.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            Text(text = "No hay citas registradas", color = MediumText)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appointments) { appointment -> AppointmentItem(appointment) }
    }
}

@Composable
fun AppointmentItem(appointment: AppointmentDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = appointment.reason ?: "Sin motivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightText)
            Text(text = appointment.dateTime?.toString() ?: "Fecha no disponible", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp), color = MediumText)
            Text(text = "Paciente: ${appointment.patient?.firstName ?: "N/D"}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp), color = MediumText)
            Text(text = "Doctor: ${appointment.doctor?.firstName ?: "N/D"}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp), color = MediumText)
            Text(text = "Estado: ${appointment.status?.name ?: "N/D"}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MediumText)
            Text(text = "Id: ${appointment.id?.toString() ?: "Sin Id"}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MediumText)
        }
    }
}

private enum class AppointmentAction(val label: String) { LIST("Lista"), CREATE("Crear"), UPDATE("Actualizar"), DELETE("Eliminar"), RELOAD("Recargar") }

@Preview(showBackground = true)
@Composable
fun AppointmentItemPreview() {
    Ocularis_MobileTheme {
        AppointmentItem(
            appointment = AppointmentDTO(1,
                LocalDate.now().toString(),
                PatientDTO(1,
                    "123",
                    "Jane",
                    null,
                    "Doe",
                    null,
                    null,
                    null,
                    null, null),
                DoctorDTO(1, "Doc. Jose", "Luis", "Torrente", null, null, null, null, null, "Oftalmología"), "Chequeo",
                StateAppoinment.CONFIRMED))
    }
}
