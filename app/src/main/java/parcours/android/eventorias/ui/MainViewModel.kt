package parcours.android.eventorias.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.exceptions.AuthException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.repository.UserRepository
import parcours.android.eventorias.domain.service.AuthService

class MainViewModel(
    private val userRepository: UserRepository,
    private val authService: AuthService,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {

        viewModelScope.launch {
            // Collect updates reactively. If network drops or errors occur,
            // you handle them cleanly via flow operators like .catch {}
            authService.authState
                .catch { _uiState.value = _uiState.value.copy(isAuthConnected = false) }
                .collectLatest { user ->
                _uiState.value = _uiState.value.copy(
                    isUserAuthenticated = user != null,
                    userId = user?.uid,
                    isAuthConnected = true,
                    isLoading = false
                )
            }
        }
    }

    fun createUser() {
        viewModelScope.launch {
            try {
                userRepository.createUser()
            } catch (e: Exception) {
                e.printStackTrace()
                val errorRes = when (e) {
                    is AuthException -> R.string.auth_error
                    is NetworkException -> R.string.network_error
                    else -> R.string.unknown_error
                }
                _uiState.update { it.copy(errorMessageId = errorRes) }
            }
        }
    }

    data class MainUiState(
        val isLoading: Boolean = true,
        val isUserAuthenticated: Boolean = false,
        val userId: String? = null,
        val isAuthConnected: Boolean = false,
        val errorMessageId: Int? = null,
    )
}