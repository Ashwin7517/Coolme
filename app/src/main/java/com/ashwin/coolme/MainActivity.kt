package com.ashwin.coolme

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppListAdapter
    private var appList = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnCoolDown: Button = findViewById(R.id.btnCoolDown)

        loadApps()

        btnCoolDown.setOnClickListener {
            if (!isAccessibilityServiceEnabled(this, ForceStopAccessibilityService::class.java)) {
                showAccessibilityDialog()
                return@setOnClickListener
            }

            val selectedApps = adapter.getSelectedApps()
            if (selectedApps.isEmpty()) {
                Toast.makeText(this, "Please select at least one app to close", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val packageNames = selectedApps.map { it.packageName }
            
            // Note: Accessibility Services act on system UI events. 
            // Here we would typically start the service logic
            // Because we can't directly call startClosingApps from here if it's not bound,
            // A more robust implementation would use startService or binding.
            // For simplicity in this example, we assume we can pass data to the service.
            
            val intent = Intent(this, ForceStopAccessibilityService::class.java).apply {
                putStringArrayListExtra("PACKAGES", ArrayList(packageNames))
            }
            // Start the action if service is ready. In this minimal code, we use a static method as an example.
            ForceStopAccessibilityService.appsToClose.clear()
            ForceStopAccessibilityService.appsToClose.addAll(packageNames)
            ForceStopAccessibilityService.isRunning = true
            
            if (ForceStopAccessibilityService.appsToClose.isNotEmpty()) {
               val pName = ForceStopAccessibilityService.appsToClose[0]
               val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$pName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
               }
               startActivity(detailIntent)
            }
            
            Toast.makeText(this, "Cooling down...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (packageInfo in packages) {
            // Filter out system apps loosely
            if ((packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && packageInfo.packageName != packageName) {
                val name = pm.getApplicationLabel(packageInfo).toString()
                val icon = pm.getApplicationIcon(packageInfo)
                val packageName = packageInfo.packageName
                appList.add(AppInfo(name, packageName, icon))
            }
        }
        
        adapter = AppListAdapter(appList)
        recyclerView.adapter = adapter
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(context.packageName + "/" + service.name, ignoreCase = true)
                    || componentName.equals(context.packageName + "/" + service.canonicalName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("To automatically close apps, please enable the AppCooler Accessibility Service in settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
