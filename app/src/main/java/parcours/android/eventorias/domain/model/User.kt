package parcours.android.eventorias.domain.model

import java.io.Serializable

data class User(
    val userId: String = "",

    val pictureUrl: String = "",

    val username: String = "",
) : Serializable