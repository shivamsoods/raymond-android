package com.shivam.raymond

import android.app.Application
import timber.log.Timber

class RaymondApplication: Application(){
    override fun onCreate() {
        super.onCreate()
        initializeTimber()

    }
    private fun initializeTimber() {
        Timber.plant(Timber.DebugTree())
    }
}