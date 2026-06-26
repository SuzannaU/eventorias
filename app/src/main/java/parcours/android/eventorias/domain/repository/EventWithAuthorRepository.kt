package parcours.android.eventorias.domain.repository

import kotlinx.coroutines.flow.Flow
import parcours.android.eventorias.domain.model.EventWithAuthor

interface EventWithAuthorRepository {
    suspend fun getEventsWithAuthor(): Flow<List<EventWithAuthor>>
    suspend fun getEventByIdWithAuthor(eventId: String): EventWithAuthor?
}