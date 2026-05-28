package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Book
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import fp.practices.ocularis_mobile.data.model.ContactMessageDTO
import fp.practices.ocularis_mobile.viewmodel.ContactViewModel


@Composable
fun DashboardGalleries(navController: NavController) {
    // Promociones con título, descripción, imagen y detalles económicos
    data class Promocion(
        val titulo: String,
        val descripcion: String,
        val imgUrl: String,
        val detalles: String
    )
    val promociones = listOf(
        Promocion(
            "Miopía",
            "Soluciones quirúrgicas avanzadas para corregir la visión de lejos, incluyendo LASIK y PRK en Clínica Ocularis.",
            "https://images.pexels.com/photos/3845128/pexels-photo-3845128.jpeg?auto=compress&w=400&q=80", // Cirujano oftalmológico en quirófano
            "Desde 950€ por ojo. Consulta gratuita. Financiación disponible."
        ),
        Promocion(
            "Ojo Seco",
            "Tratamientos de última generación y terapias especializadas.",
            "https://images.pexels.com/photos/3845734/pexels-photo-3845734.jpeg?auto=compress&w=400&q=80", // Consulta oftalmológica con lámpara de hendidura
            "Consulta inicial 40€. Pack tratamiento desde 120€."
        ),
        Promocion(
            "Presbicia",
            "Mejora tu visión de cerca con nuestros lentes progresivos de última generación y opciones de lentes de contacto premium.",
            "https://images.pexels.com/photos/3845761/pexels-photo-3845761.jpeg?auto=compress&w=400&q=80", // Paciente usando lentes en consulta
            "Lentes desde 180€. Consulta gratuita."
        ),
        Promocion(
            "Cataratas",
            "Recuperación visual con cirugía de facoemulsificación de precisión e implante de lentes intraoculares multifocales.",
            "https://images.pexels.com/photos/3845802/pexels-photo-3845802.jpeg?auto=compress&w=400&q=80", // Cirugía de cataratas
            "Cirugía desde 1.200€ por ojo. Financiación disponible."
        )
    )
    val curiosidades = listOf(
        Pair(
            "Las lágrimas no solo lubrican, también contienen enzimas que protegen contra infecciones y nutren la córnea.",
            "https://images.pexels.com/photos/3845820/pexels-photo-3845820.jpeg?auto=compress&w=400&q=80" // Detalle de ojo humano en consulta
        ),
        Pair(
            "El músculo del párpado es el más rápido del cuerpo: el parpadeo es tan rápido que dura solo 1/10 de segundo.",
            "https://images.pexels.com/photos/3845734/pexels-photo-3845734.jpeg?auto=compress&w=400&q=80" // Consulta oftalmológica con lámpara de hendidura
        ),
        Pair(
            "El ojo humano puede distinguir alrededor de 10 millones de colores diferentes.",
            "https://images.pexels.com/photos/3845761/pexels-photo-3845761.jpeg?auto=compress&w=400&q=80" // Paciente usando lentes en consulta
        ),
        Pair(
            "La córnea es el único tejido del cuerpo humano que no contiene vasos sanguíneos.",
            "https://images.pexels.com/photos/3845802/pexels-photo-3845802.jpeg?auto=compress&w=400&q=80" // Cirugía de cataratas
        ),
        Pair(
            "Los ojos parpadean aproximadamente 15-20 veces por minuto, lo que ayuda a mantenerlos limpios y húmedos.",
            "https://images.pexels.com/photos/3845128/pexels-photo-3845128.jpeg?auto=compress&w=400&q=80" // Cirujano oftalmológico en quirófano
        ),
        Pair(
            "El ojo humano puede enfocar en tan solo 2 milisegundos, más rápido que una cámara profesional.",
            "https://images.pexels.com/photos/3845850/pexels-photo-3845850.jpeg?auto=compress&w=400&q=80" // Equipo médico oftalmológico
        ),
        Pair(
            "Las lágrimas emocionales contienen hormonas y proteínas diferentes a las lágrimas basales.",
            "https://images.pexels.com/photos/1130626/pexels-photo-1130626.jpeg?auto=compress&w=400&q=80" // Macro de ojo humano
        )
    )
    var showDialog by remember { mutableStateOf<Promocion?>(null) }
    Text("Solucionamos tus problemas visuales ", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp, end = 0.dp))
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .heightIn(max = 400.dp)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            items(promociones) { promo ->
                PromotionGridCard(
                    titulo = promo.titulo,
                    descripcion = promo.descripcion,
                    imgUrl = promo.imgUrl,
                    onClick = { showDialog = promo }
                )
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text("¿Sabias que?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp, end = 0.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(curiosidades) { (texto, imgUrl) ->
            AnimatedGalleryCard(texto, imgUrl, onClick = { })
        }
    }
    if (showDialog != null) {
        val promo = showDialog!!
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text(promo.titulo) },
            text = {
                Column {
                    Text(promo.descripcion, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(promo.detalles, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
                Button(onClick = { showDialog = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun PromotionGridCard(titulo: String, descripcion: String, imgUrl: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.03f else 1f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                onClick = {
                    pressed = true
                    onClick()
                },
                onClickLabel = "Ver detalle"
            ),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = if (pressed) 10.dp else 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imgUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1
                )
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun AnimatedGalleryCard(texto: String, imgUrl: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.04f else 1f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(120.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                onClick = {
                    pressed = true
                    onClick()
                },
                onClickLabel = "Ver detalle"
            ),
        shape = RoundedCornerShape(18.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = if (pressed) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imgUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
        }
    }
}

@Composable
fun InfoCardSobreOcularis(onMoreInfo: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con gradiente sutil
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Sobre Ocularis",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("SOBRE OCULARIS", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Líderes en tratamientos oftalmologícos, Clínica Ocularis se dedica a proporcionar cuidado visual excepcional utilizando la tecnología de vanguardia y un equipo de especialistas dedicados. Nuestro compromiso es su visión, brindando atención personalizada para toda su familia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        maxLines = 3
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onMoreInfo, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                    Text("Más Información", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

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
    var showFounderInfo by remember { mutableStateOf(false) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                NavigationDrawerItem(
                    label = { Text("Login") },
                    selected = false,
                    onClick = {
                        navController.navigate("login")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Login") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("Registrarse") },
                    selected = false,
                    onClick = {
                        navController.navigate("registro_paciente")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Registro de paciente") }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú de login")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = "Bienvenido a Ocularis",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Descubre soluciones y curiosidades sobre salud visual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                InfoCardSobreOcularis(onMoreInfo = { showFounderInfo = true })
                DashboardGalleries(navController)
            }
            // FAB de contacto
            FloatingActionButton(
                onClick = { showContact = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Contacto Clínica")
            }
            if (showFounderInfo) {
                AlertDialog(
                    onDismissRequest = { showFounderInfo = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    title = {
                        Column {
                            Text("About Us", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Enmanuel Lledo, oftalmólogo y fundador de Ocularis, emprendio este proyecto enfocado en soluciones oftalmologicas y la atención cercana al paciente.",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 5
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Visión", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text("Ser un referente de confianza en salud visual para toda la comunidad.", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Misión", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text("Proveer atención oftalmológica segura, accesible y basada en tecnología moderna.", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Contacto", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text("Tel: +34 600 123 456", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            Text("info@ocularis.clinic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showFounderInfo = false }) {
                            Text("Cerrar")
                        }
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
                    title = { Text("Contacto Clínica") },
                    text = {
                        Column {
                            Text("¿Tienes dudas o quieres más información? Completa el formulario y nuestro equipo te contactará a la brevedad.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre") },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = apellido,
                                onValueChange = { apellido = it },
                                label = { Text("Apellido") },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo electrónico") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = telefono,
                                onValueChange = { telefono = it },
                                label = { Text("Teléfono") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = mensaje,
                                onValueChange = { mensaje = it },
                                label = { Text("Mensaje") },
                                minLines = 2,
                                maxLines = 4
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
                        Button(onClick = { showContact = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            if (sendMessage != null || showSuccess) {
                AlertDialog(
                    onDismissRequest = {
                        showSuccess = false
                        contactViewModel.clearMessage()
                    },
                    title = { Text("¡Mensaje enviado!") },
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
            if (sendError != null) {
                AlertDialog(
                    onDismissRequest = { contactViewModel.clearError() },
                    title = { Text("No se pudo enviar") },
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
}
