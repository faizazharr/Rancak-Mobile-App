package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Session
import id.rancak.app.domain.repository.AuthRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SessionManagementUiState(
    val sessions: ImmutableList<Session> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val revoking: String? = null,                      // sessionId sedang dicabut
    val showRevokeConfirm: Boolean = false,
    val sessionToRevoke: Session? = null
)

class SessionManagementViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionManagementUiState())
    val uiState: StateFlow<SessionManagementUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.getSessions()) {
                is Resource.Success -> _uiState.update {
                    it.copy(sessions = result.data.toImmutableList(), isLoading = false)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun requestRevoke(session: Session) {
        _uiState.update { it.copy(showRevokeConfirm = true, sessionToRevoke = session) }
    }

    fun cancelRevoke() {
        _uiState.update { it.copy(showRevokeConfirm = false, sessionToRevoke = null) }
    }

    fun confirmRevoke() {
        val session = _uiState.value.sessionToRevoke ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showRevokeConfirm = false, sessionToRevoke = null, revoking = session.sessionId) }
            when (val result = authRepository.revokeSession(session.sessionId)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            revoking = null,
                            sessions = state.sessions.filter { it.sessionId != session.sessionId }.toImmutableList()
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(revoking = null, error = result.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
