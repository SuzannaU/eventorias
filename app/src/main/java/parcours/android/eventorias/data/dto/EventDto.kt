package parcours.android.eventorias.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.domain.model.Event
import java.io.Serializable

data class EventDto(
    @DocumentId
    val eventId: String = "",
    val authorId: String? = null,
    val title: String = "",
    val description: String? = null,
    val dateTime: Timestamp? = null,
    var coordinates: GeoPoint? = null,
    val pictureUrl: String? = null,
    val category: Category = Category.OTHER,
) : Serializable

fun EventDto.toDomain(address: String? = null) = Event(
    eventId = eventId,
    authorId = authorId,
    title = title,
    description = description,
    dateTime = dateTime?.toDate(),
    location = address,
    pictureUrl = pictureUrl,
    category = category
)

fun Event.toDto(coordinates: Pair<Double, Double>? = null) = EventDto(
    eventId = eventId,
    authorId = authorId,
    title = title,
    description = description,
    dateTime = dateTime?.let { Timestamp(it) },
    coordinates = coordinates?. let { GeoPoint(coordinates.first, coordinates.second) },
    pictureUrl = pictureUrl,
    category = category
)
