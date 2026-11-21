package com.srap.wash

import android.app.Application
import com.srap.wash.ui.washlist.WashCountdownNotifications
import dagger.hilt.android.HiltAndroidApp

lateinit var myApplication: MyApplication

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        myApplication = this
        WashCountdownNotifications(this).clearLegacyNotification()
    }
}
