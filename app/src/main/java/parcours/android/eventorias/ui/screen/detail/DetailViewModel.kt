package parcours.android.eventorias.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.EventWithAuthor
import parcours.android.eventorias.domain.repository.EventWithAuthorRepository
import parcours.android.eventorias.ui.DispatcherProvider

class DetailViewModel(
    private val dispatcher: DispatcherProvider,
    private val eventWithAuthorRepository: EventWithAuthorRepository,
    private val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch(dispatcher.io) {
            _uiState.value = DetailUiState.Loading
            try {
                val event = eventWithAuthorRepository.getEventByIdWithAuthor(eventId)

                _uiState.value = when {
                    event == null -> DetailUiState.Error(R.string.event_not_found)
                    else -> DetailUiState.Success(event)
                }
            } catch (e: Exception) {
                val errorRes = when (e) {
                    is NetworkException -> R.string.network_error
                    is DatabaseException -> R.string.database_error
                    else -> R.string.unknown_error
                }
                _uiState.value = DetailUiState.Error(errorRes)
            }
        }
    }

    sealed class DetailUiState {
        object Loading : DetailUiState()
        data class Success(val eventWithAuthor: EventWithAuthor) : DetailUiState()
        data class Error(val errorMessageId: Int) : DetailUiState()
    }
}
