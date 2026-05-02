package com.example.myapplication

import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate() {
        super.onCreate()

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        startForegroundService()

        clipboardManager.addPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip ?: return@addPrimaryClipChangedListener
            val text = clip.getItemAt(0).text?.toString() ?: return@addPrimaryClipChangedListener

            Log.d("CLIP", "Service detected: $text")

            val data = mapOf(
                "content" to text,
                "device_id" to "android_1",
                "timestamp" to System.currentTimeMillis()
            )

            FirebaseDatabase.getInstance()
                .getReference("clipboard")
                .setValue(data)
        }
    }

    private fun startForegroundService() {
        val channelId = "clipboard_service"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Clipboard Sync",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Clipboard Sync Running")
            .setContentText("Syncing clipboard in background")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
