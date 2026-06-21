package parcours.android.eventorias.domain.model


data class User(
    val userId: String,
    val username: String? = null,
    val email: String? = null,
    val pictureUrl: String? = null,
    val subscribed: Boolean = true,
)