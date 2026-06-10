package parcours.android.eventorias.domain.model

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class User(
    @DocumentId
    val userId: String = "",
    val username: String? = null,
    val email: String? = null,
    val pictureUrl: String? = null,
    val subscribed: Boolean = true,
) : Serializable