package anuar.morabet.crewupnow.nav

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object App : Screen("app")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Events : Screen("events")
    data object Settings : Screen("settings")
    data object EditProfile : Screen("edit_profile")
}