package com.example.studym8.ui.screens.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studym8.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
            errorMessage = "Registro con Google cancelado o fallido"
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && account.idToken != null) {
                firebaseAuthWithGoogle(account.idToken!!, auth, onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
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
            Log.e("GoogleSignIn", "Error de Google Sign In, código: ${e.statusCode}", e)
            errorMessage = "Error al registrar con Google (${e.statusCode}): ${e.localizedMessage}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagen studym8
        Image(
            painter = painterResource(id = R.drawable.studymate),
            contentDescription = "Imagen de bienvenida",
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Texto de "Regístrate para continuar"
        Text(
            "Regístrate para continuar",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
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
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color.Gray),
            enabled = !isLoading
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Logo de Google",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Regístrate con Google",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cuadro de nombre
        Text(
            "Nombre*",
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono de Persona"
                )
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cuadro de email
        Text(
            "Email*",
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Icono de Email"
                )
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cuadro de contraseña
        Text(
            "Contraseña*",
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mostrar mensaje de error si existe
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de regístrate
        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                registerWithEmailPassword(
                    email = email,
                    password = password,
                    nombre = nombre,
                    auth = auth,
                    onSuccess = {
                        isLoading = false
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    },
                    onError = { exception ->
                        isLoading = false
                        errorMessage = exception.localizedMessage ?: "Error al registrar"
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && nombre.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Regístrate", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "¿Ya tienes una cuenta? Inicia sesión aquí"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "¿Ya tienes una cuenta? ",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Inicia sesión aquí",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
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

