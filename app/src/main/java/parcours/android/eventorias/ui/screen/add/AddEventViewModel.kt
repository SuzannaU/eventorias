package parcours.android.eventorias.ui.screen.add

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.Event

class AddEventViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    private val _uriState = MutableStateFlow<Uri?>(null)
    val uriState = _uriState.asStateFlow()

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

    fun setUri(uri: Uri?) {
        _uriState.value = uri
    }

    data class AddEventUiState(
        val event: Event = Event(),
        val date: String = "",
        val time: String = "",
        val isSaving: Boolean = false
    )

    sealed class SaveState {
        object Idle: SaveState()
        object EventSaved: SaveState()
        object NetworkError: SaveState()
        object UnknownError: SaveState()
    }
}
