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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
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
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.viewmodel.AppointmentsViewModel
import java.time.LocalDate

@Composable
fun AppointmentsScreen(modifier: Modifier = Modifier, roles: Set<String> = emptySet(), viewModel: AppointmentsViewModel = viewModel()) {
    val appointments by viewModel.appointments.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    val message by viewModel.message.observeAsState(null)

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VibrantBlue)
            error != null -> Text(text = "Error: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            else -> AppointmentsContent(appointments = appointments, roles = roles, message = message, onCreate = viewModel::createAppointment, onUpdate = viewModel::updateAppointment, onDelete = viewModel::deleteAppointment, onReload = viewModel::loadAppointments)
        }
    }
}

@Composable
private fun AppointmentsContent(appointments: List<AppointmentDTO>, roles: Set<String>, message: String?, onCreate: (AppointmentDTO) -> Unit, onUpdate: (Int, AppointmentDTO) -> Unit, onDelete: (Int) -> Unit, onReload: () -> Unit) {
    val canRead = RoleAccess.canReadAppointments(roles)
    val canManage = RoleAccess.canManageAppointments(roles)
    var currentAction by remember { mutableStateOf(AppointmentAction.LIST) }

    if (!canRead) { PermissionRequiredPanel(); return }
    if (!canManage && currentAction != AppointmentAction.LIST && currentAction != AppointmentAction.RELOAD) currentAction = AppointmentAction.LIST

    Row(modifier = Modifier.fillMaxSize()) {
        // compact vertical icon bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSurface)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NavIcon(icon = { Icon(Icons.Filled.List, contentDescription = "Lista", tint = if (currentAction == AppointmentAction.LIST) VibrantBlue else LightText) }, selected = currentAction == AppointmentAction.LIST, label = "Lista") { currentAction = AppointmentAction.LIST }
                if (canManage) {
                    NavIcon(icon = { Icon(Icons.Default.Add, contentDescription = "Crear", tint = if (currentAction == AppointmentAction.CREATE) VibrantBlue else LightText) }, selected = currentAction == AppointmentAction.CREATE, label = "Crear") { currentAction = AppointmentAction.CREATE }
                    NavIcon(icon = { Icon(Icons.Default.Edit, contentDescription = "Actualizar", tint = if (currentAction == AppointmentAction.UPDATE) VibrantBlue else LightText) }, selected = currentAction == AppointmentAction.UPDATE, label = "Actualizar") { currentAction = AppointmentAction.UPDATE }
                    NavIcon(icon = { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = if (currentAction == AppointmentAction.DELETE) VibrantBlue else LightText) }, selected = currentAction == AppointmentAction.DELETE, label = "Eliminar") { currentAction = AppointmentAction.DELETE }
                }
                NavIcon(icon = { Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = if (currentAction == AppointmentAction.RELOAD) VibrantBlue else LightText) }, selected = currentAction == AppointmentAction.RELOAD, label = "Recargar") { onReload(); currentAction = AppointmentAction.LIST }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(modifier = Modifier.weight(1f).background(DarkBackground)) {
            AppointmentCrudPanel(currentAction = currentAction, appointments = appointments, onCreate = onCreate, onUpdate = onUpdate, onDelete = onDelete, onReload = onReload, message = message, onActionDone = { currentAction = AppointmentAction.LIST })
        }
    }
}

@Composable
private fun AppointmentSideNav(currentAction: AppointmentAction, canManage: Boolean, onActionSelected: (AppointmentAction) -> Unit) {
    // kept for compatibility - the compact nav is implemented inline in AppointmentsContent
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Citas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightText)
        message?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
        localError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        when (currentAction) {
            AppointmentAction.LIST -> { Spacer(modifier = Modifier.height(12.dp)); AppointmentsList(appointments) }
            AppointmentAction.CREATE, AppointmentAction.UPDATE -> {
                OutlinedTextField(value = patientId, onValueChange = { patientId = it }, label = { Text("Id Paciente") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText))
                OutlinedTextField(value = doctorId, onValueChange = { doctorId = it }, label = { Text("Id Doctor") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText))
                OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Fecha/Hora (ISO)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText))
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Estado (SCHEDULED/...)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = { localError = null; val patient = patientId.toIntOrNull() ?: run { localError = "Id de paciente inválido"; return@ElevatedButton }; val doctor = doctorId.toIntOrNull() ?: run { localError = "Id de doctor inválido"; return@ElevatedButton }; val statusEnum = status.takeIf { it.isNotBlank() }?.let { runCatching { StateAppoinment.valueOf(it.trim().uppercase()) }.getOrNull() }; val dto = AppointmentDTO(id.toIntOrNull(), dateTime.ifBlank { null }, PatientDTO(id = patient, dni = null, firstName = "", secondName = null, lastName = "", secondLastName = null, email = null, phone = null, birthDate = null, address = null), DoctorDTO(id = doctor, firstName = "", secondName = null, lastName = "", secondLastName = null, dni = null, email = null, phone = null, licenseNumber = null, specialty = null), reason.ifBlank { null }, statusEnum); if (currentAction == AppointmentAction.UPDATE) { val targetId = dto.id ?: run { localError = "Id requerido para actualizar"; return@ElevatedButton }; onUpdate(targetId, dto) } else onCreate(dto.copy(id = null)); onActionDone() }, colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(containerColor = PrimaryBlue, contentColor = LightText)) { Text(if (currentAction == AppointmentAction.UPDATE) "Actualizar" else "Crear") }
            }
            AppointmentAction.DELETE -> {
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id a eliminar") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = { localError = null; val targetId = id.toIntOrNull() ?: run { localError = "Id inválido"; return@ElevatedButton }; onDelete(targetId); onActionDone() }, colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(containerColor = PrimaryBlue, contentColor = LightText)) { Text("Eliminar") }
            }
            AppointmentAction.RELOAD -> { onReload(); onActionDone() }
        }
    }
}

@Composable
fun AppointmentsList(appointments: List<AppointmentDTO>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(appointments) { appointment -> AppointmentItem(appointment) }
    }
}

@Composable
fun AppointmentItem(appointment: AppointmentDTO) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = DarkSurface), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = appointment.reason ?: "Sin motivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightText)
            Text(text = appointment.dateTime?.toString() ?: "Fecha no disponible", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp), color = MediumText)
            Text(text = "Paciente: ${appointment.patient?.firstName ?: "N/D"}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp), color = MediumText)
            Text(text = "Doctor: ${appointment.doctor?.firstName ?: "N/D"}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp), color = MediumText)
            Text(text = "Estado: ${appointment.status?.name ?: "N/D"}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MediumText)
        }
    }
}

private enum class AppointmentAction { LIST, CREATE, UPDATE, DELETE, RELOAD }

@Preview(showBackground = true)
@Composable
fun AppointmentItemPreview() {
    Ocularis_MobileTheme {
        AppointmentItem(appointment = AppointmentDTO(1, LocalDate.now().toString(), PatientDTO(1, "123", "Jane", null, "Doe", null, null, null, null, null), DoctorDTO(1, "Doc. Jose", "Luis", "Torrente", null, null, null, null, null, "Oftalmología"), "Chequeo", StateAppoinment.CONFIRMED))
    }
}

