package parcours.android.eventorias.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import parcours.android.eventorias.domain.model.EventWithAuthor
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.domain.repository.EventRepository
import parcours.android.eventorias.domain.repository.EventWithAuthorRepository
import parcours.android.eventorias.domain.repository.UserRepository

class EventWithAuthorRepositoryImpl(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
) : EventWithAuthorRepository {

    override suspend fun getEventsWithAuthor(): Flow<List<EventWithAuthor>> {
        return eventRepository.getEvents().map { events ->
            coroutineScope {
                events.map { event ->
                    async {
                        val author = event.authorId?.let { userRepository.getUserById(it) }
                        EventWithAuthor(
                            event = event,
                            author = author ?: User(userId = "unknown user")
                        )
                    }
                }.awaitAll()
            }
        }
    }

    override suspend fun getEventByIdWithAuthor(eventId: String): EventWithAuthor? {
        val event = eventRepository.getEventById(eventId)
        event?.authorId?. let {
            val author = userRepository.getUserById(event.authorId)
            return EventWithAuthor(
                event = event,
                author = author ?: User(userId = "unknown user")
            )
        }
        return null
    }
}