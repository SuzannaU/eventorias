package parcours.android.eventorias.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.User

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging,
) : ViewModel() {

    private val _profileScreenState =
        MutableStateFlow<ProfileScreenState>(ProfileScreenState.Loading)
    val profileScreenState: StateFlow<ProfileScreenState> = _profileScreenState.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            if (user != null) {
                _profileScreenState.value = ProfileScreenState.UserFound(user)
                _notificationsEnabled.value = user.subscribed
            } else {
                _profileScreenState.value = ProfileScreenState.NoUserFound
            }
        }
    }

    fun onNotificationsToggle(isEnabled: Boolean) {
        if (isEnabled) {
            firebaseMessaging.subscribeToTopic("all")
        } else {
            firebaseMessaging.unsubscribeFromTopic("all")
        }
        viewModelScope.launch {
            userRepository.updateSubscriptionStatus(isEnabled)
            _notificationsEnabled.value = isEnabled
        }
    }

    fun signOut() {
        userRepository.signOut()
    }

    sealed class ProfileScreenState {
        object Loading : ProfileScreenState()
        object NoUserFound : ProfileScreenState()

        data class UserFound(
            val user: User,
        ) : ProfileScreenState()
    }
}
