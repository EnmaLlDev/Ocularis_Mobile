package fp.practices.ocularis_mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val OcularisDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGreen,
    tertiary = TertiaryOrange,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LightText,
    onSecondary = LightText,
    onTertiary = LightText,
    onBackground = LightText,
    onSurface = LightText,
    error = Color(0xFFFF6B6B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Ocularis_MobileTheme(
    darkTheme: Boolean = true, // Forzamos tema oscuro
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Desactivamos colores dinámicos para mantener consistencia
    content: @Composable () -> Unit
) {
    val colorScheme = OcularisDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}