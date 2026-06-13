package anuar.morabet.crewupnow.network

import anuar.morabet.crewupnow.auth.AuthRepository

object Locator {
    val socketClient = SocketClient()
    val authRepository = AuthRepository(socketClient)
    // Más adelante añadirás aquí el EventRepository o el ChatRepository
}