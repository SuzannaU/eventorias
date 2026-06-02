package parcours.android.eventorias.ui.screen.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.data.ImageRepository
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.Event

class AddEventViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(event = it.event.copy(title = title)) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(event = it.event.copy(description = description)) }
    }

    fun updateLocation(location: String) {
        _uiState.update { it.copy(event = it.event.copy(location = location)) }
    }

    fun updateDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun updateTime(time: String) {
        _uiState.update { it.copy(time = time) }
    }

    fun updateUri(uri: Uri?) {
        _uiState.update { it.copy(uri = uri) }
        Log.d("TAG", "new uri is $uri")
    }

    fun generateImageUri(context: Context): Uri {
        return imageRepository.createImageUri(context)
    }

    data class AddEventUiState(
        val event: Event = Event(),
        val date: String = "",
        val time: String = "",
        val uri: Uri? = null,
        val isSaving: Boolean = false
    )

    sealed class SaveState {
        object Idle: SaveState()
        object EventSaved: SaveState()
        object NetworkError: SaveState()
        object UnknownError: SaveState()
    }
}
