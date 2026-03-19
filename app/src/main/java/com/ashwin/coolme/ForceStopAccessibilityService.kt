package com.ashwin.coolme

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ForceStopAccessibilityService : AccessibilityService() {

    companion object {
        var isRunning = false
        var appsToClose = mutableListOf<String>()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.DEFAULT or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
        Log.d("AppCooler", "Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isRunning || appsToClose.isEmpty()) return

        val rootNode = rootInActiveWindow ?: return
        
        // Find the "Force Stop" button or its localized equivalent.
        // Note: The text "Force stop" or "Force Stop" might change based on the device language or OEM.
        // It's usually a button in the App Info settings screen.
        var forceStopNodes = rootNode.findAccessibilityNodeInfosByText("Force stop")
        if (forceStopNodes.isEmpty()) {
             forceStopNodes = rootNode.findAccessibilityNodeInfosByText("Force Stop")
        }

        if (forceStopNodes.isNotEmpty()) {
            val button = forceStopNodes[0]
            if (button.isEnabled && button.isClickable) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        } else {
            // Find "OK" button in the confirmation dialog
            val okNodes = rootNode.findAccessibilityNodeInfosByText("OK")
            if (okNodes.isNotEmpty()) {
                val okButton = okNodes[0]
                if (okButton.isEnabled && okButton.isClickable) {
                    okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    // Once closed, move to the next app
                    appsToClose.removeAt(0)
                    closeNextApp()
                }
            }
        }
    }

    fun startClosingApps(packages: List<String>) {
        appsToClose.clear()
        appsToClose.addAll(packages)
        isRunning = true
        closeNextApp()
    }

    private fun closeNextApp() {
        if (appsToClose.isNotEmpty()) {
            val packageName = appsToClose[0]
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            startActivity(intent)
        } else {
            isRunning = false
            // Go back to the main app when done
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        isRunning = false
    }
}
