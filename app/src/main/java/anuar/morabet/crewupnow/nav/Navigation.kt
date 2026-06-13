package anuar.morabet.crewupnow.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import anuar.morabet.crewupnow.EventPanel.UpcomingEventsScreen
import anuar.morabet.crewupnow.EventPanel.UpcomingEventsViewModel
import anuar.morabet.crewupnow.auth.AuthController
import anuar.morabet.crewupnow.auth.AuthUiState
import anuar.morabet.crewupnow.auth.AuthViewModel
import anuar.morabet.crewupnow.chat.ChatViewModel
import anuar.morabet.crewupnow.chat.ChatWindow
import anuar.morabet.crewupnow.data.repository.EventRepository
import anuar.morabet.crewupnow.editProfile.EditProfileScreen
import anuar.morabet.crewupnow.editProfile.EditProfileViewModel
import anuar.morabet.crewupnow.mapa.ui.MapScreen
import anuar.morabet.crewupnow.mapa.MapViewModel
import anuar.morabet.crewupnow.paneleUsuario.ProfileAction
import anuar.morabet.crewupnow.paneleUsuario.UserProfileScreen
import anuar.morabet.crewupnow.paneleUsuario.ProfileViewModel
import anuar.morabet.crewupnow.paneleUsuario.SessionManager
import anuar.morabet.crewupnow.setting.SettingsScreen
import anuar.morabet.crewupnow.setting.SettingsViewModel
import kotlinx.coroutines.launch


@Composable
fun Navigation(
    navController: NavHostController
) {
    val coroutineScope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel()
            val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

            AuthController(
                uiState = uiState,
                onAction = authViewModel::onAction,
                navigateToHome = {
                    if (uiState is AuthUiState.Authenticated) {
                        val loggedUser = (uiState as AuthUiState.Authenticated).user
                        SessionManager.currentUser = loggedUser
                        coroutineScope.launch {
                            EventRepository.cargarEventosDelServidor()
                        }
                    }

                    navController.navigate(Screen.App.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.App.route) {
            AppScreen(rootNavController = navController)
        }
    }
}

@Composable
fun AppScreen(rootNavController: NavHostController) {

    val navController = rememberNavController()

    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    Scaffold(
        bottomBar = {
            // 🔥 CORREGIDO: Oculta la barra inferior si estás editando el perfil o en un chat
            val rutasSinBarra = listOf(Screen.EditProfile.route, "chat_screen/{chatId}/{chatTitle}")

            if (currentRoute !in rutasSinBarra) {
                BottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(Screen.Home.route) {
                val viewModel: MapViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                MapScreen(state = state, onAction = viewModel::onAction)
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel()
                val state by profileViewModel.uiState.collectAsStateWithLifecycle() // Actualizado por consistencia

                UserProfileScreen(
                    state = state,
                    onAction = { action ->
                        when (action) {
                            ProfileAction.EditProfile -> {
                                navController.navigate(Screen.EditProfile.route)
                            }
                            is ProfileAction.OnChatClicked -> {
                                navController.navigate("chat_screen/${action.chatId}/${action.chatTitle}")
                            }
                            else -> {
                                profileViewModel.onAction(action)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    onDismissDialog = {
                        profileViewModel.onDismissDetails()
                    }
                )
            }

            composable(route = "chat_screen/{chatId}/{chatTitle}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val chatTitle = backStackEntry.arguments?.getString("chatTitle") ?: ""

                val chatViewModel: ChatViewModel = viewModel()
                val chatState by chatViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(chatId) {
                    chatViewModel.initChat(chatId)
                }

                ChatWindow(
                    chatTitle = chatTitle,
                    state = chatState,
                    onAction = { chatAction ->
                        chatViewModel.onAction(chatAction) {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Screen.Events.route) {
                val upcomingViewModel: UpcomingEventsViewModel = viewModel()
                val state by upcomingViewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                UpcomingEventsScreen(
                    state = state,
                    onRetryClick = { upcomingViewModel.obtenerProximosEventos() },
                    onCardAppear = { evento ->
                        upcomingViewModel.buscarDireccionPostal(
                            context = context,
                            eventId = evento.id,
                            lat = evento.lat,
                            lng = evento.lng
                        )
                    }
                )
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel {
                    SettingsViewModel(
                        onLogoutSuccess = {
                            rootNavController.navigate(Screen.Login.route) {
                                popUpTo(Screen.App.route) { inclusive = true }
                            }
                        }
                    )
                }
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(state = state, onAction = viewModel::onAction)
            }

            // 📍 CORREGIDO: Ahora coincide exactamente con Screen.EditProfile.route ("edit_profile")
            composable(Screen.EditProfile.route) {
                val editViewModel: EditProfileViewModel = viewModel()
                val editState by editViewModel.uiState.collectAsStateWithLifecycle()

                EditProfileScreen(
                    state = editState,
                    onAction = editViewModel::onAction,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Mapa") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Events.route,
            onClick = { navController.navigate(Screen.Events.route) },
            icon = { Icon(Icons.Default.Menu, contentDescription = null) },
            label = { Text("Eventos") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { navController.navigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Perfil") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = { navController.navigate(Screen.Settings.route) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Config") }
        )
    }
}