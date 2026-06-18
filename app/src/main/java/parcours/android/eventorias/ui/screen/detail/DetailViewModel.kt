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
            } catch (e: FirebaseNetworkException) {
                _uiState.value = DetailUiState.Error(R.string.network_error)
                Log.e(TAG, "Network Error while loading event $eventId: ${e.message}")
            } catch (e: FirebaseFirestoreException) {
                _uiState.value = DetailUiState.Error(R.string.firestore_error)
                Log.e(TAG, "Firestore Error while loading event $eventId: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(R.string.unknown_error)
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
