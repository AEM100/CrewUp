package anuar.morabet.crewupnow.editProfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import anuar.morabet.crewupnow.paneleUsuario.base64ToBitmap
import anuar.morabet.crewupnow.paneleUsuario.uriToBase64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onAction: (EditProfileDataAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Launcher para abrir la galería de fotos
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64 = uriToBase64(context, it)
            onAction(EditProfileDataAction.OnAvatarSelected(base64))
        }
    }

    LaunchedEffect(state.isSaveSuccess) {
        if (state.isSaveSuccess) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // RANURA DE LA FOTO
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Si hay foto en el state, la mostramos, sino el icono
                val bitmap = base64ToBitmap(state.fotoBase64)
                if (bitmap != null) {
                    Image(bitmap, "Foto perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(55.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Face, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // TEXTFIELDS
            OutlinedTextField(state.name, { onAction(EditProfileDataAction.OnNameChanged(it)) }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.bio, { onAction(EditProfileDataAction.OnBioChanged(it)) }, label = { Text("Biografía") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.email, { onAction(EditProfileDataAction.OnEmailChanged(it)) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.password, { onAction(EditProfileDataAction.OnPasswordChanged(it)) }, label = { Text("Nueva Contraseña") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAction(EditProfileDataAction.OnSaveClicked) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        }
    }
}