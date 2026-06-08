package parcours.android.eventorias.ui.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.ui.DispatcherProvider

class ListViewModel(
    private val dispatcher: DispatcherProvider,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val _events = eventRepository.getEvents()
        .flowOn(dispatcher.io)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _eventsFlow = _refreshTrigger.flatMapLatest {
        eventRepository.getEvents()
            .catch { e ->
                emit(emptyList())
            }
    }
        .flowOn(dispatcher.io)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _listScreenState = MutableStateFlow<ListScreenState>(ListScreenState.Loading)
    //val listScreenState = _listScreenState.asStateFlow()
    val listScreenState: StateFlow<ListScreenState> = combine(
        _eventsFlow,
        _searchQuery
    ) { events, query ->
        if (events == null) return@combine ListScreenState.Loading

        val filteredEvents = if (query.isEmpty()) {
            events
        } else {
            events.filter { it.title.contains(query, ignoreCase = true) }
        }

        when {
            events.isEmpty() -> ListScreenState.NoEvents
            filteredEvents.isEmpty() -> ListScreenState.NoResultsFound
            else -> ListScreenState.EventsLoaded(filteredEvents)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListScreenState.Loading
    )

    fun onRetry() {
        _refreshTrigger.tryEmit(Unit)
    }

    sealed class ListScreenState {
        object Loading : ListScreenState()
        object NoEvents : ListScreenState()
        object NoResultsFound : ListScreenState()

        data class Error(
            val errorMessage: String? = null,
        ) : ListScreenState()

        data class EventsLoaded(
            val events: List<Event>,
        ) : ListScreenState()
    }
}