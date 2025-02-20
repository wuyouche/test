package com.example.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.notification.R
import java.util.Calendar

class NotificationApplicationService(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test",
                "音樂放通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于定时提醒的通知"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification() {

        val playIntent = Intent(context, PlayMusicReceiver::class.java)
        val playPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(context, PauseMusicReceiver::class.java)
        val pausePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "test")
            .setContentTitle("test")
            .setContentText("test")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, "播放", playPendingIntent) // 添加播放按钮
            .addAction(R.drawable.ic_launcher_foreground, "暂停", pausePendingIntent) // 添加暂停按钮
            .build()

        notificationManager.notify(1, notification)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(hour: Int, minute: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val notificationService = NotificationApplicationService(it)
            notificationService.showNotification()
        }
    }
}

class PlayMusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val playIntent = Intent(it, MusicService::class.java).apply {
                putExtra("ACTION", "PLAY")
            }
            it.startService(playIntent)
        }
    }
}

class PauseMusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val pauseIntent = Intent(it, MusicService::class.java).apply {
                putExtra("ACTION", "PAUSE")
            }
            it.startService(pauseIntent)
        }
    }
}

