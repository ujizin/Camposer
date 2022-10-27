package br.com.devlucasyuji.sample.feature.configuration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.devlucasyuji.sample.data.local.datasource.UserDataSource
import br.com.devlucasyuji.sample.domain.User
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
                .catch { Log.e(this::class.java.name, "error on load user configuration") }
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