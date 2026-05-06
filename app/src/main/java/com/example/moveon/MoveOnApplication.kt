package com.example.moveon

import android.app.Application
import com.example.moveon.data.sync.InventorySyncCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MoveOnApplication : Application() {

    @Inject
    lateinit var inventorySyncCoordinator: InventorySyncCoordinator

    override fun onCreate() {
        super.onCreate()
        inventorySyncCoordinator.warmUp()
    }
}
