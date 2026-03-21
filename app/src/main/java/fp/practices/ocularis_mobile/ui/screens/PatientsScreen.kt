package fp.practices.ocularis_mobile.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
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
    Column(modifier = Modifier.fillMaxSize()) {
        PatientCrudPanel(
            onCreate = onCreate,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onSearchByAddress = onSearchByAddress,
            onReload = onReload,
            message = message
        )
        Spacer(modifier = Modifier.height(12.dp))
        PatientsList(patients = patients)
    }
}

@Composable
private fun PatientCrudPanel(
    onCreate: (PatientDTO) -> Unit,
    onUpdate: (Int, PatientDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByAddress: (String) -> Unit,
    onReload: () -> Unit,
    message: String?
) {
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Panel Pacientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (message != null) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
        if (localError != null) {
            Text(text = localError ?: "", color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id (para actualizar/borrar)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = dni, onValueChange = { dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección (buscar/crear)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                localError = null
                val patient = PatientDTO(
                    id = null,
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
                onCreate(patient)
            }) { Text("Crear") }

            ElevatedButton(onClick = {
                localError = null
                val targetId = id.toIntOrNull()
                if (targetId == null) {
                    localError = "Id inválido"
                    return@ElevatedButton
                }
                val patient = PatientDTO(
                    id = targetId,
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
                onUpdate(targetId, patient)
            }) { Text("Actualizar") }

            ElevatedButton(onClick = {
                localError = null
                val targetId = id.toIntOrNull()
                if (targetId == null) {
                    localError = "Id inválido"
                    return@ElevatedButton
                }
                onDelete(targetId)
            }) { Text("Eliminar") }

            ElevatedButton(onClick = {
                localError = null
                if (address.isBlank()) {
                    localError = "Ingresa una dirección para buscar"
                    return@ElevatedButton
                }
                onSearchByAddress(address)
            }) { Text("Buscar por dirección") }

            ElevatedButton(onClick = {
                localError = null
                onReload()
            }) { Text("Recargar") }
        }
    }
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
                text = patient.firstName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = patient.lastName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

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

