package anuar.morabet.crewupnow.network

import android.util.Log
import java.net.Socket

class SocketClient {

    private var socket: Socket? = null

    fun connect(
        host: String,
        port: Int
    ): Boolean {

        return try {

            socket = Socket(host, port)
            Log.d("ffffffffff", "conectado")

            true

        } catch (e: Exception) {

            e.printStackTrace()
            Log.d("ffffffffff", "no hay conexion")
            Log.d("ffffffffff", e.toString())


            false
        }
    }

    fun disconnect() {

        socket?.close()
        socket = null
    }
}