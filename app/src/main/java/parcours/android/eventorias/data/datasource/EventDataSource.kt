package parcours.android.eventorias.data.datasource

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import parcours.android.eventorias.data.dto.EventDto

interface EventDataSource {
    suspend fun getEventById(eventId: String): EventDto?
    fun getEvents(): Flow<List<EventDto>>
    suspend fun saveEvent(event: EventDto)
    suspend fun uploadEventPicture(pictureUri: Uri): Uri
}
