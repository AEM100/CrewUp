package anuar.morabet.crewupnow.network

import android.content.Context
import anuar.morabet.crewupnow.auth.AuthRepository

object Locator {
    // Inicialmente nulos
    lateinit var socketClient: SocketClient
    lateinit var authRepository: AuthRepository

    // Llamas a esto en tu Application class o MainActivity al iniciar
    fun init(context: Context) {
        socketClient = SocketClient(context.applicationContext)
        authRepository = AuthRepository(socketClient)
    }
}