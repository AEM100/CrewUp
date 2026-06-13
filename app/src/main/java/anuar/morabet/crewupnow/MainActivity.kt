package anuar.morabet.crewupnow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import anuar.morabet.crewupnow.nav.Navigation
import anuar.morabet.crewupnow.themes.ThemeManager
import com.google.android.gms.maps.MapsInitializer


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Sigues necesitando escuchar tu Switch
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            // 2. Usas las paletas por defecto de Google (¡sin configurar colores a mano!)
            val esquemaColores = if (isDarkMode) {
                darkColorScheme() // 🔥 Usa el tema oscuro estándar de Google
            } else {
                lightColorScheme() // ☀️ Usa el tema claro estándar de Google
            }

            // 3. Se lo inyectas al MaterialTheme
            MaterialTheme(
                colorScheme = esquemaColores
            ) {

                val navController = rememberNavController()
                Navigation(navController)

            }
        }
    }
}