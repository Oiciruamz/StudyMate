package com.example.studym8.data.repository

import com.example.studym8.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null
        
        try {
            // Intentar obtener datos del usuario desde Firestore
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            
            return if (userDoc.exists()) {
                // Si el usuario existe en Firestore, devolver los datos
                val userData = userDoc.data ?: mapOf()
                User.fromMap(userData, currentUser.uid)
            } else {
                // Si no existe, crear un nuevo usuario con datos básicos
                val newUser = User(
                    id = currentUser.uid,
                    displayName = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    photoUrl = currentUser.photoUrl?.toString() ?: "",
                    createdAt = Timestamp.now(),
                    lastLoginAt = Timestamp.now()
                )
                // Guardar el nuevo usuario en Firestore
                usersCollection.document(currentUser.uid).set(newUser.toMap()).await()
                newUser
            }
        } catch (e: Exception) {
            // En caso de error, devolver un objeto User básico con la info de Auth
            return User(
                id = currentUser.uid,
                displayName = currentUser.displayName ?: "",
                email = currentUser.email ?: ""
            )
        }
    }

    suspend fun updateUserProfile(user: User): Boolean {
        return try {
            usersCollection.document(user.id)
                .update(mapOf(
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl,
                    "updatedAt" to Timestamp.now(),
                    "preferences" to user.preferences
                )).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateLastLogin(userId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("lastLoginAt", Timestamp.now())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
} 