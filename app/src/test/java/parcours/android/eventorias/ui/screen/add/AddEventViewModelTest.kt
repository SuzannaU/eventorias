package parcours.android.eventorias.ui.screen.add

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import parcours.android.eventorias.R
import parcours.android.eventorias.data.repository.FirebaseEventRepository
import parcours.android.eventorias.data.repository.FirebaseUserRepository
import parcours.android.eventorias.data.repository.ImageRepositoryImpl
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.ui.MainDispatcherExtension

@OptIn(ExperimentalCoroutinesApi::class)
class AddEventViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val eventRepository = mockk<FirebaseEventRepository>(relaxed = true)
    private val userRepository = mockk<FirebaseUserRepository>(relaxed = true)
    private val imageRepository = mockk<ImageRepositoryImpl>(relaxed = true)
    private lateinit var viewModel: AddEventViewModel

    @BeforeEach
    fun setUp() {
        viewModel = AddEventViewModel(eventRepository, userRepository, imageRepository)

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun `updateDescription should update uiState`() {
        viewModel.updateDescription("New Desc")
        assertEquals("New Desc", viewModel.uiState.value.description)
        assertFalse(viewModel.uiState.value.formErrors.descriptionError)
    }

    @Test
    fun `updateLocation should update uiState`() {
        viewModel.updateLocation("New Loc")
        assertEquals("New Loc", viewModel.uiState.value.location)
        assertFalse(viewModel.uiState.value.formErrors.locationError)
    }

    @Test
    fun `updateTitle should update uiState and clear error`() {
        viewModel.updateTitle("New Title")
        assertEquals("New Title", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.formErrors.titleError)
    }

    @Test
    fun `validate should catch long title`() {
        viewModel.updateTitle("a".repeat(26))
        viewModel.addEvent()
        assertTrue(viewModel.uiState.value.formErrors.titleLengthError)
    }

    @Test
    fun `validate should catch empty description`() {
        viewModel.updateDescription("")
        viewModel.addEvent()
        assertTrue(viewModel.uiState.value.formErrors.descriptionError)
    }

    @Test
    fun `validate should catch empty title`() {
        viewModel.updateTitle("")
        viewModel.addEvent()
        assertTrue(viewModel.uiState.value.formErrors.titleError)
    }

    @Test
    fun `addEvent should call repository when valid and update state`() = runTest {
        viewModel.updateTitle("Valid Title")
        viewModel.updateDescription("Valid Description")
        viewModel.updateCategory(Category.ART)
        viewModel.updateLocation("Valid Location")
        viewModel.updateDate(System.currentTimeMillis())
        viewModel.updateHour(10)
        viewModel.updateMinute(30)

        val mockUri = mockk<Uri>()
        viewModel.updateUri(mockUri)

        coEvery { userRepository.getCurrentUser() } returns mockk(relaxed = true)

        viewModel.addEvent()

        coVerify { eventRepository.addEvent(any(), mockUri) }
        assertEquals(AddEventViewModel.SaveState.EventSaved, viewModel.saveState.value)
    }

    @Test
    fun `addEvent network exception should update state with network error`() = runTest {
        viewModel.updateTitle("Valid Title")
        viewModel.updateDescription("Valid Description")
        viewModel.updateCategory(Category.ART)
        viewModel.updateLocation("Valid Location")
        viewModel.updateDate(System.currentTimeMillis())
        viewModel.updateHour(10)
        viewModel.updateMinute(30)
        val mockUri = mockk<Uri>()
        viewModel.updateUri(mockUri)

        coEvery { userRepository.getCurrentUser() } returns mockk(relaxed = true)
        coEvery { eventRepository.addEvent(any(), any()) } throws NetworkException("No network")

        viewModel.addEvent()

        coVerify { eventRepository.addEvent(any(), mockUri) }
        assertTrue(viewModel.saveState.value is AddEventViewModel.SaveState.Error)
        assertEquals(
            R.string.network_error,
            (viewModel.saveState.value as AddEventViewModel.SaveState.Error).messageId
        )
    }

    @Test
    fun `addEvent database exception should update state with firestore error`() = runTest {
        viewModel.updateTitle("Valid Title")
        viewModel.updateDescription("Valid Description")
        viewModel.updateCategory(Category.ART)
        viewModel.updateLocation("Valid Location")
        viewModel.updateDate(System.currentTimeMillis())
        viewModel.updateHour(10)
        viewModel.updateMinute(30)
        val mockUri = mockk<Uri>()
        viewModel.updateUri(mockUri)

        coEvery {
            eventRepository.addEvent(any(), any())
        } throws DatabaseException("Database error")
        coEvery { userRepository.getCurrentUser() } returns mockk(relaxed = true)

        viewModel.addEvent()

        coVerify { eventRepository.addEvent(any(), any()) }
        assertTrue(viewModel.saveState.value is AddEventViewModel.SaveState.Error)
        assertEquals(
            R.string.database_error,
            (viewModel.saveState.value as AddEventViewModel.SaveState.Error).messageId
        )
    }

    @Test
    fun `addEvent should correctly merge date and time into Timestamp`() = runTest {
        // 2024-06-20 in millis (arbitrary date)
        val selectedDate = 1718841600000L
        viewModel.updateDate(selectedDate)
        viewModel.updateHour(14)
        viewModel.updateMinute(30)

        // Setup other required fields
        viewModel.updateTitle("Title")
        viewModel.updateDescription("Desc")
        viewModel.updateCategory(Category.ART)
        viewModel.updateLocation("Loc")
        coEvery { userRepository.getCurrentUser() } returns mockk(relaxed = true)

        viewModel.addEvent()

        coVerify {
            eventRepository.addEvent(match { event ->
                val calendar = java.util.Calendar.getInstance().apply {
                    time = event.dateTime!!
                }
                calendar.get(java.util.Calendar.HOUR_OF_DAY) == 14 &&
                        calendar.get(java.util.Calendar.MINUTE) == 30
            }, any())
        }
    }

    @Test
    fun `resetSaveState should set state to Idle`() {
        viewModel.resetSaveState()
        assertEquals(AddEventViewModel.SaveState.Idle, viewModel.saveState.value)
    }
}
