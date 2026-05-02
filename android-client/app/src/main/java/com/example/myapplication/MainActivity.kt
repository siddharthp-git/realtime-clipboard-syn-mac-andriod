package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var database: DatabaseReference

    private lateinit var statusText: TextView
    private lateinit var firebaseDataText: TextView
    private lateinit var testInput: EditText
    private lateinit var copyButton: Button
    private lateinit var settingsButton: Button

    private var listener: ClipboardManager.OnPrimaryClipChangedListener? = null

    private val deviceId = "android_1"
    private var lastText = ""
    private var ignoreNext = false
    private var lastTimestamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        firebaseDataText = findViewById(R.id.firebaseData)
        testInput = findViewById(R.id.testInput)
        copyButton = findViewById(R.id.copyButton)
        settingsButton = findViewById(R.id.settingsButton)

        Log.d("CLIP", "🚀 App started")

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        database = FirebaseDatabase.getInstance().getReference("clipboard")

        copyButton.setOnClickListener {
            val text = testInput.text.toString()
            if (text.isNotEmpty()) {
                val clip = ClipData.newPlainText("test", text)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }

        settingsButton.setOnClickListener {
            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "1. Enable 'ClipboardSync'\n2. Disable Battery Optimization for this app", Toast.LENGTH_LONG).show()
        }

        startService(android.content.Intent(this, ClipboardService::class.java))
        listenToFirebase()
    }

    override fun onResume() {
        super.onResume()
        startClipboardListener()
    }

    override fun onPause() {
        super.onPause()
        stopClipboardListener()
    }

    private fun startClipboardListener() {
        listener = ClipboardManager.OnPrimaryClipChangedListener {

            Log.d("CLIP", "📋 Clipboard listener triggered")

            if (ignoreNext) {
                Log.d("CLIP", "ℹ️ ignoreNext is true, skipping this event")
                ignoreNext = false
                return@OnPrimaryClipChangedListener
            }

            val clip = clipboardManager.primaryClip

            if (clip == null || clip.itemCount == 0) {
                Log.d("CLIP", "⚠️ Clipboard empty or null")
                return@OnPrimaryClipChangedListener
            }

            val item = clip.getItemAt(0)
            val text = item.text?.toString() ?: item.coerceToText(this)?.toString()

            if (text == null) {
                Log.d("CLIP", "⚠️ No text found in clip item")
                return@OnPrimaryClipChangedListener
            }

            Log.d("CLIP", "🔍 Clipboard text: $text")

            if (text != lastText) {
                sendToFirebase(text)
            } else {
                Log.d("CLIP", "ℹ️ Text is same as last sent, skipping")
            }
        }

        clipboardManager.addPrimaryClipChangedListener(listener!!)
    }

    private fun stopClipboardListener() {
        listener?.let {
            clipboardManager.removePrimaryClipChangedListener(it)
        }
    }

    private fun sendToFirebase(text: String) {
        Log.d("CLIP", "⬆️ Sending to Firebase: $text")

        val timestamp = System.currentTimeMillis()

        val data = mapOf(
            "content" to text,
            "device_id" to deviceId,
            "timestamp" to timestamp
        )

        database.setValue(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("CLIP", "✅ Sent to Firebase successfully")
            } else {
                Log.e("CLIP", "❌ Failed to send to Firebase: ${task.exception?.message}")
                Toast.makeText(this, "Firebase Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }

        lastText = text
        lastTimestamp = timestamp
    }

    // 🔥 Firebase → Android
    private fun listenToFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val content = snapshot.child("content").getValue(String::class.java)
                val sender = snapshot.child("device_id").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                if (content == null || sender == null || timestamp == null) {
                    Log.d("CLIP", "⚠️ Invalid Firebase data")
                    firebaseDataText.text = "No data in Firebase"
                    return
                }

                firebaseDataText.text = "Firebase Content: $content\nFrom: $sender"

                if (sender == deviceId || timestamp == lastTimestamp) return

                Log.d("CLIP", "⬇️ Received from Firebase: $content")

                ignoreNext = true

                val clip = ClipData.newPlainText("firebase_sync", content)
                clipboardManager.setPrimaryClip(clip)

                lastText = content
                lastTimestamp = timestamp
                
                Toast.makeText(this@MainActivity, "Clipboard updated from Firebase!", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "❌ Error: ${error.message}")
                Toast.makeText(this@MainActivity, "Firebase Read Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
