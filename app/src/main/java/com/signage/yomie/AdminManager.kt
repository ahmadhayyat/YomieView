package com.signage.yomie


import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent


class AdminManager : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        // Enable the profile
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName: ComponentName = getComponentName(context)
        manager.setProfileName(componentName, "Yomie Profile")
        // Open the main screen
        val launch = Intent(context, RegistrationActivity::class.java)
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
    }

    fun getComponentName(context: Context): ComponentName {
        return ComponentName(context.applicationContext, AdminManager::class.java)
    }
}