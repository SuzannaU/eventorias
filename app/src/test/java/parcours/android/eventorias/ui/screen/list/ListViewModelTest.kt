package parcours.android.eventorias.ui.screen.list

import android.text.TextUtils
import android.util.Log
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.MainDispatcherExtension

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val dispatcherProvider = mockk<DispatcherProvider>()
    private lateinit var viewModel: ListViewModel

    @BeforeEach
    fun setUp() {
        every { dispatcherProvider.io } returns mainDispatcherExtension.testDispatcher
        every { dispatcherProvider.main } returns mainDispatcherExtension.testDispatcher

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun `when events are loaded, state should be EventsLoaded`() = runTest {
        val mockEvents = listOf(mockk<Event>(relaxed = true))
        every { eventRepository.getEvents() } returns flowOf(mockEvents)

        viewModel = ListViewModel(dispatcherProvider, eventRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        assertTrue(viewModel.listScreenState.value is ListViewModel.ListScreenState.EventsLoaded)
        assertEquals(
            mockEvents,
            (viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded).events
        )
    }

    @Test
    fun `when getEvents fails, state should be Error`() = runTest {
        val flow = flow<List<Event>> {
            throw NetworkException("No network")
        }
        every { eventRepository.getEvents() } returns flow

        viewModel = ListViewModel(dispatcherProvider, eventRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        assertTrue(viewModel.listScreenState.value is ListViewModel.ListScreenState.Error)
    }

    @Test
    fun `search should filter events`() = runTest {
        val event1 = mockk<Event>(relaxed = true)
        every { event1.title } returns "Apple"
        val event2 = mockk<Event>(relaxed = true)
        every { event2.title } returns "Banana"

        every { eventRepository.getEvents() } returns flowOf(listOf(event1, event2))

        viewModel = ListViewModel(dispatcherProvider, eventRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        viewModel.onSearchQueryChange("app")

        val state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(1, state.events.size)
        assertEquals("Apple", state.events[0].title)
    }

    @Test
    fun `onRetry should trigger event reload`() = runTest {
        every { eventRepository.getEvents() } returns flowOf(emptyList())
        viewModel = ListViewModel(dispatcherProvider, eventRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        viewModel.onRetry()

        verify(exactly = 2) { eventRepository.getEvents() }
    }

    @Test
    fun `sorting logic should work correctly`() = runTest {
        val event1 = mockk<Event>(relaxed = true)
        every { event1.dateTime } returns Timestamp(1000, 0)
        every { event1.category } returns Category.ART

        val event2 = mockk<Event>(relaxed = true)
        every { event2.dateTime } returns Timestamp(2000, 0)
        every { event2.category } returns Category.TECH

        every { eventRepository.getEvents() } returns flowOf(listOf(event2, event1))

        viewModel = ListViewModel(dispatcherProvider, eventRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        // Default is DATE_ASCENDING
        var state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event1, state.events[0])
        assertEquals(event2, state.events[1])

        // Sort by DATE_DESCENDING
        viewModel.sortEventsBy(1)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event2, state.events[0])
        assertEquals(event1, state.events[1])

        // Sort by CATEGORY_ASCENDING
        viewModel.sortEventsBy(2)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event1, state.events[0])
        assertEquals(event2, state.events[1])

        // Sort by CATEGORY_DESCENDING
        viewModel.sortEventsBy(3)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event2, state.events[0])
        assertEquals(event1, state.events[1])
    }
}
