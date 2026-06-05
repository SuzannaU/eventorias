package parcours.android.eventorias

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import parcours.android.eventorias.data.ImageRepository
import parcours.android.eventorias.data.UserRepository

class MainViewModel(
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
    private val firebaseAuth: FirebaseAuth,
): ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private lateinit var listener: FirebaseAuth.AuthStateListener

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        try {
            listener = FirebaseAuth.AuthStateListener {
                _authState.value = AuthState(isAuthenticated = it.currentUser != null)
            }
            firebaseAuth.addAuthStateListener(listener)
        } catch (e: FirebaseNetworkException) {
            Log.e("FirebaseNetworkException", e.message.toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(listener)
    }

    fun createUser() {
        viewModelScope.launch{
            userRepository.createUser()
        }
    }

    fun signOut() {
        userRepository.signOut()
    }


data class AuthState(
    val isAuthenticated: Boolean = false,
)

    fun generateImageUri(context: Context): Uri {
        return imageRepository.createImageUri(context)
    }
}