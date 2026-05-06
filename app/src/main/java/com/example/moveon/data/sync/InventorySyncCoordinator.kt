package com.example.moveon.data.sync

import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.InventoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventorySyncCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val inventoryRepository: InventoryRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            authRepository.currentUser
                .distinctUntilChanged { a, b -> a?.id == b?.id }
                .collect { user ->
                    val uid = user?.id
                    if (!uid.isNullOrBlank()) {
                        inventoryRepository.syncInventoryWithCloud(uid)
                    }
                }
        }
    }

    /** Ensures eager initialization when referenced from Application.onCreate(). */
    fun warmUp() = Unit
}
