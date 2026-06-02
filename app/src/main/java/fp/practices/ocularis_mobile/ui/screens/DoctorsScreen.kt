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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.viewmodel.DoctorsViewModel

/**
 * Pantalla de gestión de médicos con operaciones CRUD y búsqueda.
 * @param roles roles del usuario
 * @param viewModel ViewModel de médicos
 */
@Composable
fun DoctorsScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
    viewModel: DoctorsViewModel = viewModel()
) {
    val doctors by viewModel.doctors.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    val message by viewModel.message.observeAsState(null)
    val appointmentsLoading by viewModel.appointmentsLoading.observeAsState(false)
    val doctorAppointments by viewModel.doctorAppointments.observeAsState(emptyList())
    val appointmentDetails by viewModel.appointmentDetails.observeAsState(emptyMap())

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VibrantBlue)
            error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            else -> DoctorsContent(
                doctors = doctors,
                roles = roles,
                message = message,
                appointmentsLoading = appointmentsLoading,
                doctorAppointments = doctorAppointments,
                appointmentDetails = appointmentDetails,
                onCreate = viewModel::createDoctor,
                onUpdate = viewModel::updateDoctor,
                onDelete = viewModel::deleteDoctor,
                onSearchByLicense = viewModel::searchByLicense,
                onSearchBySpecialty = viewModel::searchBySpecialty,
                onReload = viewModel::loadDoctors,
                onLoadAppointmentDetails = viewModel::loadAppointmentDetails
            )
        }
    }
}

/**
 * Contenido principal de médicos: lista, búsqueda y operaciones según permisos.
 */
