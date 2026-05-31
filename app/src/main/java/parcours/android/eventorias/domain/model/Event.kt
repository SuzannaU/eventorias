package parcours.android.eventorias.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Event(
    @DocumentId
    val eventId: String = "",
    val author: User? = null,
    val title: String = "",
    val dateTime: Timestamp? = null,
    val description: String? = null,
    val pictureUrl: String? = null,
    val location: String? = null,
): Serializable