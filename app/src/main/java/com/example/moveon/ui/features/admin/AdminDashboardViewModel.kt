package com.example.moveon.ui.features.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {

    private val _state = mutableStateOf(AdminDashboardUiState())
    val state: State<AdminDashboardUiState> = _state

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            val admin = authRepository.currentUser.first()
            if (admin?.role != UserRole.ADMIN) {
                _state.value = _state.value.copy(
                    isAuthorized = false,
                    errorMessage = "Only admins can access this panel."
                )
                return@launch
            }

            _state.value = _state.value.copy(
                isAuthorized = true,
                isLoadingUsers = true,
                isLoadingProviders = true,
                errorMessage = null,
                adminUser = admin
            )
            loadUsers()
            loadProviders()
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            authRepository.getAllUsers()
                .onSuccess { users ->
                    _state.value = _state.value.copy(
                        isLoadingUsers = false,
                        users = users.sortedBy { it.firstName.lowercase() },
                        usersError = null
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoadingUsers = false,
                        usersError = e.message ?: "Unable to load users."
                    )
                }
        }
    }

    fun loadProviders() {
        viewModelScope.launch {
            logisticsRepository.getAllProvidersForAdmin()
                .onSuccess { providers ->
                    _state.value = _state.value.copy(
                        isLoadingProviders = false,
                        providers = providers.sortedBy { it.establishmentName.lowercase() },
                        providersError = null
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoadingProviders = false,
                        providersError = e.message ?: "Unable to load providers."
                    )
                }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            authRepository.updateUserByAdmin(user).onSuccess {
                loadUsers()
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val selfId = _state.value.adminUser?.id
                ?: authRepository.currentUser.first()?.id
            if (selfId != null && userId == selfId) return@launch
            authRepository.deleteUserByAdmin(userId).onSuccess {
                loadUsers()
            }
        }
    }

    fun updateProvider(provider: Provider) {
        viewModelScope.launch {
            logisticsRepository.updateProviderByAdmin(provider).onSuccess {
                loadProviders()
            }
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch {
            logisticsRepository.deleteProviderByAdmin(providerId).onSuccess {
                // keep provider/user records in sync for admin panel
                authRepository.deleteUserByAdmin(providerId)
                loadProviders()
                loadUsers()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

data class AdminDashboardUiState(
    val isAuthorized: Boolean = true,
    val isLoadingUsers: Boolean = false,
    val isLoadingProviders: Boolean = false,
    val users: List<User> = emptyList(),
    val providers: List<Provider> = emptyList(),
    val usersError: String? = null,
    val providersError: String? = null,
    val errorMessage: String? = null,
    val adminUser: User? = null
)

