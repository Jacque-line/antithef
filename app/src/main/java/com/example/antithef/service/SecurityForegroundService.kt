package com.example.antithef.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.antithef.MainActivity

class SecurityForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "anti_theft_protection_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_ARM = "com.example.antithef.ACTION_START_ARM"
        const val ACTION_STOP_ARM = "com.example.antithef.ACTION_STOP_ARM"
        const val ACTION_TRIGGER_ALARM = "com.example.antithef.ACTION_TRIGGER_ALARM"
        const val ACTION_STOP_ALARM = "com.example.antithef.ACTION_STOP_ALARM"

        fun startService(context: Context, isAlarming: Boolean = false) {
            val intent = Intent(context, SecurityForegroundService::class.java).apply {
                action = if (isAlarming) ACTION_TRIGGER_ALARM else ACTION_START_ARM
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SecurityForegroundService::class.java).apply {
                action = ACTION_STOP_ARM
            }
            context.startService(intent)
        }
    }

    private var isAlarmActive = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ARM -> {
                isAlarmActive = false
                val notification = buildNotification(
                    title = "AntiTheft Protection Active",
                    text = "Monitoring Accelerometer & Proximity sensors..."
                )
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_TRIGGER_ALARM -> {
                isAlarmActive = true
                val notification = buildNotification(
                    title = "⚠️ ALARM TRIGGERED!",
                    text = "Unauthorized movement detected on device!"
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP_ALARM -> {
                isAlarmActive = false
                val notification = buildNotification(
                    title = "AntiTheft Protection Active",
                    text = "Alarm stopped. Sensors active and monitoring."
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP_ARM -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AntiTheft Security Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows real-time status of smartphone anti-theft sensors"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
