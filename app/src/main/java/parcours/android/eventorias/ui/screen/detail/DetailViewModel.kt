package parcours.android.eventorias.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.domain.model.Event

class DetailViewModel(
    private val eventRepository: EventRepository,
    private val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val event = eventRepository.getEventById(eventId)
            if (event != null) {
                _uiState.value = DetailUiState.Success(event)
            } else {
                _uiState.value = DetailUiState.Error("Event not found")
            }
        }
    }

    sealed class DetailUiState {
        object Loading : DetailUiState()
        data class Success(val event: Event) : DetailUiState()
        data class Error(val errorMessage: String) : DetailUiState()
    }
}
