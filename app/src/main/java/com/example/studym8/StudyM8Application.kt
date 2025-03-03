package com.example.studym8

import android.app.Application
import com.google.firebase.FirebaseApp

class StudyM8Application : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase aquí
        FirebaseApp.initializeApp(this)
    }
}