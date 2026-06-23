package parcours.android.eventorias.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import parcours.android.eventorias.data.datasource.EventDataSource
import parcours.android.eventorias.data.dto.EventDto
import parcours.android.eventorias.data.service.LocationService
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Event

class FirebaseEventRepositoryTest {

    private val eventDataSource = mockk<EventDataSource>()
    private val locationService = mockk<LocationService>()
    private lateinit var eventRepository: FirebaseEventRepository

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        
        eventRepository = FirebaseEventRepository(eventDataSource, locationService)
    }

    @Test
    fun `getEventById returns Event when found`() = runTest {
        val eventDto = EventDto(eventId = "1", title = "Test Event")
        coEvery { eventDataSource.getEventById("1") } returns eventDto

        val result = eventRepository.getEventById("1")

        assertEquals("1", result?.eventId)
        assertEquals("Test Event", result?.title)
    }

    @Test
    fun `getEventById returns null when not found`() = runTest {
        coEvery { eventDataSource.getEventById("1") } returns null

        val result = eventRepository.getEventById("1")

        assertNull(result)
    }

    @Test
    fun `getEventById throws NetworkException when FirebaseNetworkException occurs`() = runTest {
        val networkException = mockk<FirebaseNetworkException>(relaxed = true)
        coEvery { networkException.message } returns "Network error"
        coEvery { eventDataSource.getEventById(any()) } throws networkException

        assertThrows<NetworkException> {
            eventRepository.getEventById("1")
        }
    }

    @Test
    fun `getEventById throws DatabaseException when FirebaseFirestoreException occurs`() = runTest {
        val firestoreException = mockk<FirebaseFirestoreException>(relaxed = true)
        coEvery { firestoreException.message } returns "Firestore error"
        coEvery { eventDataSource.getEventById(any()) } throws firestoreException

        assertThrows<DatabaseException> {
            eventRepository.getEventById("1")
        }
    }

    @Test
    fun `getEvents returns list of Events`() = runTest {
        val eventDtos = listOf(EventDto(eventId = "1"), EventDto(eventId = "2"))
        coEvery { eventDataSource.getEvents() } returns flowOf(eventDtos)

        val result = eventRepository.getEvents().first()

        assertEquals(2, result.size)
        assertEquals("1", result[0].eventId)
        assertEquals("2", result[1].eventId)
    }

    @Test
    fun `addEvent without picture calls saveEvent`() = runTest {
        val event = Event(title = "New Event")
        coEvery { eventDataSource.saveEvent(any()) } returns Unit

        eventRepository.addEvent(event, null)

        coVerify { eventDataSource.saveEvent(match { it.title == "New Event" }) }
        coVerify(exactly = 0) { eventDataSource.uploadEventPicture(any()) }
    }

    @Test
    fun `addEvent with picture uploads then saves`() = runTest {
        val event = Event(title = "New Event")
        val mockUri = mockk<Uri>()
        val downloadUri = mockk<Uri>()
        every { downloadUri.toString() } returns "https://photo.url"
        
        coEvery { eventDataSource.uploadEventPicture(mockUri) } returns downloadUri
        coEvery { eventDataSource.saveEvent(any()) } returns Unit

        eventRepository.addEvent(event, mockUri)

        coVerify { eventDataSource.uploadEventPicture(mockUri) }
        coVerify { eventDataSource.saveEvent(match { it.pictureUrl == "https://photo.url" }) }
    }
}
