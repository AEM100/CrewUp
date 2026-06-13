package anuar.morabet.crewupnow.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SocketClient {
    private var socket: Socket? = null
    private val host = "192.168.1.48" // Tu IP fija
    private val port = 9000

    // Modificado para que sea suspendido y reutilizable
    suspend fun sendRequest(jsonPayload: String): String? = withContext(Dispatchers.IO) {
        try {
            // Abrimos conexión en caliente para transacciones rápidas (Login, Registro, Crear Evento)
            val currentSocket = Socket(host, port)
            val out = PrintWriter(currentSocket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(currentSocket.getInputStream()))

            // Enviamos el JSON
            out.println(jsonPayload)

            // Leemos la respuesta del servidor
            val response = reader.readLine()

            // Cerramos
            currentSocket.close()

            response
        } catch (e: Exception) {
            Log.e("SocketClient", "Error de red: ${e.message}")
            null
        }
    }
}