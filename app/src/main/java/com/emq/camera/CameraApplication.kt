package com.emq.camera

import android.app.Application
import com.emq.camera.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CameraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@CameraApplication)
            modules(viewModelModule)
        }
    }
}