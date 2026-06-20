package parcours.android.eventorias.ui.screen.detail

import android.text.TextUtils
import android.util.Log
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import parcours.android.eventorias.R
import parcours.android.eventorias.data.EventRepository
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.ui.MainDispatcherExtension

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val eventId = "test_event_id"
    private lateinit var viewModel: DetailViewModel

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun `loadEvent success should update uiState with event`() = runTest {
        val mockEvent = mockk<Event>()
        coEvery { eventRepository.getEventById(eventId) } returns mockEvent

        viewModel = DetailViewModel(eventRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Success)
        assertEquals(mockEvent, (viewModel.uiState.value as DetailViewModel.DetailUiState.Success).event)
    }

    @Test
    fun `loadEvent failure should update uiState with error`() = runTest {
        coEvery { eventRepository.getEventById(eventId) } returns null

        viewModel = DetailViewModel(eventRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Error)
    }

    @Test
    fun `loadEvent network exception should update uiState with network error`() = runTest {
        coEvery { eventRepository.getEventById(eventId) } throws NetworkException("No network")

        viewModel = DetailViewModel(eventRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Error)
        assertEquals(R.string.network_error, (viewModel.uiState.value as DetailViewModel.DetailUiState.Error).errorMessageId)
    }

    @Test
    fun `loadEvent database exception should update uiState with database error`() = runTest {
        coEvery { eventRepository.getEventById(eventId) } throws DatabaseException("Firestore error")

        viewModel = DetailViewModel(eventRepository, eventId)

        assertTrue(viewModel.uiState.value is DetailViewModel.DetailUiState.Error)
        assertEquals(R.string.database_error, (viewModel.uiState.value as DetailViewModel.DetailUiState.Error).errorMessageId)
    }
}
