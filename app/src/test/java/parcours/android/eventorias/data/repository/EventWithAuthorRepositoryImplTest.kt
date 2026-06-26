package parcours.android.eventorias.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.domain.repository.EventRepository
import parcours.android.eventorias.domain.repository.UserRepository

class EventWithAuthorRepositoryImplTest {

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private lateinit var repository: EventWithAuthorRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = EventWithAuthorRepositoryImpl(eventRepository, userRepository)
    }

    @Test
    fun `getEventsWithAuthor should return list of EventWithAuthor`() = runTest {
        val user1 = User(userId = "user1", username = "Author 1")
        val event1 = Event(
            eventId = "event1",
            authorId = "user1",
            title = "Event 1",
            category = Category.ART
        )
        val event2 = Event(
            eventId = "event2",
            authorId = "user2",
            title = "Event 2",
            category = Category.TECH
        )

        every { eventRepository.getEvents() } returns flowOf(listOf(event1, event2))
        coEvery { userRepository.getUserById("user1") } returns user1
        coEvery { userRepository.getUserById("user2") } returns null

        val result = repository.getEventsWithAuthor().first()

        assertEquals(2, result.size)
        assertEquals(event1, result[0].event)
        assertEquals(user1, result[0].author)

        assertEquals(event2, result[1].event)
        assertEquals("unknown user", result[1].author.userId)
    }

    @Test
    fun `getEventByIdWithAuthor should return EventWithAuthor when event and author exist`() =
        runTest {
            val userId = "user1"
            val eventId = "event1"
            val user = User(userId = userId, username = "Author 1")
            val event = Event(eventId = eventId, authorId = userId, title = "Event 1")

            coEvery { eventRepository.getEventById(eventId) } returns event
            coEvery { userRepository.getUserById(userId) } returns user

            val result = repository.getEventByIdWithAuthor(eventId)

            assertEquals(event, result?.event)
            assertEquals(user, result?.author)
        }

    @Test
    fun `getEventByIdWithAuthor should return EventWithAuthor with unknown user when author does not exist`() =
        runTest {
            val userId = "user1"
            val eventId = "event1"
            val event = Event(eventId = eventId, authorId = userId, title = "Event 1")

            coEvery { eventRepository.getEventById(eventId) } returns event
            coEvery { userRepository.getUserById(userId) } returns null

            val result = repository.getEventByIdWithAuthor(eventId)

            assertEquals(event, result?.event)
            assertEquals("unknown user", result?.author?.userId)
        }

    @Test
    fun `getEventByIdWithAuthor should return null when event does not exist`() = runTest {
        val eventId = "nonexistent"
        coEvery { eventRepository.getEventById(eventId) } returns null

        val result = repository.getEventByIdWithAuthor(eventId)

        assertNull(result)
    }

    @Test
    fun `getEventByIdWithAuthor should return null when authorId is null`() = runTest {
        val eventId = "event1"
        val event = Event(eventId = eventId, authorId = null, title = "Event 1")
        coEvery { eventRepository.getEventById(eventId) } returns event

        val result = repository.getEventByIdWithAuthor(eventId)

        assertNull(result)
    }
}
