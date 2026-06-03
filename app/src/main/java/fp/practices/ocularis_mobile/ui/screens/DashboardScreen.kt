package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import fp.practices.ocularis_mobile.R
import fp.practices.ocularis_mobile.data.model.ContactMessageDTO
import fp.practices.ocularis_mobile.data.model.Dashboard
import fp.practices.ocularis_mobile.data.model.Curiosity
import fp.practices.ocularis_mobile.data.model.Promotion
import fp.practices.ocularis_mobile.viewmodel.ContactViewModel
import kotlinx.coroutines.launch


/**
 * Encabezado de sección con indicador visual.
 * @param title texto del encabezado
 */
@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 20.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Muestra las galerías de promociones y curiosidades en el dashboard público.
 * @param navController controlador de navegación
 */
@Composable
fun DashboardGalleries(navController: NavController) {
    val dashboard = remember { Dashboard.default() }
    val promociones = dashboard.promotions
    val curiosidades = dashboard.curiosities

    var showDialog by remember { mutableStateOf<Promotion?>(null) }

    SectionHeader("Todo problema tiene solución")

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .heightIn(max = 400.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(promociones) { promo ->
            PromotionGridCard(
                promo = promo,
                onClick = { showDialog = promo }
            )
        }
    }

    SectionHeader("¿Sabías que?")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        items(curiosidades) { curioso ->
            AnimatedGalleryCard(curioso = curioso, onClick = { })
        }
    }
    if (showDialog != null) {
        val promo = showDialog!!
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text(promo.title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(promo.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(promo.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = null
                    navController.navigate("login")
                }) {
                    Text("Hazte con este plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

/**
 * Tarjeta de promoción con imagen y detalles.
 * @param promo datos de la promoción
 * @param onClick acción al pulsar
 */
@Composable
fun PromotionGridCard(promo: Promotion, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.02f else 1f,
        animationSpec = tween(durationMillis = 150), label = ""
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable {
                pressed = true
                onClick()
                pressed = false
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = promo.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 120f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    promo.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    promo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * Tarjeta animada de curiosidad oftalmológica.
 * @param curioso datos de la curiosidad
 * @param onClick acción al pulsar
 */
@Composable
fun AnimatedGalleryCard(curioso: Curiosity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = curioso.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    curioso.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    curioso.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5
                )
            }
        }
    }
}

/**
 * Pantalla principal pública del dashboard con navegación lateral, contacto y galerías.
 * @param navController controlador de navegación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val contactViewModel: ContactViewModel = viewModel()
    val isSending by contactViewModel.isSending.observeAsState(false)
    val sendError by contactViewModel.error.observeAsState(null)
    val sendMessage by contactViewModel.message.observeAsState(null)

    var showContact by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(290.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(14.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("¡Hola, Bienvenido!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("AUTENTICACIÓN", modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                NavigationDrawerItem(
                    label = { Text("Iniciar Sesión", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { navController.navigate("login"); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.AutoMirrored.Filled.Login, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                /*NavigationDrawerItem(
                    label = { Text("Registrarse", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { navController.navigate("registro_paciente"); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.AppRegistration, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                 */
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text("INFORMACIÓN", modifier = Modifier.padding(start = 20.dp, bottom = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                NavigationDrawerItem(
                    label = { Text("Sobre Nosotros", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { showInfo = true; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Info, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("OCULARIS", fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú de navegación")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showContact = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Contacto Clínica")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Descubre nuestros servicios, somos especilistas en restaurar y cuidar tu salud visual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
                )
                DashboardGalleries(navController)
            }
        }

        if (showInfo) {
            AlertDialog(
                onDismissRequest = { showInfo = false },
                modifier = Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(16.dp)),
                title = {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("About Us", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Juan Eustaquio Nazario, oftalmólogo y fundador de Ocularis, emprendió este proyecto enfocado en soluciones oftalmológicas y la atención cercana al paciente.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Visión", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Ser un referente de confianza en salud visual para toda la comunidad.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Misión", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Proveer atención oftalmológica segura, accesible y basada en tecnología moderna.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Contacto", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Tel: +34 600 123 456", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text("info@ocularis.clinic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                },
                confirmButton = {
                    Button(onClick = { showInfo = false }) { Text("Cerrar") }
                }
            )
        }

        if (showContact) {
            var nombre by remember { mutableStateOf("") }
            var apellido by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var telefono by remember { mutableStateOf("") }
            var mensaje by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showContact = false },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                title = {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Contacto", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "¿Tienes dudas o quieres más información? Completa el formulario y nuestro equipo te contactará a la brevedad.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = apellido,
                            onValueChange = { apellido = it },
                            label = { Text("Apellido") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mensaje,
                            onValueChange = { mensaje = it },
                            label = { Text("Mensaje") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            contactViewModel.sendMessage(
                                ContactMessageDTO(
                                    nombre = nombre,
                                    apellido = apellido,
                                    email = email,
                                    telefono = telefono,
                                    mensaje = mensaje,
                                    revisado = false
                                )
                            )
                            showContact = false
                        },
                        enabled = !isSending &&
                                nombre.isNotBlank() &&
                                apellido.isNotBlank() &&
                                email.isNotBlank() &&
                                telefono.isNotBlank() &&
                                mensaje.isNotBlank()
                    ) {
                        Text(if (isSending) "Enviando..." else "Enviar mensaje")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showContact = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Éxito en Envío
        if (sendMessage != null || showSuccess) {
            AlertDialog(
                onDismissRequest = {
                    showSuccess = false
                    contactViewModel.clearMessage()
                },
                title = { Text("¡Mensaje enviado!", fontWeight = FontWeight.Bold) },
                text = { Text("Tu mensaje ha sido enviado correctamente. Nos pondremos en contacto contigo lo antes posible.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccess = false
                            contactViewModel.clearMessage()
                        }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
        }

        //Error en Envío
        if (sendError != null) {
            AlertDialog(
                onDismissRequest = { contactViewModel.clearError() },
                title = { Text("No se pudo enviar", fontWeight = FontWeight.Bold) },
                text = { Text(sendError ?: "Error al enviar el mensaje") },
                confirmButton = {
                    Button(onClick = { contactViewModel.clearError() }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}