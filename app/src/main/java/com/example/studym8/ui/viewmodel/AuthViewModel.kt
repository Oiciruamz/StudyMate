package com.example.studym8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studym8.data.model.User
import com.example.studym8.data.repository.UserRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    
    private val repository = UserRepository()
    private val auth: FirebaseAuth = Firebase.auth
    
    // StateFlow para el usuario actual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    // StateFlow para controlar el estado de autenticación
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    
    // StateFlow para errores
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError
    
    init {
        // Comprobar si hay un usuario autenticado
        checkCurrentUser()
    }
    
    fun checkCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Verificar si hay un usuario autenticado en Firebase
                val firebaseUser = auth.currentUser
                
                if (firebaseUser != null) {
                    // Obtener datos completos del usuario desde el repositorio
                    val user = repository.getCurrentUser()
                    
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.SignedIn
                    } else {
                        _authState.value = AuthState.SignedOut
                    }
                } else {
                    _authState.value = AuthState.SignedOut
                }
            } catch (e: Exception) {
                _authError.value = "Error al verificar el usuario actual: ${e.message}"
                _authState.value = AuthState.Error
            }
        }
    }
    
    fun signInWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authError.value = null
            
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                
                // Actualizar el timestamp de último login
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    repository.updateLastLogin(currentUserId)
                }
                
                // Obtener los datos completos del usuario
                val user = repository.getCurrentUser()
                
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.SignedIn
                } else {
                    _authError.value = "No se pudieron obtener los datos del usuario"
                    _authState.value = AuthState.Error
                }
            } catch (e: Exception) {
                _authError.value = "Error de inicio de sesión: ${e.message}"
                _authState.value = AuthState.Error
            }
        }
    }
    
    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authError.value = null
            
            try {
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // Actualizar el timestamp de último login
                    repository.updateLastLogin(firebaseUser.uid)
                    
                    // Obtener los datos completos del usuario
                    val user = repository.getCurrentUser()
                    
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.SignedIn
                    } else {
                        _authError.value = "No se pudieron obtener los datos del usuario"
                        _authState.value = AuthState.Error
                    }
                } else {
                    _authError.value = "No se pudo obtener el usuario de Firebase"
                    _authState.value = AuthState.Error
                }
            } catch (e: Exception) {
                _authError.value = "Error de inicio de sesión con Google: ${e.message}"
                _authState.value = AuthState.Error
            }
        }
    }
    
    fun createUserWithEmailPassword(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authError.value = null
            
            try {
                // Crear usuario en Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    // Actualizar el perfil con el nombre
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    firebaseUser.updateProfile(profileUpdates).await()
                    
                    // Los datos del usuario se crearán automáticamente en getCurrentUser()
                    val user = repository.getCurrentUser()
                    
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.SignedIn
                    } else {
                        _authError.value = "No se pudieron obtener los datos del usuario"
                        _authState.value = AuthState.Error
                    }
                } else {
                    _authError.value = "No se pudo crear el usuario"
                    _authState.value = AuthState.Error
                }
            } catch (e: Exception) {
                _authError.value = "Error al crear usuario: ${e.message}"
                _authState.value = AuthState.Error
            }
        }
    }
    
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.SignedOut
    }
    
    fun resetAuthState() {
        _authError.value = null
        if (auth.currentUser != null) {
            _authState.value = AuthState.SignedIn
        } else {
            _authState.value = AuthState.SignedOut
        }
    }
}

// Estados de autenticación
enum class AuthState {
    Initial,    // Estado inicial
    Loading,    // Cargando
    SignedIn,   // Autenticado
    SignedOut,  // No autenticado
    Error       // Error en la autenticación
} 