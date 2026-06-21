package parcours.android.eventorias.ui.screen.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.repository.EventRepository
import parcours.android.eventorias.ui.DispatcherProvider

private const val TAG = "TAG ListViewModel"

class ListViewModel(
    private val dispatcher: DispatcherProvider,
    private val eventRepository: EventRepository,
) : ViewModel() {


    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    fun onRetry() {
        _refreshTrigger.tryEmit(Unit)
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val _sortOption = MutableStateFlow(SortOption.DATE_ASCENDING)
    val sortOptions = listOf(
        R.string.date_soonest_first,
        R.string.date_latest_first,
        R.string.category_a_z,
        R.string.category_z_a,
    )

    fun sortEventsBy(sortOption: Int) {
        when (sortOption) {
            0 -> _sortOption.value = SortOption.DATE_ASCENDING
            1 -> _sortOption.value = SortOption.DATE_DESCENDING
            2 -> _sortOption.value = SortOption.CATEGORY_ASCENDING
            3 -> _sortOption.value = SortOption.CATEGORY_DESCENDING
            else -> _sortOption.value = SortOption.DATE_ASCENDING
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _eventsFlow: StateFlow<Result<List<Event>>?> = _refreshTrigger
        .flatMapLatest {
            eventRepository.getEvents()
                .map { Result.success(it) }
                .catch { e -> emit(Result.failure(e)) }
        }
        .flowOn(dispatcher.io)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val listScreenState: StateFlow<ListScreenState> = combine(
        _eventsFlow,
        _searchQuery,
        _sortOption,
    ) { result, query, sortOption ->
        if (result == null) return@combine ListScreenState.Loading
        if (result.isFailure) {
            val exception = result.exceptionOrNull()
            val errorRes = when (exception) {
                is NetworkException -> R.string.network_error
                is DatabaseException -> R.string.database_error
                else -> R.string.unknown_error
            }
            Log.e(TAG, "Error while adding event: ${exception?.message}")
            return@combine ListScreenState.Error(errorRes)
        }

        val events = result.getOrNull() ?: emptyList()

        val filteredEvents = if (query.isEmpty()) {
            events
        } else {
            events.filter { it.title.contains(query, ignoreCase = true) }
        }

        val sortedEvents = when (sortOption) {
            SortOption.DATE_ASCENDING -> filteredEvents.sortedBy { it.dateTime }
            SortOption.DATE_DESCENDING -> filteredEvents.sortedByDescending { it.dateTime }
            SortOption.CATEGORY_ASCENDING -> filteredEvents.sortedBy { it.category.name }
            SortOption.CATEGORY_DESCENDING -> filteredEvents.sortedByDescending { it.category.name }
        }

        when {
            events.isEmpty() -> ListScreenState.NoEvents
            filteredEvents.isEmpty() -> ListScreenState.NoResultsFound
            else -> ListScreenState.EventsLoaded(sortedEvents)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListScreenState.Loading
    )

    sealed class ListScreenState {
        object Loading : ListScreenState()
        object NoEvents : ListScreenState()
        object NoResultsFound : ListScreenState()

        data class Error(
            val errorMessageRes: Int,
        ) : ListScreenState()

        data class EventsLoaded(
            val events: List<Event>,
        ) : ListScreenState()
    }
}

enum class SortOption { DATE_ASCENDING, DATE_DESCENDING, CATEGORY_ASCENDING, CATEGORY_DESCENDING, }