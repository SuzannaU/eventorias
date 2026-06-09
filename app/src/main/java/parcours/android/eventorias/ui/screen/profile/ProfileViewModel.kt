package parcours.android.eventorias.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.User

class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _user = MutableStateFlow(userRepository.getCurrentUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _profileScreenState = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Loading)
    val profileScreenState: StateFlow<ProfileScreenState> = _profileScreenState.asStateFlow()

    init {
        val user = userRepository.getCurrentUser()
        if (user != null) {
            _profileScreenState.value = ProfileScreenState.UserFound(user)
        } else {
            _profileScreenState.value = ProfileScreenState.NoUserFound
        }
    }

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun onNotificationsToggle(enabled: Boolean) {
        // TODO add notification logic
        _notificationsEnabled.value = enabled
    }

    sealed class ProfileScreenState {
        object Loading : ProfileScreenState()
        object NoUserFound : ProfileScreenState()

        data class UserFound(
            val user: User,
        ) : ProfileScreenState()
    }
}
