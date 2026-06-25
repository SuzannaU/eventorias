package parcours.android.eventorias.ui.screen.list

import android.util.Log
import io.mockk.coEvery
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
import parcours.android.eventorias.data.repository.FirebaseEventRepository
import parcours.android.eventorias.data.repository.FirebaseUserRepository
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.MainDispatcherExtension
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val eventRepository = mockk<FirebaseEventRepository>(relaxed = true)
    private val userRepository = mockk<FirebaseUserRepository>(relaxed = true)
    private val dispatcherProvider = mockk<DispatcherProvider>()
    private lateinit var viewModel: ListViewModel

    @BeforeEach
    fun setUp() {
        every { dispatcherProvider.io } returns mainDispatcherExtension.testDispatcher
        every { dispatcherProvider.main } returns mainDispatcherExtension.testDispatcher

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun `when events are loaded, state should be EventsLoaded`() = runTest {
        val mockEvents = listOf(mockk<Event>(relaxed = true))
        val mockAuthor = mockk<User>(relaxed = true)
        val fakeEventsWithAuthor = listOf(
            Pair(mockEvents[0], mockAuthor)
        )
        every { eventRepository.getEvents() } returns flowOf(mockEvents)
        coEvery { userRepository.getUserById(any()) } returns mockAuthor

        viewModel = ListViewModel(dispatcherProvider, eventRepository, userRepository)
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
        val flow = flow<List<Event>> {
            throw NetworkException("No network")
        }
        every { eventRepository.getEvents() } returns flow

        viewModel = ListViewModel(dispatcherProvider, eventRepository, userRepository)
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

        every { eventRepository.getEvents() } returns flowOf(listOf(event1, event2))
        coEvery { userRepository.getUserById(any()) } returns mockAuthor


        viewModel = ListViewModel(dispatcherProvider, eventRepository, userRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        viewModel.onSearchQueryChange("app")

        val state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(1, state.eventsWithAuthor.size)
        assertEquals("Apple", state.eventsWithAuthor[0].first.title)
    }

    @Test
    fun `onRetry should trigger event reload`() = runTest {
        val mockAuthor = User(userId = "123")
        every { eventRepository.getEvents() } returns flowOf(emptyList())
        coEvery { userRepository.getUserById(any()) } returns mockAuthor

        viewModel = ListViewModel(dispatcherProvider, eventRepository, userRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        viewModel.onRetry()

        verify(exactly = 2) { eventRepository.getEvents() }
    }

    @Test
    fun `sorting logic should work correctly`() = runTest {
        val event1 = mockk<Event>(relaxed = true)
        every { event1.dateTime } returns Calendar.getInstance().apply {timeInMillis = 1000}.time
        every { event1.category } returns Category.ART

        val event2 = mockk<Event>(relaxed = true)
        every { event2.dateTime } returns Calendar.getInstance().apply {timeInMillis = 2000}.time
        every { event2.category } returns Category.TECH

        val mockAuthor = mockk<User>(relaxed = true)
        every { mockAuthor.userId } returns "123"

        every { eventRepository.getEvents() } returns flowOf(listOf(event2, event1))
        coEvery { userRepository.getUserById(any()) } returns mockAuthor

        viewModel = ListViewModel(dispatcherProvider, eventRepository, userRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listScreenState.collect()
        }

        // Default is DATE_ASCENDING
        var state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event1, state.eventsWithAuthor[0].first)
        assertEquals(event2, state.eventsWithAuthor[1].first)

        // Sort by DATE_DESCENDING
        viewModel.sortEventsBy(1)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event2, state.eventsWithAuthor[0].first)
        assertEquals(event1, state.eventsWithAuthor[1].first)

        // Sort by CATEGORY_ASCENDING
        viewModel.sortEventsBy(2)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event1, state.eventsWithAuthor[0].first)
        assertEquals(event2, state.eventsWithAuthor[1].first)

        // Sort by CATEGORY_DESCENDING
        viewModel.sortEventsBy(3)
        state = viewModel.listScreenState.value as ListViewModel.ListScreenState.EventsLoaded
        assertEquals(event2, state.eventsWithAuthor[0].first)
        assertEquals(event1, state.eventsWithAuthor[1].first)
    }
}
