package anuar.morabet.crewupnow.paneleUsuario

import anuar.morabet.crewupnow.auth.UserSession
import anuar.morabet.crewupnow.data.repository.ChatRepository
import anuar.morabet.crewupnow.data.repository.EventRepository


object SessionManager {
    // Aquí guardaremos los datos reales en memoria una vez el usuario pase el Login/Registro
    var currentUser: UserSession? = null

    fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }

    fun logout() {
        currentUser = null
        EventRepository.clear() // 🔥 Limpiamos eventos
        ChatRepository.clear()
    }
}