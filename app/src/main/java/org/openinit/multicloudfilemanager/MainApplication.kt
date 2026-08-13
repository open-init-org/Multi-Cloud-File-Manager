package org.openinit.multicloudfilemanager

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.openinit.multicloudfilemanager.util.BiometricLockManager

class MainApplication : Application(), Application.ActivityLifecycleCallbacks {

    private var startedActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedActivityCount == 0 && BiometricLockManager.isBiometricUnlockEnabled(this)) {
            BiometricLockManager.isAppLocked = true
        }
        startedActivityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount--
        if (startedActivityCount <= 0) {
            startedActivityCount = 0
            if (BiometricLockManager.isBiometricUnlockEnabled(this) && !BiometricLockManager.isPromptShowing) {
                BiometricLockManager.isAppLocked = true
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {
        if (activity is androidx.fragment.app.FragmentActivity) {
            BiometricLockManager.checkAndPromptLock(activity)
        }
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
