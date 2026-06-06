package parcours.android.eventorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import parcours.android.eventorias.data.UserRepository

class MainViewModel(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _userAuthState = MutableStateFlow(UserAuthState(isUserAuthenticated = firebaseAuth.currentUser != null))
    val userAuthState = _userAuthState.asStateFlow()

    private val _authNetworkState = MutableStateFlow(NetworkState())
    val authNetworkState = _authNetworkState.asStateFlow()

    private lateinit var listener: FirebaseAuth.AuthStateListener

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        try {
            listener = FirebaseAuth.AuthStateListener {
                _userAuthState.value = UserAuthState(isUserAuthenticated = it.currentUser != null)
            }
            firebaseAuth.addAuthStateListener(listener)
            _authNetworkState.update {
                _authNetworkState.value.copy(isAuthConnected = true)
            }
        } catch (e: FirebaseNetworkException) {
            _authNetworkState.update {
                _authNetworkState.value.copy(isAuthConnected = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(listener)
    }

    fun createUser() {
        viewModelScope.launch {
            userRepository.createUser()
        }
    }

    fun signOut() {
        userRepository.signOut()
    }


    data class UserAuthState(
        val isUserAuthenticated: Boolean = false,
    )

    data class NetworkState(
        val isAuthConnected: Boolean = false,
    )
}