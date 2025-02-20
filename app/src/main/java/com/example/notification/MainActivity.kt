package com.example.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        getMusicFromAssets()?.let {
            saveMusicToSharedPreferences(context = this,
                it
            )
        }


        val notificationService = NotificationApplicationService(this)
        notificationService.scheduleNotification(16, 44)
        setContent {

        }
    }

    private fun getMusicFromAssets(): ByteArray? {
        return try {
            val inputStream: InputStream = assets.open("music2.mp3")
            val buffer = ByteArrayOutputStream()
            val byteChunk = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(byteChunk).also { bytesRead = it } != -1) {
                buffer.write(byteChunk, 0, bytesRead)
            }

            inputStream.close()
            buffer.toByteArray()
        } catch (e: IOException) {
            Log.e("MainActivity", "读取 assets 文件失败", e)
            null
        }
    }

    private fun saveMusicToSharedPreferences(context: Context, musicBytes: ByteArray) {
        val encodedString = Base64.encodeToString(musicBytes, Base64.DEFAULT)
        val prefs = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("music_data", encodedString).apply()
    }

}
