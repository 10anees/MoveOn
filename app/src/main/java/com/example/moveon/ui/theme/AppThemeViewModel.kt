package com.example.moveon.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.data.local.dao.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity-level ViewModel that surfaces the persisted dark-mode preference to
 * `MoveOnTheme`. Kept intentionally tiny so it can be hosted at the very root
 * of the Compose tree (in `MainActivity`) without dragging in feature-specific
 * dependencies.
 *
 * The state is sourced from [UserPreferences.darkModeFlow], which is a hot
 * `StateFlow` updated whenever any caller (e.g. `SettingsViewModel`) changes
 * the preference. As soon as the flow emits, the wrapping `MoveOnTheme`
 * recomposes and the entire UI flips palettes.
 */
@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val darkModeEnabled: StateFlow<Boolean> = userPreferences.darkModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = userPreferences.darkModeFlow.value
        )

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkModeEnabled(enabled)
        }
    }
}
