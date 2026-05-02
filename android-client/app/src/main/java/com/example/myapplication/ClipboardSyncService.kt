package com.example.myapplication

import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class ClipboardSyncService : Service() {

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var database: DatabaseReference
    private val deviceId = "android_1"
    private var lastTimestamp: Long = 0

    companion object {
        private const val CHANNEL_ID = "ClipboardSyncChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        database = FirebaseDatabase.getInstance().getReference("clipboard")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Clipboard Sync Active")
            .setContentText("Listening for clipboard updates...")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        listenToFirebase()

        return START_STICKY
    }

    private fun listenToFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val content = snapshot.child("content").getValue(String::class.java)
                val sender = snapshot.child("device_id").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                if (content == null || sender == null || timestamp == null) return

                if (sender == deviceId || timestamp == lastTimestamp) return

                Log.d("CLIP_SERVICE", "⬇️ Foreground Service: Received from Firebase: $content")

                // Note: Writing to clipboard in background is restricted on Android 10+
                // But we attempt it anyway as part of the service logic.
                try {
                    val clip = ClipData.newPlainText("firebase_sync", content)
                    clipboardManager.setPrimaryClip(clip)
                    lastTimestamp = timestamp
                } catch (e: Exception) {
                    Log.e("CLIP_SERVICE", "❌ Error setting clipboard: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CLIP_SERVICE", "❌ Firebase Error: ${error.message}")
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Clipboard Sync Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
