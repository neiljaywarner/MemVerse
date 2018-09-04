package com.spiritflightapps.memverse

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import java.lang.ref.WeakReference

class MVApplication : Application(), Application.ActivityLifecycleCallbacks {
    /*
    // consider Koin here
    */

    override fun onCreate() {
        super.onCreate()
        instance = this

        registerActivityLifecycleCallbacks(this)

        enableCrashlyticsIfRelease()
        //TODO: onLowMemory callbacks for crashlytics logging.

    }

    override fun onTerminate() {
        super.onTerminate()

        unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityPaused(activity: Activity?) {
        if (activity == null) {
            return
        }
        Crashlytics.log("onActivityPaused ${activity.localClassName}")
    }

    override fun onActivityResumed(activity: Activity?) {
        if (activity == null) {
            return
        }
        currentActivity = activity
        Crashlytics.log("onActivityResumed ${activity.localClassName}")
    }

    override fun onActivityStarted(activity: Activity?) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity?) {

    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        if (activity == null) {
            return
        }
        Crashlytics.log("onActivitySaveInstanceState ${activity.localClassName}")

    }

    override fun onActivityStopped(activity: Activity?) {

    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    private fun enableCrashlyticsIfRelease() {
        if (BuildConfig.DEBUG.not()) {
            Fabric.with(this, Crashlytics())
        }
    }

    private lateinit var currentActivityWeakReference: WeakReference<Activity?>
    var currentActivity: Activity?
        get() = currentActivityWeakReference.get()
        set(value) {
            currentActivityWeakReference = WeakReference(value)
        }

    companion object {
        lateinit var instance: MVApplication
            private set
        val currentActivity: Activity?
            get() = MVApplication.instance.currentActivity
    }
}