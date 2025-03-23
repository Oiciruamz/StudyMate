package com.example.studym8

import android.content.Intent
import android.widget.Toast
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    navigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth = remember { Firebase.auth }

    // Configurar el cliente de Google Sign-In
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Resultado del inicio de sesión con Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true

        if (result.resultCode != Activity.RESULT_OK) {
            isLoading = false
            errorMessage = "Inicio de sesión con Google cancelado o fallido"
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && account.idToken != null) {
                firebaseAuthWithGoogle(account.idToken!!, auth, onSuccess = {
                    isLoading = false

                    // Mostrar toast de éxito
                    Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    // Navegar a la pantalla de inicio
                    onLoginSuccess()
                }, onError = { exception ->
                    isLoading = false
                    errorMessage = "Error al iniciar sesión con Google: ${exception.localizedMessage}"
                    Log.e("GoogleSignIn", "Error al autenticar con Firebase", exception)
                })
            } else {
                isLoading = false
                errorMessage = "No se pudo obtener la información de la cuenta de Google"
            }
        } catch (e: ApiException) {
            isLoading = false
            Log.e("GoogleSignIn", "Error de Google Sign In, código: ${e.statusCode}", e)
            errorMessage = "Error al iniciar sesión con Google (${e.statusCode}): ${e.localizedMessage}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagen studym8
        val image: Painter = painterResource(id = R.drawable.studymate)
        Image(painter = image, contentDescription = "Imagen de bienvenida", modifier = Modifier.size(180.dp))

        Spacer(modifier = Modifier.height(32.dp))

        // Texto de "Iniciar sesión para continuar"
        Text(
            "Iniciar sesión para continuar",
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Google
        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color.Gray),
            enabled = !isLoading
        ) {
            val googleLogo: Painter = painterResource(id = R.drawable.google)
            Image(painter = googleLogo, contentDescription = "Logo de Google", modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Iniciar sesión con Google", color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cuadro de email
        Text("Email*", fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.Start),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cuadro de contraseña
        Text("Contraseña*", fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.Start),
            shape = MaterialTheme.shapes.medium,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje de error si existe
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // "Recuérdame" y "Olvidé mi contraseña"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    enabled = !isLoading
                )
                Text("Recuérdame", fontSize = 15.sp)
            }

            TextButton(
                onClick = {
                    // Acción para recuperar contraseña
                    // Podrías implementar esto más adelante
                    Toast.makeText(context, "Función en desarrollo", Toast.LENGTH_SHORT).show()
                },
                enabled = !isLoading
            ) {
                Text("Olvidé mi contraseña",
                    color = Color(0xFF1338BE),
                    fontSize = 15.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de iniciar sesión
        Button(
            onClick = {
                isLoading = true
                errorMessage = null

                // Iniciar sesión con email y contraseña
                loginWithEmailPassword(
                    email = email,
                    password = password,
                    rememberMe = rememberMe,
                    auth = auth,
                    onSuccess = {
                        isLoading = false

                        // Mostrar toast de éxito
                        Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                        // Navegar a la pantalla de inicio
                        onLoginSuccess()
                    },
                    onError = { exception ->
                        isLoading = false
                        errorMessage = exception.localizedMessage ?: "Error al iniciar sesión"
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1338BE)),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Iniciar sesión", color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "Primera vez? Regístrate"
        ClickableText(
            text = AnnotatedString("¿Primera vez? Regístrate"),
            onClick = {
                if (!isLoading) {
                    navigateToRegister()
                }
            },
            modifier = Modifier.padding(8.dp),
            style = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF1338BE),
                textDecoration = TextDecoration.Underline
            )
        )
    }
}

// Función para iniciar sesión con email y contraseña
private fun loginWithEmailPassword(
    email: String,
    password: String,
    rememberMe: Boolean,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si se seleccionó "Recuérdame", podrías guardar esta preferencia
                // en SharedPreferences u otro mecanismo de almacenamiento

                // Inicio de sesión exitoso
                onSuccess()
            } else {
                // Error en inicio de sesión
                onError(task.exception ?: Exception("Error al iniciar sesión"))
            }
        }
}

// Función para iniciar sesión con Google
private fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)

    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Inicio de sesión exitoso
                onSuccess()
            } else {
                // Error en inicio de sesión
                onError(task.exception ?: Exception("Error al iniciar sesión con Google"))
            }
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        onLoginSuccess = {},
        navigateToRegister = {}
    )
}