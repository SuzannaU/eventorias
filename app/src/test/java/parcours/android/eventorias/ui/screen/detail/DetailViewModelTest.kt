package parcours.android.eventorias.ui.screen.detail

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.EventWithAuthor
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.domain.repository.EventWithAuthorRepository
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.MainDispatcherExtension

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val dispatcherProvider = mockk<DispatcherProvider>()
    private val eventWithAuthorRepository = mockk<EventWithAuthorRepository>(relaxed = true)
    private val eventId = "test_event_id"
    private lateinit var viewModel: DetailViewModel

    @BeforeEach
    fun setUp() {
        every { dispatcherProvider.io } returns mainDispatcherExtension.testDispatcher
        every { dispatcherProvider.main } returns mainDispatcherExtension.testDispatcher
    }

    @Test
    fun `loadEvent success should update uiState with event`() = runTest {
        val mockEventWithAuthor =
            EventWithAuthor(Event(eventId = "123", title = "title"), User(userId = "123"))
        coEvery { eventWithAuthorRepository.getEventByIdWithAuthor(eventId) } returns mockEventWithAuthor


        viewModel = DetailViewModel(dispatcherProvider, eventWithAuthorRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Success)
        assertEquals(
            mockEventWithAuthor,
            (viewModel.uiState.value as DetailViewModel.DetailUiState.Success).eventWithAuthor
        )
    }

    @Test
    fun `loadEvent failure should update uiState with error`() = runTest {
        coEvery { eventWithAuthorRepository.getEventByIdWithAuthor(eventId) } returns null


        viewModel = DetailViewModel(dispatcherProvider, eventWithAuthorRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Error)
    }

    @Test
    fun `loadEvent network exception should update uiState with network error`() = runTest {
        coEvery { eventWithAuthorRepository.getEventByIdWithAuthor(eventId) } throws NetworkException(
            "No network"
        )

        viewModel = DetailViewModel(dispatcherProvider, eventWithAuthorRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Error)
        assertEquals(
            R.string.network_error,
            (viewModel.uiState.value as DetailViewModel.DetailUiState.Error).errorMessageId
        )
    }

    @Test
    fun `loadEvent database exception should update uiState with database error`() = runTest {
        coEvery { eventWithAuthorRepository.getEventByIdWithAuthor(eventId) } throws DatabaseException(
            "Firestore error"
        )

        viewModel = DetailViewModel(dispatcherProvider, eventWithAuthorRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Error)
        assertEquals(
            R.string.database_error,
            (viewModel.uiState.value as DetailViewModel.DetailUiState.Error).errorMessageId
        )
    }
}
