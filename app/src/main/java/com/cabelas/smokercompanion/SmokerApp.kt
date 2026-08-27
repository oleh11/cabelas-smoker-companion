package com.cabelas.smokercompanion

import android.app.Application
import com.cabelas.smokercompanion.data.repository.SmokerRepository
import com.cabelas.smokercompanion.data.repository.SmokerRepositoryImpl
import com.cabelas.smokercompanion.service.CookMonitoringService

class SmokerApp : Application() {

    lateinit var repository: SmokerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = SmokerRepositoryImpl(this)
        // Automatically start background cook monitoring service
        CookMonitoringService.startService(this)
    }
}
