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
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.ui.theme.Ocularis_MobileTheme
import fp.practices.ocularis_mobile.viewmodel.DoctorsViewModel

@Composable
fun DoctorsScreen(
    modifier: Modifier = Modifier,
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
    message: String?,
    onCreate: (DoctorDTO) -> Unit,
    onUpdate: (Int, DoctorDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByLicense: (String) -> Unit,
    onSearchBySpecialty: (String) -> Unit,
    onReload: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DoctorCrudPanel(
            onCreate = onCreate,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onSearchByLicense = onSearchByLicense,
            onSearchBySpecialty = onSearchBySpecialty,
            onReload = onReload,
            message = message
        )
        Spacer(modifier = Modifier.height(12.dp))
        DoctorsList(doctors = doctors)
    }
}

@Composable
private fun DoctorCrudPanel(
    onCreate: (DoctorDTO) -> Unit,
    onUpdate: (Int, DoctorDTO) -> Unit,
    onDelete: (Int) -> Unit,
    onSearchByLicense: (String) -> Unit,
    onSearchBySpecialty: (String) -> Unit,
    onReload: () -> Unit,
    message: String?
) {
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Panel Doctores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (message != null) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
        if (localError != null) {
            Text(text = localError ?: "", color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Id (actualizar/borrar)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Licencia") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Especialidad") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                localError = null
                val doctor = DoctorDTO(
                    id = null,
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
                onCreate(doctor)
            }) { Text("Crear") }

            ElevatedButton(onClick = {
                localError = null
                val targetId = id.toIntOrNull()
                if (targetId == null) {
                    localError = "Id inválido"
                    return@ElevatedButton
                }
                val doctor = DoctorDTO(
                    id = targetId,
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
                onUpdate(targetId, doctor)
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
                if (license.isBlank()) {
                    localError = "Ingresa licencia para buscar"
                    return@ElevatedButton
                }
                onSearchByLicense(license)
            }) { Text("Buscar por licencia") }

            ElevatedButton(onClick = {
                localError = null
                if (specialty.isBlank()) {
                    localError = "Ingresa especialidad para buscar"
                    return@ElevatedButton
                }
                onSearchBySpecialty(specialty)
            }) { Text("Buscar por especialidad") }

            ElevatedButton(onClick = {
                localError = null
                onReload()
            }) { Text("Recargar") }
        }
    }
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
                text = doctor.firstName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = doctor.lastName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

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

