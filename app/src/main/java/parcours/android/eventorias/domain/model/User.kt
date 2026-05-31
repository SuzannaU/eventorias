package parcours.android.eventorias.domain.model

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class User(
    @DocumentId
    val userId: String = "",
    val pictureUrl: String = "",
    val username: String = "",
) : Serializable