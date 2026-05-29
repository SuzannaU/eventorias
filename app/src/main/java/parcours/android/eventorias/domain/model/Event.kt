package parcours.android.eventorias.domain.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class Event(
    val eventId: String = "",
    val author: User? = null,
    val title: String = "",
    val dateTime: Timestamp? = null,
    val description: String? = null,
    val pictureUrl: String? = null,
    val location: String? = null,
): Serializable