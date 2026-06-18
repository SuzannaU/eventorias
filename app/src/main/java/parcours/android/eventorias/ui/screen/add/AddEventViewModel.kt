package parcours.android.eventorias.ui.screen.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import parcours.android.eventorias.R
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.data.ImageRepository
import parcours.android.eventorias.data.UserRepository
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.User
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = "TAG AddEventViewModel"

class AddEventViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FromUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun updateTitle(input: String) {
        _uiState.update {
            it.copy(
                title = input,
                formErrors = it.formErrors.copy(titleError = false)
            )
        }
    }

    fun updateDescription(input: String) {
        _uiState.update {
            it.copy(
                description = input,
                formErrors = it.formErrors.copy(descriptionError = false)
            )
        }
    }

    fun updateCategory(input: Category) {
        _uiState.update {
            it.copy(
                category = input,
                formErrors = it.formErrors.copy(categoryError = false)
            )
        }
    }

    fun updateLocation(input: String) {
        _uiState.update {
            it.copy(
                location = input,
                formErrors = it.formErrors.copy(locationError = false)
            )
        }
    }

    fun updateDate(input: Long?) {
        _uiState.update {
            it.copy(
                selectedDateMillis = input,
                formErrors = it.formErrors.copy(dateError = false)
            )
        }
    }

    fun updateHour(input: Int) {
        _uiState.update {
            it.copy(
                selectedHour = input,
                formErrors = it.formErrors.copy(timeError = false)
            )
        }
    }

    fun updateMinute(input: Int) {
        _uiState.update {
            it.copy(
                selectedMinute = input,
                formErrors = it.formErrors.copy(timeError = false)
            )
        }
    }

    fun updateUri(input: Uri?) {
        _uiState.update { it.copy(uri = input) }
    }

    fun generateImageUri(context: Context): Uri {
        return imageRepository.createImageUri(context)
    }

    private fun mergeDateTime(): Timestamp {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.selectedDateMillis ?: System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, _uiState.value.selectedHour ?: 0)
            set(Calendar.MINUTE, _uiState.value.selectedMinute ?: 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }

    fun addEvent() {
        if (!validate()) return
        _saveState.value = SaveState.Loading

        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                val event = Event(
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    category = uiState.value.category ?: Category.OTHER,
                    dateTime = mergeDateTime(),
                    location = _uiState.value.location,
                    pictureUrl = _uiState.value.uri.toString(),
                )
                user?.let {
                    eventRepository.addEvent(
                        event.copy(
                            author = user
                        ),
                        pictureUri = _uiState.value.uri,
                    )
                    _saveState.value = SaveState.EventSaved
                }
            } catch (e: FirebaseNetworkException) {
                _saveState.value = SaveState.Error(R.string.network_error)
                Log.e(TAG, "Network Error while adding post: ${e.message}")
            } catch (e: FirebaseFirestoreException) {
                _saveState.value = SaveState.Error(R.string.firestore_error)
                Log.e(TAG, "Error while adding post: ${e.message}")
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(R.string.unknown_error)
                Log.e(TAG, "Error while adding post: ${e.message}")
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value

        val titleError = state.title.isBlank()
        val titleLengthError = state.title.length > 25
        val descriptionError = state.description.isBlank()
        val categoryError = state.category == null
        val locationError = state.location.isBlank()
        val dateError = state.selectedDateMillis == null
        val timeError = state.selectedHour == null

        val errors = FormErrorState(
            titleError = titleError,
            titleLengthError = titleLengthError,
            descriptionError = descriptionError,
            categoryError = categoryError,
            locationError = locationError,
            dateError = dateError,
            timeError = timeError
        )

        _uiState.update { it.copy(formErrors = errors) }

        return !titleError && !titleLengthError && !descriptionError && !categoryError &&
                !locationError && !dateError && !timeError
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    data class FromUiState(
        val author: User? = null,
        val title: String = "",
        val description: String = "",
        val category: Category? = null,
        val location: String = "",
        val selectedDateMillis: Long? = null,
        val selectedHour: Int? = null,
        val selectedMinute: Int? = null,
        val uri: Uri? = null,
        val formErrors: FormErrorState = FormErrorState(),
    ) {

        private val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
        val formattedDate: String
            get() = selectedDateMillis?.let {
                dateFormatter.format(it)
            } ?: ""

        val formattedTime: String
            get() = if (selectedHour != null && selectedMinute != null) {
                String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            } else {
                ""
            }
    }

    data class FormErrorState(
        val titleError: Boolean = false,
        val titleLengthError: Boolean = false,
        val descriptionError: Boolean = false,
        val categoryError: Boolean = false,
        val locationError: Boolean = false,
        val dateError: Boolean = false,
        val timeError: Boolean = false,
    )

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object EventSaved : SaveState()
        data class Error(val messageId: Int) : SaveState()
    }
}
