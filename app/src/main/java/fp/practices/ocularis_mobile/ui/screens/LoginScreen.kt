package fp.practices.ocularis_mobile.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import fp.practices.ocularis_mobile.R
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.viewmodel.RegisterViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Roles disponibles para el registro de usuarios.
 */
enum class RegisterRole {
    PATIENT,
    DOCTOR
}

/**
 * Pantalla de inicio de sesión y registro de pacientes/doctores.
 * @param navController controlador de navegación
 * @param isLoading indica si hay una petición en curso
 * @param error mensaje de error a mostrar
 * @param onLogin callback de autenticación
 * @param onClearError callback para limpiar errores
 * @param showRegisterOnStart abre el diálogo de registro al inicio
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    isLoading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onClearError: () -> Unit,
    showRegisterOnStart: Boolean = false,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(showRegisterOnStart) }

    val registerViewModel: RegisterViewModel = viewModel()
    val registerState by registerViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var registerRole by remember { mutableStateOf(RegisterRole.PATIENT) }

    var firstName by remember { mutableStateOf("") }
    var secondName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var secondLastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(showRegister) {
        if (showRegister) {
            registerViewModel.clearStatus()
        }
    }

    LaunchedEffect(username, password) {
        if (!error.isNullOrBlank()) {
            onClearError()
        }
    }

    LaunchedEffect(registerState.successMessage) {
        registerState.successMessage?.let { message ->
            showRegister = false
            firstName = ""; secondName = ""; lastName = ""; secondLastName = ""
            dni = ""; email = ""; phone = ""; birthDate = ""; address = ""
            licenseNumber = ""; specialty = ""
            snackbarHostState.showSnackbar(message)
            registerViewModel.clearStatus()
        }
    }

    val isDoctor = registerRole == RegisterRole.DOCTOR
    val isFormValid = firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        dni.isNotBlank() &&
        email.isNotBlank() &&
        phone.isNotBlank() &&
        if (isDoctor) {
            licenseNumber.isNotBlank() && specialty.isNotBlank()
        } else {
            birthDate.isNotBlank() && address.isNotBlank()
        }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        IconButton(
            onClick = { navController.navigate("dashboard") },
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver al Dashboard",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Espacio superior para el botón de retroceso

            Image(
                painter = painterResource(id = R.drawable.logo_clinica),
                contentDescription = "OCULARIS",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bienvenido de nuevo, inicia sesión para acceder al sistema",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Alternar visibilidad de contraseña")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!error.isNullOrBlank()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Botón Principal de Acción ---
            Button(
                onClick = { onLogin(username, password) },
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { showRegister = true }
            ) {
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    if (showRegister) {
        AlertDialog(
            onDismissRequest = {
                showRegister = false
                registerViewModel.clearStatus()
            },
            title = { Text("Registro", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = registerRole == RegisterRole.PATIENT,
                            onClick = { registerRole = RegisterRole.PATIENT }
                        )
                        Text("Paciente", modifier = Modifier.padding(end = 12.dp))
                        RadioButton(
                            selected = registerRole == RegisterRole.DOCTOR,
                            onClick = { registerRole = RegisterRole.DOCTOR }
                        )
                        Text("Doctor")
                    }

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = secondName,
                        onValueChange = { secondName = it },
                        label = { Text("Segundo nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = secondLastName,
                        onValueChange = { secondLastName = it },
                        label = { Text("Segundo apellido") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { dni = it },
                        label = { Text("DNI") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (isDoctor) {
                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("Número de licencia") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = specialty,
                            onValueChange = { specialty = it },
                            label = { Text("Especialidad") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { },
                            label = { Text("Fecha de nacimiento") },
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Dirección") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    registerState.error?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isDoctor) {
                            registerViewModel.registerDoctor(
                                DoctorDTO(
                                    id = null,
                                    firstName = firstName.trim(),
                                    secondName = secondName.trim().ifBlank { null },
                                    lastName = lastName.trim(),
                                    secondLastName = secondLastName.trim().ifBlank { null },
                                    dni = dni.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    licenseNumber = licenseNumber.trim(),
                                    specialty = specialty.trim()
                                )
                            )
                        } else {
                            registerViewModel.registerPatient(
                                PatientDTO(
                                    id = null,
                                    dni = dni.trim(),
                                    firstName = firstName.trim(),
                                    secondName = secondName.trim().ifBlank { null },
                                    lastName = lastName.trim(),
                                    secondLastName = secondLastName.trim().ifBlank { null },
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    birthDate = birthDate.trim(),
                                    address = address.trim()
                                )
                            )
                        }
                    },
                    enabled = !registerState.isLoading && isFormValid,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (registerState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Registrar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRegister = false
                    registerViewModel.clearStatus()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        birthDate = datePickerState.selectedDateMillis?.let { millis ->
                            LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
                                .toString()
                        }.orEmpty()
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
