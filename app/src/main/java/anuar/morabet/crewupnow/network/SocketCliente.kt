package anuar.morabet.crewupnow.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val context: Context) {
    private val port = 9000

    private fun getHost(): String {
        // Busca la IP guardada, si no hay, usa la de casa por defecto
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("server_ip", "192.168.1.48") ?: "192.168.1.48"
    }

    suspend fun sendRequest(jsonPayload: String): String? = withContext(Dispatchers.IO) {
        try {
            val currentSocket = Socket(getHost(), port)
            val out = PrintWriter(currentSocket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(currentSocket.getInputStream()))

            out.println(jsonPayload)
            val response = reader.readLine()
            currentSocket.close()
            response
        } catch (e: Exception) {
            Log.e("SocketClient", "Error de red: ${e.message}")
            null
        }
    }
}