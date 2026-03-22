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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
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
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.viewmodel.PatientsViewModel
import java.time.LocalDate

@Composable
fun PatientsScreen(
    modifier: Modifier = Modifier,
    viewModel: PatientsViewModel = viewModel()
) {
    val patients by viewModel.patients.observeAsState(emptyList())
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

            else -> PatientsContent(
                patients = patients,
                message = message,
                onCreate = viewModel::createPatient,
                onUpdate = viewModel::updatePatient,
                onDelete = viewModel::deletePatient,
                onSearchByAddress = viewModel::searchByAddress,
                onReload = viewModel::loadPatients
            )
        }
    }
}

@Composable
private fun PatientsContent(
    patients: List<PatientDTO>,
    message: String?,
    onCreate: (PatientDTO) -> Unit,
    onUpdate: (Int, PatientDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByAddress: (String) -> Unit,
    onReload: () -> Unit
) {
    var currentAction by remember { mutableStateOf(PatientAction.LIST) }

    Row(modifier = Modifier.fillMaxSize()) {
        PatientSideNav(
            currentAction = currentAction,
            onActionSelected = { action ->
                if (action == PatientAction.RELOAD) {
                    onReload()
                    currentAction = PatientAction.LIST
                } else {
                    currentAction = action
                }
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            PatientCrudPanel(
                currentAction = currentAction,
                patients = patients,
                onCreate = onCreate,
                onUpdate = onUpdate,
                onDelete = onDelete,
                onSearchByAddress = onSearchByAddress,
                onReload = onReload,
                message = message,
                onActionDone = { currentAction = PatientAction.LIST }
            )
        }
    }
}

@Composable
private fun PatientSideNav(
    currentAction: PatientAction,
    onActionSelected: (PatientAction) -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            selected = currentAction == PatientAction.LIST,
            onClick = { onActionSelected(PatientAction.LIST) },
            icon = { Icon(Icons.Default.List, contentDescription = "Lista") },
            label = { Text("Lista") }
        )
        NavigationRailItem(
            selected = currentAction == PatientAction.CREATE,
            onClick = { onActionSelected(PatientAction.CREATE) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
            label = { Text("Crear") }
        )
        NavigationRailItem(
            selected = currentAction == PatientAction.UPDATE,
            onClick = { onActionSelected(PatientAction.UPDATE) },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Actualizar") },
            label = { Text("Actualizar") }
        )
        NavigationRailItem(
            selected = currentAction == PatientAction.DELETE,
            onClick = { onActionSelected(PatientAction.DELETE) },
            icon = { Icon(Icons.Default.Delete, contentDescription = "Eliminar") },
            label = { Text("Eliminar") }
        )
        NavigationRailItem(
            selected = currentAction == PatientAction.SEARCH,
            onClick = { onActionSelected(PatientAction.SEARCH) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = { Text("Buscar") }
        )
        NavigationRailItem(
            selected = currentAction == PatientAction.RELOAD,
            onClick = { onActionSelected(PatientAction.RELOAD) },
            icon = { Icon(Icons.Default.Refresh, contentDescription = "Recargar") },
            label = { Text("Recargar") }
        )
    }
}

@Composable
private fun PatientCrudPanel(
    currentAction: PatientAction,
    patients: List<PatientDTO>,
    onCreate: (PatientDTO) -> Unit,
    onUpdate: (Int, PatientDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByAddress: (String) -> Unit,
    onReload: () -> Unit,
    message: String?,
    onActionDone: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pacientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        message?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
        localError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        when (currentAction) {
            PatientAction.LIST -> {
                Spacer(modifier = Modifier.height(12.dp))
                PatientsList(patients = patients)
            }
            PatientAction.CREATE, PatientAction.UPDATE -> {
                PatientFormFields(
                    id = id,
                    onIdChange = { id = it },
                    firstName = firstName,
                    onFirstNameChange = { firstName = it },
                    lastName = lastName,
                    onLastNameChange = { lastName = it },
                    dni = dni,
                    onDniChange = { dni = it },
                    email = email,
                    onEmailChange = { email = it },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    address = address,
                    onAddressChange = { address = it },
                    showId = currentAction == PatientAction.UPDATE
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    val dto = PatientDTO(
                        id = id.toIntOrNull(),
                        dni = dni.ifBlank { null },
                        firstName = firstName,
                        secondName = null,
                        lastName = lastName,
                        secondLastName = null,
                        email = email.ifBlank { null },
                        phone = phone.ifBlank { null },
                        birthDate = null,
                        address = address.ifBlank { null }
                    )
                    if (currentAction == PatientAction.UPDATE) {
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
                }) {
                    Text(if (currentAction == PatientAction.UPDATE) "Actualizar" else "Crear")
                }
            }
            PatientAction.DELETE -> {
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
            PatientAction.SEARCH -> {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    localError = null
                    if (address.isBlank()) {
                        localError = "Ingresa una dirección"
                        return@ElevatedButton
                    }
                    onSearchByAddress(address)
                }) { Text("Buscar") }
            }
            PatientAction.RELOAD -> {
                onReload()
                onActionDone()
            }
        }
    }
}

@Composable
private fun PatientFormFields(
    id: String,
    onIdChange: (String) -> Unit,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    dni: String,
    onDniChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    showId: Boolean
) {
    if (showId) {
        OutlinedTextField(
            value = id,
            onValueChange = onIdChange,
            label = { Text("Id") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }
    OutlinedTextField(value = firstName, onValueChange = onFirstNameChange, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = lastName, onValueChange = onLastNameChange, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = dni, onValueChange = onDniChange, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    OutlinedTextField(value = address, onValueChange = onAddressChange, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
}

@Composable
fun PatientsList(patients: List<PatientDTO>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(patients) { patient ->
            PatientItem(patient = patient)
        }
    }
}

@Composable
fun PatientItem(patient: PatientDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${patient.firstName} ${patient.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "DNI: ${patient.dni ?: "N/D"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Email: ${patient.email ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Teléfono: ${patient.phone ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Dirección: ${patient.address ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private enum class PatientAction { LIST, CREATE, UPDATE, DELETE, SEARCH, RELOAD }

@Preview(showBackground = true)
@Composable
fun PatientItemPreview() {
    Ocularis_MobileTheme {
        PatientItem(
            patient = PatientDTO(
                id = 1,
                dni = "12345678",
                firstName = "John",
                secondName = "Doe",
                lastName = "Doe",
                secondLastName = "Doe",
                email = "john.c.calhoun@examplepetstore.com",
                phone = "123456789",
                birthDate = LocalDate.now() as String?,
                address = "Address"
            )
        )
    }
}

