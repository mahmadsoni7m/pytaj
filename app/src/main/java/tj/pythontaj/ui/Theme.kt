package tj.pythontaj.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Рангҳои барнома. Барои тағйири намуди зоҳирӣ ин ҷоро таҳрир кунед. */
object Palette {
    val Accent = Color(0xFF2DD4BF)
    val Background = Color(0xFF121416)
    val Card = Color(0xFF1C2024)
    val CardHigh = Color(0xFF262B30)
    val Code = Color(0xFF0D0F10)
    val CodeText = Color(0xFFCDE7E2)
    val Error = Color(0xFFFF7B7B)
    val Success = Color(0xFF4ADE80)
}

private val Dark = darkColorScheme(
    primary = Palette.Accent,
    onPrimary = Color(0xFF00201C),
    secondary = Palette.Success,
    background = Palette.Background,
    onBackground = Color(0xFFE6E8EA),
    surface = Palette.Background,
    onSurface = Color(0xFFE6E8EA),
    surfaceVariant = Palette.Card,
    onSurfaceVariant = Color(0xFFB8C0C6),
    error = Palette.Error,
)

@Composable
fun PythonTajTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = Dark, content = content)
