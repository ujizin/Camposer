package com.ujizin.sample.feature.configuration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujizin.sample.data.local.datasource.UserDataSource
import com.ujizin.sample.domain.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val userDataSource: UserDataSource,
) : ViewModel() {

    private val _uiState: MutableStateFlow<ConfigurationUiState> = MutableStateFlow(
        value = ConfigurationUiState.Initial
    )
    val uiState = _uiState.asStateFlow()

    init {
        getUser()
    }

    private fun getUser() {
        viewModelScope.launch {
            userDataSource.getUser()
                .onStart { ConfigurationUiState.Initial }
                .catch {
                    Log.e(
                        this::class.java.name,
                        "Error on load user configuration: ${it.message}"
                    )
                }
                .collect { _uiState.emit(ConfigurationUiState.Success(it)) }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userDataSource.updateUser(user)
            _uiState.update { ConfigurationUiState.Success(user) }
        }
    }
}

sealed interface ConfigurationUiState {
    object Initial : ConfigurationUiState
    data class Success(val user: User) : ConfigurationUiState
}