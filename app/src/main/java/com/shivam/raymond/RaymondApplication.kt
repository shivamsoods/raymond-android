package com.shivam.raymond

import android.app.Application
import com.shivam.raymond.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class RaymondApplication: Application(){
    override fun onCreate() {
        super.onCreate()
        initializeTimber()
        initializeKoin()

    }
    private fun initializeTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RaymondApplication)
            modules(appModule)
        }
    }
}