@Composable
private fun DoctorsContent(
    doctors: List<DoctorDTO>,
    roles: Set<String>,
    message: String?,
    appointmentsLoading: Boolean,
    doctorAppointments: List<AppointmentDTO>,
    appointmentDetails: Map<Int, List<DetailsDTO>>,
    onCreate: (DoctorDTO) -> Unit,
    onUpdate: (Int, DoctorDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByLicense: (String) -> Unit,
    onSearchBySpecialty: (String) -> Unit,
    onReload: () -> Unit,
    onLoadAppointmentDetails: (Int) -> Unit
) {
    val canRead = RoleAccess.canReadDoctors(roles)
    val canManage = RoleAccess.canManageDoctors(roles)
    var action by remember { mutableStateOf(DoctorAction.LIST) }
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var expandedDoctorId by remember { mutableStateOf<Int?>(null) }

    if (!canRead) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permiso requerido para acceder a esta vista", color = LightText)
        }
        return
    }

    val actions = buildList {
        add(DoctorAction.LIST)
        if (canManage) {
            add(DoctorAction.CREATE)
            add(DoctorAction.UPDATE)
            add(DoctorAction.DELETE)
            add(DoctorAction.SEARCH_LICENSE)
            add(DoctorAction.SEARCH_SPECIALTY)
        }
        add(DoctorAction.RELOAD)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(actions) { item ->
                FilterChip(
                    selected = action == item,
                    onClick = {
                        if (item == DoctorAction.RELOAD) {
                            onReload()
                            action = DoctorAction.LIST
                        } else if (!canManage && item != DoctorAction.LIST) {
                            localError = "Permiso requerido para gestionar"
                            action = DoctorAction.LIST
                        } else {
                            action = item
                        }
                    },
                    label = { Text(item.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (action) {
            DoctorAction.LIST -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(doctors) { doctor ->
                    val isExpanded = expandedDoctorId == doctor.id
                    Card(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${doctor.firstName} ${doctor.lastName}", color = LightText, fontWeight = FontWeight.Bold)
                                    Text("Licencia: ${doctor.licenseNumber ?: "N/D"}", color = MediumText)
                                    Text("Especialidad: ${doctor.specialty ?: "N/D"}", color = MediumText)
                                    Text("Email: ${doctor.email ?: "N/D"}", color = MediumText)
                                    Text("Teléfono: ${doctor.phone ?: "N/D"}", color = MediumText)
                                }
                                IconButton(onClick = {
                                    if (isExpanded) {
                                        expandedDoctorId = null
                                    } else {
                                        expandedDoctorId = doctor.id
                                        doctor.id?.let { onLoadAppointmentDetails(it) }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = if (isExpanded) "Ocultar citas" else "Ver citas",
                                        tint = VibrantBlue
                                    )
                                }
                            }

                            if (isExpanded) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    "Citas del doctor",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = LightText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                when {
                                    appointmentsLoading -> Box(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = VibrantBlue)
                                    }
                                    doctorAppointments.isEmpty() -> Text(
                                        "Sin citas registradas",
                                        color = MediumText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        doctorAppointments.forEach { appt ->
                                            AppointmentDetailItem(
                                                appointment = appt,
                                                details = appointmentDetails[appt.id] ?: emptyList()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            DoctorAction.CREATE, DoctorAction.UPDATE -> {
                val formTitle = if (action == DoctorAction.CREATE) "Nuevo doctor" else "Actualizar doctor"
                val formSubtitle = if (action == DoctorAction.CREATE) {
                    "Completa los datos para registrar al doctor"
                } else {
                    "Actualiza los datos del doctor"
                }
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(formTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightText)
                            Text(formSubtitle, style = MaterialTheme.typography.bodyMedium, color = MediumText)
                        }
                    }
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = dni, onValueChange = { dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Licencia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Especialidad") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    ElevatedButton(onClick = {
                        val trimmedFirstName = firstName.trim()
                        val trimmedLastName = lastName.trim()
                        val trimmedDni = dni.trim()
                        if (trimmedFirstName.isBlank() || trimmedLastName.isBlank()) {
                            localError = "Nombre y apellidos son obligatorios"
                            return@ElevatedButton
                        }
                        if (trimmedDni.isBlank()) {
                            localError = "El DNI es obligatorio"
                            return@ElevatedButton
                        }

                        val dto = DoctorDTO(
                            id = id.toIntOrNull() ?: 0,
                            firstName = trimmedFirstName,
                            secondName = null,
                            lastName = trimmedLastName,
                            secondLastName = null,
                            dni = trimmedDni,
                            email = email.trim().ifBlank { null },
                            phone = phone.trim().ifBlank { null },
                            licenseNumber = license.trim().ifBlank { "" },
                            specialty = specialty.trim().ifBlank { "" }
                        )
                        if (action == DoctorAction.UPDATE) {
                            val targetId = dto.id
                            if (targetId == null || targetId == 0) {
                                localError = "Id requerido para actualizar"
                                return@ElevatedButton
                            }
                            onUpdate(targetId, dto)
                        } else {
                            onCreate(dto.copy(id = null))
                        }
                        action = DoctorAction.LIST
                    }) {
                        Text(if (action == DoctorAction.UPDATE) "Actualizar" else "Crear")
                    }
                }
            }

            DoctorAction.DELETE -> {
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id a eliminar") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    val targetId = id.toIntOrNull()
                    if (targetId == null) {
                        localError = "Id inválido"
                        return@ElevatedButton
                    }
                    onDelete(targetId)
                    action = DoctorAction.LIST
                }) { Text("Eliminar") }
            }

            DoctorAction.SEARCH_LICENSE -> {
                OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Licencia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    if (license.isBlank()) {
                        localError = "Ingresa licencia"
                        return@ElevatedButton
                    }
                    onSearchByLicense(license)
                    action = DoctorAction.LIST
                }) { Text("Buscar") }
            }

            DoctorAction.SEARCH_SPECIALTY -> {
                OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Especialidad") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    if (specialty.isBlank()) {
                        localError = "Ingresa especialidad"
                        return@ElevatedButton
                    }
                    onSearchBySpecialty(specialty)
                    action = DoctorAction.LIST
                }) { Text("Buscar") }
            }

            DoctorAction.RELOAD -> Unit
        }
    }
}

@Composable
private fun AppointmentDetailItem(
    appointment: AppointmentDTO,
    details: List<DetailsDTO>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Cita #${appointment.id ?: "?"} — ${appointment.dateTime ?: "Sin fecha"}",
                color = LightText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            appointment.reason?.let {
                Text("Motivo: $it", color = MediumText, style = MaterialTheme.typography.bodySmall)
            }
            appointment.status?.let {
                Text("Estado: $it", color = MediumText, style = MaterialTheme.typography.bodySmall)
            }
            appointment.patient?.let { patient ->
                Text(
                    "Paciente: ${patient.firstName} ${patient.lastName}",
                    color = MediumText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Detalles clínicos",
                    color = VibrantBlue,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                details.forEach { detail ->
                    detail.diagnosis?.let { Text("Diagnóstico: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                    detail.treatment?.let { Text("Tratamiento: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                    detail.prescription?.let { Text("Prescripción: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                    detail.notes?.let { Text("Notas: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                    detail.followUp?.let { Text("Seguimiento: $it", color = MediumText, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

private enum class DoctorAction(val label: String) {
    LIST("Lista"),
    CREATE("Nuevo Doctor"),
    UPDATE("Actualizar"),
    DELETE("Eliminar"),
    SEARCH_LICENSE("Licencia"),
    SEARCH_SPECIALTY("Especialidad"),
    RELOAD("Recargar")
}
