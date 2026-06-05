package parcours.android.eventorias.ui.screen.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.data.ImageRepository
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.User

class AddEventViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateLocation(input: String) {
        _uiState.update { it.copy(location = input) }
    }

    fun updateDate(input: String) {
        _uiState.update { it.copy(date = input) }
    }

    fun updateTime(input: String) {
        _uiState.update { it.copy(time = input) }
    }

    fun updateUri(input: Uri?) {
        _uiState.update { it.copy(uri = input) }
    }

    fun generateImageUri(context: Context): Uri {
        return imageRepository.createImageUri(context)
    }

    fun addEvent() {
        val user = userRepository.getCurrentUser()
        // TODO calculate dateTime from date and time
        val event = Event(
            title = _uiState.value.title,
            description = _uiState.value.description,
            location = _uiState.value.location,
            pictureUrl = _uiState.value.uri.toString(),
        )
        user?.let {
            try {
                eventRepository.addEvent(
                    event.copy(
                        author = user
                    ),
                    pictureUri = _uiState.value.uri,
                )
                _saveState.value = SaveState.EventSaved
            } catch (e: FirebaseNetworkException) {
                _saveState.value = SaveState.NetworkError
                Log.e("TAG", "Network Error while adding post: ${e.message}")
            } catch (e: Exception) {
                _saveState.value = SaveState.UnknownError
                Log.e("TAG", "Error while adding post: ${e.message}")
            }
        }
    }

    data class FormErrorState(
        val titleError: String? = null,
        val descriptionError: String? = null,
        val locationError: String? = null,
        val dateError: String? = null,
        val timeError: String? = null,
    )

    data class AddEventUiState(
        val author: User? = null,
        val title: String = "",
        val description: String = "",
        val location: String = "",
        val date: String = "",
        val time: String = "",
        val uri: Uri? = null,
        val isSaving: Boolean = false,
    )

    sealed class SaveState {
        object Idle : SaveState()
        object EventSaved : SaveState()
        object NetworkError : SaveState()
        object UnknownError : SaveState()
    }
}
