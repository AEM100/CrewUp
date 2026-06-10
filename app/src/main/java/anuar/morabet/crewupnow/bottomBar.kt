package anuar.morabet.crewupnow

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import anuar.morabet.crewupnow.mapa.MapUiAction

@Composable
fun BottomBar4Tabs(
    selectedTab: Int,
    onAction: (MapUiAction) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onAction(MapUiAction.OnTabSelected(0)) },
            icon = { Text("🏠") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onAction(MapUiAction.OnTabSelected(1)) },
            icon = { Text("🗺️") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onAction(MapUiAction.OnTabSelected(2)) },
            icon = { Text("📅") },
            label = { Text("Eventos") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onAction(MapUiAction.OnTabSelected(3)) },
            icon = { Text("⚙️") },
            label = { Text("Ajustes") }
        )
    }
}