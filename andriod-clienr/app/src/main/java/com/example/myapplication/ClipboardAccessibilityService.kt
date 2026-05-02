package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.database.FirebaseDatabase

class ClipboardAccessibilityService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private val database = FirebaseDatabase.getInstance().getReference("clipboard")
    private val deviceId = "android_1"
    private var lastText = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("CLIP_SERVICE", "✅ Accessibility Service Connected")
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        // This listener helps detect changes immediately if the system allows it
        clipboardManager.addPrimaryClipChangedListener {
            Log.d("CLIP_SERVICE", "📋 Clipboard changed listener triggered")
            checkClipboard()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Any interaction (tap, scroll, window change) triggers a check
        checkClipboard()
    }

    private fun checkClipboard() {
        if (!::clipboardManager.isInitialized) return

        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            
            if (text.isNotEmpty() && text != lastText) {
                Log.d("CLIP_SERVICE", "⬆️ Background Copy Detected: $text")
                
                val data = mapOf(
                    "content" to text,
                    "device_id" to deviceId,
                    "timestamp" to System.currentTimeMillis()
                )

                database.setValue(data).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("CLIP_SERVICE", "✅ Background Sync Successful")
                    } else {
                        Log.e("CLIP_SERVICE", "❌ Background Sync Failed: ${task.exception?.message}")
                    }
                }
                
                lastText = text
            }
        }
    }

    override fun onInterrupt() {
        Log.d("CLIP_SERVICE", "⚠️ Service Interrupted")
    }
}
