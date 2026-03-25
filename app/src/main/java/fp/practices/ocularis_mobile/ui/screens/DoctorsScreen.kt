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
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
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

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            error != null -> {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> DoctorsContent(
                doctors = doctors,
                roles = roles,
                message = message,
                onCreate = viewModel::createDoctor,
                onUpdate = viewModel::updateDoctor,
                onDelete = viewModel::deleteDoctor,
                onSearchByLicense = viewModel::searchByLicense,
                onSearchBySpecialty = viewModel::searchBySpecialty,
                onReload = viewModel::loadDoctors
            )
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
    var currentAction by remember { mutableStateOf(DoctorAction.LIST) }

    if (!canRead) {
        PermissionRequiredPanel()
        return
    }

    if (!canManage && currentAction != DoctorAction.LIST && currentAction != DoctorAction.RELOAD) {
        currentAction = DoctorAction.LIST
    }

    Row(modifier = Modifier.fillMaxSize()) {
        DoctorSideNav(
            currentAction = currentAction,
            canManage = canManage,
            onActionSelected = { action ->
                if (!canManage && action != DoctorAction.LIST && action != DoctorAction.RELOAD) {
                    return@DoctorSideNav
                }
                if (action == DoctorAction.RELOAD) {
                    onReload()
                    currentAction = DoctorAction.LIST
                } else {
                    currentAction = action
                }
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            DoctorCrudPanel(
                currentAction = currentAction,
                doctors = doctors,
                onCreate = onCreate,
                onUpdate = onUpdate,
                onDelete = onDelete,
                onSearchByLicense = onSearchByLicense,
                onSearchBySpecialty = onSearchBySpecialty,
                onReload = onReload,
                message = message,
                onActionDone = { currentAction = DoctorAction.LIST }
            )
        }
    }
}

@Composable
private fun DoctorSideNav(
    currentAction: DoctorAction,
    canManage: Boolean,
    onActionSelected: (DoctorAction) -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            selected = currentAction == DoctorAction.LIST,
            onClick = { onActionSelected(DoctorAction.LIST) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lista") },
            label = { Text("Lista") }
        )
        if (canManage) {
            NavigationRailItem(
                selected = currentAction == DoctorAction.CREATE,
                onClick = { onActionSelected(DoctorAction.CREATE) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                label = { Text("Crear") }
            )
            NavigationRailItem(
                selected = currentAction == DoctorAction.UPDATE,
                onClick = { onActionSelected(DoctorAction.UPDATE) },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Actualizar") },
                label = { Text("Actualizar") }
            )
            NavigationRailItem(
                selected = currentAction == DoctorAction.DELETE,
                onClick = { onActionSelected(DoctorAction.DELETE) },
                icon = { Icon(Icons.Default.Delete, contentDescription = "Eliminar") },
                label = { Text("Eliminar") }
            )
        }
        if (canManage) {
            NavigationRailItem(
                selected = currentAction == DoctorAction.SEARCH_LICENSE,
                onClick = { onActionSelected(DoctorAction.SEARCH_LICENSE) },
                icon = { Icon(Icons.Default.Search, contentDescription = "Licencia") },
                label = { Text("Licencia") }
            )
            NavigationRailItem(
                selected = currentAction == DoctorAction.SEARCH_SPECIALTY,
                onClick = { onActionSelected(DoctorAction.SEARCH_SPECIALTY) },
                icon = { Icon(Icons.Default.Search, contentDescription = "Especialidad") },
                label = { Text("Especialidad") }
            )
        }
        NavigationRailItem(
            selected = currentAction == DoctorAction.RELOAD,
            onClick = { onActionSelected(DoctorAction.RELOAD) },
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
private fun DoctorCrudPanel(
    currentAction: DoctorAction,
    doctors: List<DoctorDTO>,
    onCreate: (DoctorDTO) -> Unit,
    onUpdate: (Int, DoctorDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByLicense: (String) -> Unit,
    onSearchBySpecialty: (String) -> Unit,
    onReload: () -> Unit,
    message: String?,
    onActionDone: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Doctores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        message?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
        localError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        when (currentAction) {
            DoctorAction.LIST -> {
                Spacer(modifier = Modifier.height(12.dp))
                DoctorsList(doctors = doctors)
            }
            DoctorAction.CREATE, DoctorAction.UPDATE -> {
                DoctorFormFields(
                    id = id,
                    onIdChange = { id = it },
                    firstName = firstName,
                    onFirstNameChange = { firstName = it },
                    lastName = lastName,
                    onLastNameChange = { lastName = it },
                    email = email,
                    onEmailChange = { email = it },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    license = license,
                    onLicenseChange = { license = it },
                    specialty = specialty,
                    onSpecialtyChange = { specialty = it },
                    showId = currentAction == DoctorAction.UPDATE
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    val dto = DoctorDTO(
                        id = id.toIntOrNull(),
                        firstName = firstName,
                        secondName = null,
                        lastName = lastName,
                        secondLastName = null,
                        dni = null,
                        email = email.ifBlank { null },
                        phone = phone.ifBlank { null },
                        licenseNumber = license.ifBlank { null },
                        specialty = specialty.ifBlank { null }
                    )
                    if (currentAction == DoctorAction.UPDATE) {
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
                }) { Text(if (currentAction == DoctorAction.UPDATE) "Actualizar" else "Crear") }
            }
            DoctorAction.DELETE -> {
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
            DoctorAction.SEARCH_LICENSE -> {
                OutlinedTextField(
                    value = license,
                    onValueChange = { license = it },
                    label = { Text("Licencia") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    if (license.isBlank()) {
                        localError = "Ingresa licencia"
                        return@ElevatedButton
                    }
                    onSearchByLicense(license)
                }) { Text("Buscar") }
            }
            DoctorAction.SEARCH_SPECIALTY -> {
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Especialidad") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    if (specialty.isBlank()) {
                        localError = "Ingresa especialidad"
                        return@ElevatedButton
                    }
                    onSearchBySpecialty(specialty)
                }) { Text("Buscar") }
            }
            DoctorAction.RELOAD -> {
                onReload()
                onActionDone()
            }
        }
    }
}

@Composable
private fun DoctorFormFields(
    id: String,
    onIdChange: (String) -> Unit,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    license: String,
    onLicenseChange: (String) -> Unit,
    specialty: String,
    onSpecialtyChange: (String) -> Unit,
    showId: Boolean
) {
    if (showId) {
        OutlinedTextField(value = id, onValueChange = onIdChange, label = { Text("Id") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
    OutlinedTextField(value = firstName, onValueChange = onFirstNameChange, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = lastName, onValueChange = onLastNameChange, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = license, onValueChange = onLicenseChange, label = { Text("Licencia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = specialty, onValueChange = onSpecialtyChange, label = { Text("Especialidad") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
}

@Composable
fun DoctorsList(doctors: List<DoctorDTO>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(doctors) { doctor ->
            DoctorItem(doctor = doctor)
        }
    }
}

@Composable
fun DoctorItem(doctor: DoctorDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${doctor.firstName} ${doctor.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Licencia: ${doctor.licenseNumber ?: "N/D"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Especialidad: ${doctor.specialty ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Email: ${doctor.email ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Teléfono: ${doctor.phone ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private enum class DoctorAction { LIST, CREATE, UPDATE, DELETE, SEARCH_LICENSE, SEARCH_SPECIALTY, RELOAD }

@Preview(showBackground = true)
@Composable
fun DoctorItemPreview() {
    Ocularis_MobileTheme {
        DoctorItem(
            doctor = DoctorDTO(
                id = 1,
                dni = "12345678",
                firstName = "John",
                secondName = "Doe",
                lastName = "Doe",
                secondLastName = "Doe",
                email = "john.c.calhoun@examplepetstore.com",
                phone = "123456789",
                licenseNumber = "123456",
                specialty = "Specialty"
            )
        )
    }
}

