package parcours.android.eventorias.ui.screen.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import parcours.android.eventorias.R
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.User

private const val TAG = "TAG ProfileViewModel"
const val FCM_ALL_TOPICS = "all"

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

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _profileScreenState.value = ProfileScreenState.UserFound(user)
                    _notificationsEnabled.value = user.subscribed
                } else {
                    _profileScreenState.value = ProfileScreenState.NoUserFound
                }
            } catch (e: Exception) {
                val errorRes = when (e) {
                    is FirebaseNetworkException -> R.string.network_error
                    is FirebaseFirestoreException -> R.string.firestore_error
                    else -> R.string.unknown_error
                }
                _profileScreenState.value = ProfileScreenState.Error(errorRes)
                Log.e(TAG, "Error while loading user profile: ${e.message}")
            }
        }
    }

    fun onNotificationsToggle(isEnabled: Boolean) {
        if (isEnabled) {
            firebaseMessaging.subscribeToTopic(FCM_ALL_TOPICS)
        } else {
            firebaseMessaging.unsubscribeFromTopic(FCM_ALL_TOPICS)
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

        data class Error(
            val errorMessageId: Int,
        ) : ProfileScreenState()
    }
}
