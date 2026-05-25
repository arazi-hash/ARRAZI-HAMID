package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AnchorBlockingAccessibilityService : AccessibilityService() {

    companion object {
        var isServiceRunning = false
            private set

        private val BLOCKED_PACKAGES = setOf(
            "com.instagram.android",
            "com.tiktok.android",
            "com.snapchat.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.google.android.youtube",
            "com.facebook.lite",
            "com.twitter.android.lite"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.d("AnchorAccessibility", "Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Do not block ourselves to avoid infinite loops
            if (packageName == "com.example" || packageName == "com.aistudio.analoganchor.dmvqr") return

            val prefs = getSharedPreferences("analog_anchor_prefs", Context.MODE_PRIVATE)
            val isLocked = prefs.getBoolean("is_locked", false)
            
            if (isLocked && BLOCKED_PACKAGES.contains(packageName)) {
                // User is locked and opening a blocked distraction app! Block and redirect to us
                performBlockRedirect()
            }
        }
    }

    private fun performBlockRedirect() {
        try {
            // Display Warning
            Toast.makeText(
                this, 
                "🕌 Access Locked! Visit your physical Sanctuary to reopen daytime social media.", 
                Toast.LENGTH_LONG
            ).show()

            // Launch Analog Anchor UI
            val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            if (intent != null) {
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("AnchorAccessibility", "Redirect failed: ${e.message}")
        }
    }

    override fun onInterrupt() {
        // Required method
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
