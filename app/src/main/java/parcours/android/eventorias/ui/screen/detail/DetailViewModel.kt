package parcours.android.eventorias.ui.screen.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import parcours.android.eventorias.R
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.domain.model.Event

private const val TAG = "TAG DetailViewModel"

class DetailViewModel(
    private val eventRepository: EventRepository,
    private val eventId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val event = eventRepository.getEventById(eventId)
                if (event != null) {
                    _uiState.value = DetailUiState.Success(event)
                } else {
                    _uiState.value = DetailUiState.Error(R.string.event_not_found)
                }
            } catch (e: Exception) {
                val errorRes = when (e) {
                    is FirebaseNetworkException -> R.string.network_error
                    is FirebaseFirestoreException -> R.string.firestore_error
                    else -> R.string.unknown_error
                }
                _uiState.value = DetailUiState.Error(errorRes)
                Log.e(TAG, "Error while loading event $eventId: ${e.message}")
            }
        }
    }

    sealed class DetailUiState {
        object Loading : DetailUiState()
        data class Success(val event: Event) : DetailUiState()
        data class Error(val errorMessageId: Int) : DetailUiState()
    }
}
