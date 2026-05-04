package fp.practices.ocularis_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fp.practices.ocularis_mobile.ui.theme.DarkBackground
import fp.practices.ocularis_mobile.ui.theme.LightText
import fp.practices.ocularis_mobile.ui.theme.MediumText
import fp.practices.ocularis_mobile.ui.theme.PrimaryBlue
import fp.practices.ocularis_mobile.ui.theme.VibrantBlue

@Composable
fun LoginScreen(
    isLoading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(username, password) {
        if (!error.isNullOrBlank()) {
            onClearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "👁", fontSize = 56.sp, modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "Bienvenido a Ocularis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = LightText, fontSize = 24.sp)
        Text(text = "Acceso al sistema", style = MaterialTheme.typography.bodyMedium, color = MediumText, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedLabelColor = VibrantBlue, unfocusedLabelColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText, cursorColor = VibrantBlue), shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Text(if (passwordVisible) "👁" else "👁‍🗨", modifier = Modifier.padding(4.dp)) } }, singleLine = true, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VibrantBlue, unfocusedBorderColor = MediumText, focusedLabelColor = VibrantBlue, unfocusedLabelColor = MediumText, focusedTextColor = LightText, unfocusedTextColor = LightText, cursorColor = VibrantBlue), shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(24.dp))

        if (!error.isNullOrBlank()) { Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.bodySmall); Spacer(modifier = Modifier.height(8.dp)) }

        Button(onClick = { onLogin(username, password) }, enabled = !isLoading && username.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = LightText, disabledContainerColor = MediumText.copy(alpha = 0.5f), disabledContentColor = LightText.copy(alpha = 0.5f))) { if (isLoading) CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp, color = LightText) else Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "¿Necesitas ayuda?", color = VibrantBlue, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
    }
}

