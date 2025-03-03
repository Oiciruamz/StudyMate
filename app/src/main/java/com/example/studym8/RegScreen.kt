package com.example.studym8

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun TryReg() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

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
            shape = MaterialTheme.shapes.medium
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

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de regístrate
        Button(
            onClick = { /* Acción de registro */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1338BE)),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Regístrate", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "¿Ya tienes una cuenta? Inicia sesión aquí"
        ClickableText(
            text = AnnotatedString("¿Ya tienes una cuenta? Inicia sesión aquí"),
            onClick = { navController.navigate("login") },  // O la navegación que necesites
            modifier = Modifier.padding(8.dp),
            style = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Blue, textDecoration = TextDecoration.Underline)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTryReg() {
    TryReg()
}


@Preview(showBackground = true)
@Composable
fun PreviewTryLog() {
    TryLogin()
}
