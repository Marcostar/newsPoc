package com.m10.everystory

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.m10.everystory.di.AppContainer
import com.m10.everystory.di.DefaultAppContainer
import timber.log.Timber

class EveryStoryApplication : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        container = DefaultAppContainer(this)
    }

    // Coil asks the Application for its image loader; ours downloads through the counted client.
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { container.imageHttpClient })) }
            .build()
}
