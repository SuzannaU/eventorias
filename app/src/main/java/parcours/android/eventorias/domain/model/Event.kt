package parcours.android.eventorias.domain.model

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Event(
    @DocumentId
    val eventId: String = "",
    val author: User? = null,
    val title: String = "",
    val description: String? = null,
    val dateTime: Timestamp? = null,
    val location: String? = null,
    val pictureUrl: String? = null,
    val category: Category = Category.OTHER,
): Serializable

enum class Category { ART, TECH, CHARITY, SPORT, FOOD, OTHER }