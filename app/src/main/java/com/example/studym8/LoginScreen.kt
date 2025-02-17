package com.example.studym8

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TryLogin() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp), // Más espacio alrededor
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
            onClick = { /* Acción de Google */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            val googleLogo: Painter = painterResource(id = R.drawable.google)
            Image(painter = googleLogo, contentDescription = "Logo de Google", modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Iniciar sesión con Google", color = Color.Black)
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
            shape = MaterialTheme.shapes.medium
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
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // "Recuérdame" y "Olvidé mi contraseña"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text("Recuérdame", fontSize = 15.sp)
            }

            TextButton(onClick = { /* Acción de olvidar contraseña */ }) {
                Text("Olvidé mi contraseña",
                    color = Color.Blue,
                    fontSize = 15.sp,
                    style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de iniciar sesión
        Button(
            onClick = { /* Acción de iniciar sesión */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1338BE)),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Iniciar sesión", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "Primera vez? Regístrate"
        ClickableText(
            text = AnnotatedString("¿Primera vez? Regístrate"),
            onClick = { /* Acción de registro */ },
            modifier = Modifier.padding(8.dp),
            style = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Blue, textDecoration = TextDecoration.Underline)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTryLogin() {
    TryLogin()
}
