package anuar.morabet.crewupnow.auth

import android.util.Log
import anuar.morabet.crewupnow.network.SocketClient
import org.json.JSONObject

data class UserSession(
    val id: Int,
    val name: String,
    val email: String,
    val token: String,
    val bio: String = "",
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false
)

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}

class AuthRepository(private val socketClient: SocketClient) {

    private val TAG = "DEBUG_CREWUP"

    suspend fun registerUser(name: String, email: String, password: String): Resource<UserSession> {
        return try {
            val requestJson = JSONObject().apply {
                put("action", "REGISTER")
                put("name", name)
                put("email", email)
                put("password", password)
            }

            Log.d(TAG, "Registro - Enviando: $requestJson")
            val responseString = socketClient.sendRequest(requestJson.toString())
                ?: return Resource.Error("No se pudo conectar con el servidor.")

            val responseJson = JSONObject(responseString)
            Log.d(TAG, "Registro - Recibido JSON: $responseJson")

            if (responseJson.getString("status") == "SUCCESS") {
                val user = parseUser(responseJson)
                Resource.Success(user)
            } else {
                Resource.Error(responseJson.optString("message", "Error en el registro."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro: ${e.message}")
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun loginUser(email: String, password: String): Resource<UserSession> {
        return try {
            val requestJson = JSONObject().apply {
                put("action", "LOGIN")
                put("email", email)
                put("password", password)
            }

            Log.d(TAG, "Login - Enviando: $requestJson")
            val responseString = socketClient.sendRequest(requestJson.toString())
                ?: return Resource.Error("No se pudo conectar con el servidor.")

            val responseJson = JSONObject(responseString)
            Log.d(TAG, "Login - Recibido JSON: $responseJson")

            if (responseJson.getString("status") == "SUCCESS") {
                val isBanned = responseJson.optBoolean("isBanned", false) ||
                        responseJson.optString("status_cuenta") == "BANNED"

                if (isBanned) {
                    Log.w(TAG, "Login - Usuario baneado detectado")
                    Resource.Error("Tu cuenta ha sido suspendida.")
                } else {
                    val user = parseUser(responseJson)
                    Log.d(TAG, "Login - Usuario parseado correctamente: $user")
                    Resource.Success(user)
                }
            } else {
                Log.w(TAG, "Login - Fallido: ${responseJson.optString("message")}")
                Resource.Error(responseJson.optString("message", "Credenciales incorrectas."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}")
            Resource.Error("Error en el login: ${e.localizedMessage}")
        }
    }

    private fun parseUser(json: JSONObject): UserSession {
        // Log para ver qué campos recibimos realmente del servidor
        Log.d(TAG, "parseUser - Intentando parsear JSON: $json")

        val adminFlag = json.optBoolean("isAdmin", false) ||
                json.optString("tipo_cuenta").uppercase() == "ADMIN" ||
                json.optString("role").uppercase() == "ADMIN"

        val user = UserSession(
            id = json.getInt("id"),
            name = json.getString("name"),
            email = json.getString("email"),
            token = json.optString("token", ""),
            bio = json.optString("bio", ""),
            isAdmin = adminFlag,
            isBanned = json.optBoolean("isBanned", false)
        )

        Log.d(TAG, "parseUser - Resultado final isAdmin: ${user.isAdmin}")
        return user
    }
}