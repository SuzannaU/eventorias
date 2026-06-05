package parcours.android.eventorias.ui.screen.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.data.ImageRepository
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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

    fun updateDate(input: Long?) {
        _uiState.update { it.copy(selectedDateMillis = input) }
    }

    fun updateHour(input: Int) {
        _uiState.update { it.copy(selectedHour = input) }
    }

    fun updateMinute(input: Int) {
        _uiState.update { it.copy(selectedMinute = input) }
    }

    fun updateUri(input: Uri?) {
        _uiState.update { it.copy(uri = input) }
    }

    fun generateImageUri(context: Context): Uri {
        return imageRepository.createImageUri(context)
    }

    private fun mergeDateTime(): Timestamp {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = _uiState.value.selectedDateMillis ?: System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, _uiState.value.selectedHour ?: 0)
            set(Calendar.MINUTE, _uiState.value.selectedMinute ?: 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }

    fun addEvent() {
        val user = userRepository.getCurrentUser()

        val event = Event(
            title = _uiState.value.title,
            description = _uiState.value.description,
            dateTime = mergeDateTime(),
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
        val selectedDateMillis: Long? = null,
        val selectedHour: Int? = null,
        val selectedMinute: Int? = null,
        val uri: Uri? = null,
    ) {
        val formattedTime: String
            get() = if (selectedHour != null && selectedMinute != null) {
                String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            } else {
                ""
            }

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate: String
            get() = selectedDateMillis?.let {
                dateFormatter.format(it)
            } ?: ""
    }

    sealed class SaveState {
        object Idle : SaveState()
        object EventSaved : SaveState()
        object NetworkError : SaveState()
        object UnknownError : SaveState()
    }
}
