package com.example.notification

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentPosition: Int = 0
    private var isPlaying: Boolean = false
    private var tempMusicFile: File? = null


    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION")
        when (action) {
            "PLAY" -> playMusic()
            "PAUSE" -> pauseMusic()
        }
        return START_STICKY
    }

    private fun playMusic() {
        if (isPlaying) return

        try {
            if (currentPosition == 0 || currentPosition == mediaPlayer?.duration) {
                mediaPlayer?.reset()
                val musicBytes = getMusicFromSharedPreferences()
                tempMusicFile = File.createTempFile("temp_music", ".mp3", cacheDir)
                FileOutputStream(tempMusicFile).use { it.write(musicBytes) }
                mediaPlayer?.setDataSource(tempMusicFile?.absolutePath)
                mediaPlayer?.prepare()
            } else {
                mediaPlayer?.seekTo(currentPosition)
            }

            mediaPlayer?.start()
            isPlaying = true
        } catch (e: Exception) {
            Log.e("MusicService", "Error playing music", e)
        }
    }

    private fun pauseMusic() {
        if (!isPlaying) return

        currentPosition = mediaPlayer?.currentPosition ?: 0
        mediaPlayer?.pause()
        isPlaying = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        tempMusicFile?.delete()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getMusicFromSharedPreferences(): ByteArray? {
        val prefs = getSharedPreferences("music_prefs", MODE_PRIVATE)
        val base64String = prefs.getString("music_data", null) ?: return null
        return android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
    }
}
