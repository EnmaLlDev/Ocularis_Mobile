package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
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
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.viewmodel.DoctorsViewModel

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

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VibrantBlue)
            error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            else -> DoctorsContent(doctors, roles, message, viewModel::createDoctor, viewModel::updateDoctor, viewModel::deleteDoctor, viewModel::searchByLicense, viewModel::searchBySpecialty, viewModel::loadDoctors)
        }
    }
}

@Composable
private fun DoctorsContent(
    doctors: List<DoctorDTO>,
    roles: Set<String>,
    message: String?,
    onCreate: (DoctorDTO) -> Unit,
    onUpdate: (Int, DoctorDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByLicense: (String) -> Unit,
    onSearchBySpecialty: (String) -> Unit,
    onReload: () -> Unit
) {
    val canRead = RoleAccess.canReadDoctors(roles)
    val canManage = RoleAccess.canManageDoctors(roles)
    var action by remember { mutableStateOf(DoctorAction.LIST) }
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    if (!canRead) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permiso requerido para acceder a esta vista", color = LightText)
        }
        return
    }

    val actions = buildList {
        add(DoctorAction.LIST)
        if (canManage) addAll(listOf(DoctorAction.CREATE, DoctorAction.UPDATE, DoctorAction.DELETE, DoctorAction.SEARCH_LICENSE, DoctorAction.SEARCH_SPECIALTY))
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
                        } else {
                            action = item
                        }
                    },
                    label = { Text(item.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Doctores", style = MaterialTheme.typography.titleLarge, color = LightText)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        localError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        when (action) {
            DoctorAction.LIST -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(doctors) { doctor ->
                    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${doctor.firstName} ${doctor.lastName}", color = LightText, fontWeight = FontWeight.Bold)
                            Text("Licencia: ${doctor.licenseNumber ?: "N/D"}", color = MediumText)
                            Text("Especialidad: ${doctor.specialty ?: "N/D"}", color = MediumText)
                            Text("Email: ${doctor.email ?: "N/D"}", color = MediumText)
                            Text("Teléfono: ${doctor.phone ?: "N/D"}", color = MediumText)
                        }
                    }
                }
            }

            DoctorAction.CREATE, DoctorAction.UPDATE -> {
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id (solo actualizar)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Licencia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Especialidad") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    val dto = DoctorDTO(id = id.toIntOrNull(), firstName = firstName, secondName = null, lastName = lastName, secondLastName = null, dni = null, email = email.ifBlank { null }, phone = phone.ifBlank { null }, licenseNumber = license.ifBlank { null }, specialty = specialty.ifBlank { null })
                    if (action == DoctorAction.UPDATE) {
                        val targetId = dto.id
                        if (targetId == null) {
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

private enum class DoctorAction(val label: String) {
    LIST("Lista"),
    CREATE("Crear"),
    UPDATE("Actualizar"),
    DELETE("Eliminar"),
    SEARCH_LICENSE("Licencia"),
    SEARCH_SPECIALTY("Especialidad"),
    RELOAD("Recargar")
}

