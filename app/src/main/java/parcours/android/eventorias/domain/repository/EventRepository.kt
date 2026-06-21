package parcours.android.eventorias.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import parcours.android.eventorias.domain.model.Event

interface EventRepository {

    suspend fun getEventById(postId: String): Event?
    fun getEvents(): Flow<List<Event>>
    suspend fun addEvent(event: Event, pictureUri: Uri?)
}