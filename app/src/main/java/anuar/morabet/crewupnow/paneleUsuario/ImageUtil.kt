package anuar.morabet.crewupnow.paneleUsuario

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

fun base64ToBitmap(base64Str: String?): ImageBitmap? {
    if (base64Str.isNullOrEmpty()) return null
    return try {
        // 1. Decodificar Base64 a bytes
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        // 2. Crear Bitmap de Android a partir de los bytes
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        // 3. Convertir a ImageBitmap de Compose
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
fun uriToBase64(context: Context, uri: Uri): String {
    return context.contentResolver.openInputStream(uri)?.use {
        Base64.encodeToString(it.readBytes(), Base64.DEFAULT)
    } ?: ""
}

