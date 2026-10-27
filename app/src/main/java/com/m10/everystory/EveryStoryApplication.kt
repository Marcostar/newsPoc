package com.m10.everystory

import android.app.Application
import com.m10.everystory.di.AppContainer
import com.m10.everystory.di.DefaultAppContainer
import timber.log.Timber

class EveryStoryApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        container = DefaultAppContainer(this)
    }
}
