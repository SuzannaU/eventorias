package parcours.android.eventorias.ui.screen.list

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.EventWithAuthor
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.domain.repository.EventWithAuthorRepository
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.MainDispatcherExtension
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val eventWithAuthorRepository = mockk<EventWithAuthorRepository>(relaxed = true)
    private val dispatcherProvider = mockk<DispatcherProvider>()
    private lateinit var viewModel: ListViewModel

    @BeforeEach
    fun setUp() {
        every { dispatcherProvider.io } returns mainDispatcherExtension.testDispatcher
        every { dispatcherProvider.main } returns mainDispatcherExtension.testDispatcher
    }

    @Test
    fun `when events are loaded, state should be EventsLoaded`() = runTest {
        val mockEvents = listOf(mockk<Event>(relaxed = true))
        val mockAuthor = mockk<User>(relaxed = true)
        val fakeEventsWithAuthor = listOf(
            EventWithAuthor(mockEvents[0], mockAuthor)
        )
        coEvery { eventWithAuthorRepository.getEventsWithAuthor() } returns flowOf(
            fakeEventsWithAuthor
        )

        viewModel = ListViewModel(dispatcherProvider, eventWithAuthorRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        assertTrue(viewModel.listScreenState.value is ListViewModel.ListScreenState.EventsLoaded)
        assertEquals(
            fakeEventsWithAuthor,
            (viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded).eventsWithAuthor
        )
    }

    @Test
    fun `when getEvents fails, state should be Error`() = runTest {
        val flow = flow<List<EventWithAuthor>> {
            throw NetworkException("No network")
        }
        coEvery { eventWithAuthorRepository.getEventsWithAuthor() } returns flow

        viewModel = ListViewModel(dispatcherProvider, eventWithAuthorRepository)
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
        val mockAuthor = User(userId = "123")
        val eventsWithAuthor = listOf(
            EventWithAuthor(event1, mockAuthor),
            EventWithAuthor(event2, mockAuthor),
        )

        coEvery { eventWithAuthorRepository.getEventsWithAuthor() } returns flowOf(eventsWithAuthor)


        viewModel = ListViewModel(dispatcherProvider, eventWithAuthorRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        viewModel.onSearchQueryChange("app")

        val state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(1, state.eventsWithAuthor.size)
        assertEquals("Apple", state.eventsWithAuthor[0].event.title)
    }

    @Test
    fun `onRetry should trigger event reload`() = runTest {
        coEvery { eventWithAuthorRepository.getEventsWithAuthor() } returns flowOf(emptyList())

        viewModel = ListViewModel(dispatcherProvider, eventWithAuthorRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        viewModel.onRetry()

        coVerify(exactly = 2) { eventWithAuthorRepository.getEventsWithAuthor() }
    }

    @Test
    fun `sorting logic should work correctly`() = runTest {
        val event1 = mockk<Event>(relaxed = true)
        every { event1.dateTime } returns Calendar.getInstance().apply { timeInMillis = 1000 }.time
        every { event1.category } returns Category.ART

        val event2 = mockk<Event>(relaxed = true)
        every { event2.dateTime } returns Calendar.getInstance().apply { timeInMillis = 2000 }.time
        every { event2.category } returns Category.TECH

        val mockAuthor = mockk<User>(relaxed = true)
        every { mockAuthor.userId } returns "123"

        val mockEventWithAuthor = listOf(
            EventWithAuthor(event1, mockAuthor),
            EventWithAuthor(event2, mockAuthor),
        )

        coEvery { eventWithAuthorRepository.getEventsWithAuthor() } returns flowOf(
            mockEventWithAuthor
        )

        viewModel = ListViewModel(dispatcherProvider, eventWithAuthorRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        // Default is DATE_ASCENDING
        var state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event1, state.eventsWithAuthor[0].event)
        assertEquals(event2, state.eventsWithAuthor[1].event)

        // Sort by DATE_DESCENDING
        viewModel.sortEventsBy(SortOption.DATE_DESCENDING)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event2, state.eventsWithAuthor[0].event)
        assertEquals(event1, state.eventsWithAuthor[1].event)

        // Sort by CATEGORY_ASCENDING
        viewModel.sortEventsBy(SortOption.CATEGORY_ASCENDING)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event1, state.eventsWithAuthor[0].event)
        assertEquals(event2, state.eventsWithAuthor[1].event)

        // Sort by CATEGORY_DESCENDING
        viewModel.sortEventsBy(SortOption.CATEGORY_DESCENDING)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event2, state.eventsWithAuthor[0].event)
        assertEquals(event1, state.eventsWithAuthor[1].event)
    }
}
