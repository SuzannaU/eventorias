package parcours.android.eventorias.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import parcours.android.eventorias.R
import parcours.android.eventorias.data.UserRepository

class MainViewModel(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private lateinit var listener: FirebaseAuth.AuthStateListener

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        try {
            listener = FirebaseAuth.AuthStateListener {
                _uiState.value = _uiState.value.copy(
                    isUserAuthenticated = it.currentUser != null,
                    userId = it.currentUser?.uid,
                    isAuthConnected = true,
                    isLoading = false,
                )
            }
            firebaseAuth.addAuthStateListener(listener)
        } catch (e: FirebaseNetworkException) {
            _uiState.value = _uiState.value.copy(isAuthConnected = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(listener)
    }

    fun createUser() {
        viewModelScope.launch {
            try {
                userRepository.createUser()
            } catch (e: Exception) {
                e.printStackTrace()
                val errorRes = when (e) {
                    is FirebaseAuthException -> R.string.auth_error
                    is FirebaseNetworkException -> R.string.network_error
                    else -> R.string.unknown_error
                }
                _uiState.update {it.copy(errorMessageId = errorRes)}
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