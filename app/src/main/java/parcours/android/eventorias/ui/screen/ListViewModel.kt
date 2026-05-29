package parcours.android.eventorias.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.ui.DispatcherProvider

class ListViewModel(
    private val dispatcher: DispatcherProvider,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _listScreenState = MutableStateFlow<ListScreenState>(ListScreenState.Loading)
    val listScreenState = _listScreenState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            delay(1000)
            withContext(dispatcher.io) {
                eventRepository.getEvents()
            }
                .collect { events ->
                    if (events.isEmpty()) {
                        _listScreenState.value = ListScreenState.NoEvents
                    } else {
                        _listScreenState.value = ListScreenState.EventsLoaded(events)
                    }
                }
        }

    }

    sealed class ListScreenState {
        object Loading : ListScreenState()
        object NoEvents : ListScreenState()

        data class Error(
            val errorMessage: String? = null,
        ) : ListScreenState()

        data class EventsLoaded(
            val events: List<Event>,
        ) : ListScreenState()
    }

}