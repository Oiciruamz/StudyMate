package com.example.studym8.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.studym8.MainActivity
import com.example.studym8.R
import com.example.studym8.data.model.StudyPlan
import com.example.studym8.ui.screens.StudyPlanDetailScreen
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class NotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "study_plan_notifications"
    private val channelName = "Study Plan Notifications"
    private val channelDescription = "Notifications for study plan updates and reminders"
    
    // Mapa para controlar la frecuencia de notificaciones por plan de estudio
    private val notificationTimestamps = ConcurrentHashMap<String, Long>()
    // Tiempo mínimo entre notificaciones para el mismo plan (5 minutos)
    private val MIN_NOTIFICATION_INTERVAL = TimeUnit.MINUTES.toMillis(5)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showStudyPlanNotification(studyPlan: StudyPlan, type: NotificationType) {
        val studyPlanId = studyPlan.id
        val currentTime = System.currentTimeMillis()
        
        // Verificar si ya se mostró una notificación recientemente para este plan
        val lastNotificationTime = notificationTimestamps[studyPlanId] ?: 0L
        if (currentTime - lastNotificationTime < MIN_NOTIFICATION_INTERVAL) {
            // Si no ha pasado el tiempo mínimo, no mostrar la notificación
            return
        }
        
        // Actualizar el timestamp para este plan
        notificationTimestamps[studyPlanId] = currentTime
        
        // Crear el intent para la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("studyPlanId", studyPlanId)
            putExtra("destination", "StudyPlanDetail")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            studyPlanId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getNotificationTitle(type))
            .setContentText(getNotificationContent(studyPlan, type))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(studyPlanId.hashCode(), notification)
    }

    private fun getNotificationTitle(type: NotificationType): String {
        return when (type) {
            NotificationType.OVERDUE -> "Plan de Estudio Vencido"
            NotificationType.UPCOMING -> "Plan de Estudio Próximo a Vencer"
            NotificationType.INCOMPLETE -> "Actividades Pendientes"
        }
    }

    private fun getNotificationContent(studyPlan: StudyPlan, type: NotificationType): String {
        return when (type) {
            NotificationType.OVERDUE -> "El plan '${studyPlan.title}' ha vencido"
            NotificationType.UPCOMING -> "El plan '${studyPlan.title}' vence pronto"
            NotificationType.INCOMPLETE -> "Tienes actividades pendientes en '${studyPlan.title}'"
        }
    }
}

enum class NotificationType {
    OVERDUE,
    UPCOMING,
    INCOMPLETE
} 