package com.faultcall.mobile.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.faultcall.mobile.presentation.MainActivity

class MatchSessionForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "faultcall_match_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.faultcall.mobile.service.START"
        const val ACTION_UPDATE = "com.faultcall.mobile.service.UPDATE"
        const val ACTION_STOP = "com.faultcall.mobile.service.STOP"

        const val EXTRA_SCORE_TEXT = "extra_score_text"
        const val EXTRA_STATUS_TEXT = "extra_status_text"

        private var isForegroundStarted = false

        fun start(context: Context, scoreText: String, statusText: String) {
            val intent = Intent(context, MatchSessionForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SCORE_TEXT, scoreText)
                putExtra(EXTRA_STATUS_TEXT, statusText)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.w("MatchService", "Could not startForegroundService: ${e.message}")
            }
        }

        fun update(context: Context, scoreText: String, statusText: String) {
            val intent = Intent(context, MatchSessionForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_SCORE_TEXT, scoreText)
                putExtra(EXTRA_STATUS_TEXT, statusText)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.w("MatchService", "Could not update service: ${e.message}")
            }
        }

        fun stop(context: Context) {
            isForegroundStarted = false
            val intent = Intent(context, MatchSessionForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.w("MatchService", "Could not stop service: ${e.message}")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                isForegroundStarted = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
            ACTION_UPDATE -> {
                val score = intent.getStringExtra(EXTRA_SCORE_TEXT) ?: "Match in progress"
                val status = intent.getStringExtra(EXTRA_STATUS_TEXT) ?: "Listening for Watch8"
                if (!isForegroundStarted) {
                    try {
                        startForeground(NOTIFICATION_ID, buildNotification(score, status))
                        isForegroundStarted = true
                    } catch (e: Exception) {
                        android.util.Log.w("MatchService", "Could not startForeground on update: ${e.message}")
                    }
                } else {
                    updateNotification(score, status)
                }
            }
            ACTION_START, null -> {
                val score = intent?.getStringExtra(EXTRA_SCORE_TEXT) ?: "Match in progress"
                val status = intent?.getStringExtra(EXTRA_STATUS_TEXT) ?: "Listening for Watch8"
                try {
                    startForeground(NOTIFICATION_ID, buildNotification(score, status))
                    isForegroundStarted = true
                } catch (e: Exception) {
                    android.util.Log.w("MatchService", "Could not startForeground: ${e.message}")
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FaultCall Active Match",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps FaultCall active to announce tennis calls from Galaxy Watch8"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(scoreText: String, statusText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FaultCall — $scoreText")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(scoreText: String, statusText: String) {
        val notification = buildNotification(scoreText, statusText)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }
}
