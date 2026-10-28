package com.m10.everystory

import android.app.Application
import android.os.StrictMode
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.m10.everystory.di.AppContainer
import com.m10.everystory.di.DefaultAppContainer
import timber.log.Timber

class EveryStoryApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            enableStrictMode()
        }
        container = DefaultAppContainer(this)
        container.savedReminderNotifier.createChannel()
    }

    // Debug only, log instead of crash: Firebase and the ads SDK trip it on their own too.
    // Test Lab runs keep the logcat, so violations from our code can be picked out afterwards.
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
    }

    // WorkManager starts on first use and asks for this; the manifest turns off its default start-up.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(container.workerFactory).build()

    // Coil asks the Application for its image loader; ours downloads through the counted client.
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { container.imageHttpClient })) }
            .build()
}
