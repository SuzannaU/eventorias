package parcours.android.eventorias.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.domain.model.Event
import java.io.Serializable

data class EventDto(
    @DocumentId
    val eventId: String = "",
    val author: UserDto? = null,
    val title: String = "",
    val description: String? = null,
    val dateTime: Timestamp? = null,
    val location: String? = null,
    val pictureUrl: String? = null,
    val category: Category = Category.OTHER,
) : Serializable

fun EventDto.toDomain() = Event(
    eventId = eventId,
    author = author?.toDomain(),
    title = title,
    description = description,
    dateTime = dateTime?.toDate(),
    location = location,
    pictureUrl = pictureUrl,
    category = category
)

fun Event.toDto() = EventDto(
    eventId = eventId,
    author = author?.toDto(),
    title = title,
    description = description,
    dateTime = dateTime?.let { Timestamp(it) },
    location = location,
    pictureUrl = pictureUrl,
    category = category
)
