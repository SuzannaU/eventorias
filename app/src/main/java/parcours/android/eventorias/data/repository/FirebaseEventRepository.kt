package parcours.android.eventorias.data.repository

import android.net.Uri
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import parcours.android.eventorias.data.datasource.EventDataSource
import parcours.android.eventorias.data.dto.toDomain
import parcours.android.eventorias.data.dto.toDto
import parcours.android.eventorias.data.service.LocationService
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.repository.EventRepository

class FirebaseEventRepository(
    private val eventDataSource: EventDataSource,
    private val locationService: LocationService,
) : EventRepository {

    override suspend fun getEventById(postId: String): Event? {
        return try {
            eventDataSource.getEventById(postId)?.let { eventDto ->
                val coordinates = eventDto.coordinates?.let { Pair(it.latitude, it.longitude) }
                eventDto.toDomain(locationService.getEventAddress(coordinates))
            }
        } catch (e: Exception) {
            handleException(e, "Error fetching event")
        }
    }

    override fun getEvents(): Flow<List<Event>> {
        return try {
            eventDataSource.getEvents().map { entities ->
                entities.map { entity ->
                    entity.toDomain(
                        locationService
                            .getEventAddress(entity.coordinates?.let {
                                Pair(
                                    it.latitude,
                                    it.longitude
                                )
                            })
                    )
                }
            }
        } catch (e: Exception) {
            handleException(e, "Error fetching events")
        }
    }

    override suspend fun addEvent(event: Event, pictureUri: Uri?) {
        try {
            var eventToSave = event
            if (pictureUri != null) {
                val downloadUri = eventDataSource.uploadEventPicture(pictureUri)
                eventToSave = eventToSave.copy(pictureUrl = downloadUri.toString())
            }

            val coordinates = event.location?.let {
                locationService.getEventCoordinates(it)
            }

            eventDataSource.saveEvent(eventToSave.toDto(coordinates))
        } catch (e: Exception) {
            handleException(e, "Error adding event")
        }
    }

    private fun handleException(e: Exception, messagePrefix: String): Nothing {
        e.printStackTrace()
        val customException = when (e) {
            is FirebaseNetworkException -> NetworkException(
                e.message ?: "$messagePrefix (Network)"
            )

            is FirebaseFirestoreException -> DatabaseException(
                e.message ?: "$messagePrefix (Firestore)"
            )

            else -> Exception(e.message)
        }
        throw customException
    }
}
