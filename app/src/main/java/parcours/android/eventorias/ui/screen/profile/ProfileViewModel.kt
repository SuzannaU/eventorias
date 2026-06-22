package parcours.android.eventorias.ui.screen.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.domain.repository.UserRepository
import parcours.android.eventorias.domain.service.NotificationService
import parcours.android.eventorias.ui.DispatcherProvider

private const val TAG = "TAG ProfileViewModel"
const val FCM_ALL_TOPICS = "all"

class ProfileViewModel(
    private val dispatcher: DispatcherProvider,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
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
        viewModelScope.launch(dispatcher.io) {
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
                    is NetworkException -> R.string.network_error
                    is DatabaseException -> R.string.database_error
                    else -> R.string.unknown_error
                }
                _profileScreenState.value = ProfileScreenState.Error(errorRes)
                Log.e(TAG, "Error while loading user profile: ${e.message}")
            }
        }
    }

    fun onNotificationsToggle(isEnabled: Boolean) {
        if (isEnabled) {
            notificationService.subscribeToTopic(FCM_ALL_TOPICS)
        } else {
            notificationService.unsubscribeFromTopic(FCM_ALL_TOPICS)
        }
        viewModelScope.launch(dispatcher.io) {
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
