package com.m10.everystory.di

import android.content.Context

// Manual DI: everything the app shares is created here once, and ViewModel
// factories pull what they need from it. Repositories get added as the data layer lands.
interface AppContainer

class DefaultAppContainer(
    @Suppress("unused") private val context: Context,
) : AppContainer
