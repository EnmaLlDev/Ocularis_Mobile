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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.ui.auth.RoleAccess
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.DarkSurface
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue
import fp.practices.ocularis_mobile.viewmodel.PatientsViewModel

@Composable
fun PatientsScreen(
    modifier: Modifier = Modifier,
    roles: Set<String> = emptySet(),
    viewModel: PatientsViewModel = viewModel()
) {
    val patients by viewModel.patients.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    val message by viewModel.message.observeAsState(null)

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VibrantBlue)
            error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            else -> PatientsContent(
                patients = patients,
                roles = roles,
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
    roles: Set<String>,
    message: String?,
    onCreate: (PatientDTO) -> Unit,
    onUpdate: (Int, PatientDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByAddress: (String) -> Unit,
    onReload: () -> Unit
) {
    val canRead = RoleAccess.canReadPatients(roles)
    val canManage = RoleAccess.canManagePatients(roles)
    var currentAction by remember { mutableStateOf(PatientAction.LIST) }

    if (!canRead) {
        PermissionRequiredPanel()
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ActionChips(
            currentAction = currentAction,
            canManage = canManage,
            onSelect = { action ->
                if (!canManage && action != PatientAction.LIST && action != PatientAction.RELOAD) return@ActionChips
                if (action == PatientAction.RELOAD) {
                    onReload()
                    currentAction = PatientAction.LIST
                } else {
                    currentAction = action
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PatientCrudPanel(
            currentAction = currentAction,
            patients = patients,
            onCreate = onCreate,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onSearchByAddress = onSearchByAddress,
            message = message,
            onActionDone = { currentAction = PatientAction.LIST }
        )
    }
}

@Composable
private fun ActionChips(
    currentAction: PatientAction,
    canManage: Boolean,
    onSelect: (PatientAction) -> Unit
) {
    val items = buildList {
        add(PatientAction.LIST)
        if (canManage) {
            add(PatientAction.CREATE)
            add(PatientAction.UPDATE)
            add(PatientAction.DELETE)
            add(PatientAction.SEARCH)
        }
        add(PatientAction.RELOAD)
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Permiso requerido para acceder a esta vista", color = LightText)
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

    Column {
        Text("Pacientes", style = MaterialTheme.typography.titleLarge, color = LightText)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        localError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        when (currentAction) {
            PatientAction.LIST -> {
                Spacer(modifier = Modifier.height(8.dp))
                PatientsList(patients)
            }

            PatientAction.CREATE, PatientAction.UPDATE -> {
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id (solo actualizar)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = dni, onValueChange = { dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
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
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id a eliminar") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
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
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(onClick = {
                    if (address.isBlank()) {
                        localError = "Ingresa una dirección"
                        return@ElevatedButton
                    }
                    onSearchByAddress(address)
                    onActionDone()
                }) { Text("Buscar") }
            }

            PatientAction.RELOAD -> Unit
        }
    }
}

@Composable
private fun PatientsList(patients: List<PatientDTO>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(patients) { patient ->
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${patient.firstName} ${patient.lastName}", fontWeight = FontWeight.Bold, color = LightText)
                    Text("DNI: ${patient.dni ?: "N/D"}", color = MediumText)
                    Text("Email: ${patient.email ?: "N/D"}", color = MediumText)
                    Text("Teléfono: ${patient.phone ?: "N/D"}", color = MediumText)
                    Text("Dirección: ${patient.address ?: "N/D"}", color = MediumText)
                }
            }
        }
    }
}

private enum class PatientAction(val label: String) {
    LIST("Lista"),
    CREATE("Crear"),
    UPDATE("Actualizar"),
    DELETE("Eliminar"),
    SEARCH("Buscar"),
    RELOAD("Recargar")
}

