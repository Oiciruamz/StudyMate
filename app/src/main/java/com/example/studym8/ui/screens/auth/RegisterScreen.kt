package com.example.studym8

import android.content.Intent
import android.widget.Toast
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    navigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth = remember { Firebase.auth }

    // Configurar el cliente de Google Sign-In
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // IMPORTANTE: Usa este método
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
            errorMessage = "Registro con Google cancelado o fallido"
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && account.idToken != null) {
                firebaseAuthWithGoogle(account.idToken!!, auth, onSuccess = {
                    isLoading = false
                    onRegisterSuccess()
                }, onError = { exception ->
                    isLoading = false
                    errorMessage = "Error al registrar con Google: ${exception.localizedMessage}"
                    Log.e("GoogleSignIn", "Error al autenticar con Firebase", exception)
                })
            } else {
                isLoading = false
                errorMessage = "No se pudo obtener la información de la cuenta de Google"
            }
        } catch (e: ApiException) {
            isLoading = false
            // Log detallado para ayudar en la depuración
            Log.e("GoogleSignIn", "Error de Google Sign In, código: ${e.statusCode}", e)
            errorMessage = "Error al registrar con Google (${e.statusCode}): ${e.localizedMessage}"
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

        // Texto de "Regístrate para continuar"
        Text(
            "Regístrate para continuar",
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
            Text("Regístrate con Google", color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cuadro de nombre
        Text("Nombre*", fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(text = "Nombre") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.Start),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading
        )

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

        Spacer(modifier = Modifier.height(24.dp))

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

        // Botón de regístrate
        Button(
            onClick = {
                isLoading = true
                registerWithEmailPassword(
                    email = email,
                    password = password,
                    nombre = nombre,
                    auth = auth,
                    onSuccess = {
                        isLoading = false
                        onRegisterSuccess()
                    },
                    onError = { exception ->
                        isLoading = false
                        errorMessage = exception.localizedMessage
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1338BE)),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && nombre.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Regístrate", color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "¿Ya tienes una cuenta? Inicia sesión aquí"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("¿Ya tienes una cuenta? ")
            Text(
                "Inicia sesión aquí",
                color = Color(0xFF1338BE),
                modifier = Modifier.clickable(enabled = !isLoading) {
                    navigateToLogin()
                }
            )
        }
    }
}

// Función para registrar con email y contraseña
private fun registerWithEmailPassword(
    email: String,
    password: String,
    nombre: String,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Guardar el nombre de usuario en el perfil de Firebase
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre)
                    .build()

                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            // Guarda información adicional en Firestore
                            saveUserDataToFirestore(
                                userId = user.uid,
                                displayName = nombre,
                                email = email,
                                registrationMethod = "email"
                            )
                            onSuccess()
                        } else {
                            onError(profileTask.exception ?: Exception("Error al actualizar el perfil"))
                        }
                    }
            } else {
                onError(task.exception ?: Exception("Error al registrar usuario"))
            }
        }
}

// Función para registrar con Google
private fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)

    // Primero verificamos si es un nuevo usuario o ya existente
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                if (isNewUser) {
                    // Es un nuevo registro - podemos guardar datos adicionales en Firestore
                    saveUserDataToFirestore(user?.uid, user?.displayName, user?.email)
                    onSuccess()
                } else {
                    // El usuario ya existía
                    onSuccess()
                }
            } else {
                onError(task.exception ?: Exception("Error al registrar con Google"))
            }
        }
}

// Función para guardar datos del usuario en Firestore después del registro
private fun saveUserDataToFirestore(
    userId: String?,
    displayName: String?,
    email: String?,
    registrationMethod: String = "google"
) {
    if (userId == null) return

    // Esta es una implementación básica - puedes expandirla según tus necesidades
    val db = Firebase.firestore
    val userData = hashMapOf(
        "displayName" to (displayName ?: ""),
        "email" to (email ?: ""),
        "registrationMethod" to registrationMethod,
        "registrationDate" to FieldValue.serverTimestamp()
    )

    db.collection("users")
        .document(userId)
        .set(userData)
        .addOnSuccessListener {
            Log.d("RegisterScreen", "Datos de usuario guardados correctamente")
        }
        .addOnFailureListener { e ->
            Log.e("RegisterScreen", "Error al guardar datos de usuario", e)
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(
        onRegisterSuccess = {},
        navigateToLogin = {}
    )
}

