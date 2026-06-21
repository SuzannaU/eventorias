package parcours.android.eventorias.data.dto

import com.google.firebase.firestore.DocumentId
import parcours.android.eventorias.domain.model.User
import java.io.Serializable

data class UserDto(
    @DocumentId
    val userId: String = "",
    val username: String? = null,
    val email: String? = null,
    val pictureUrl: String? = null,
    val subscribed: Boolean = true,
) : Serializable

fun UserDto.toDomain() = User(
    userId = userId,
    username = username,
    email = email,
    pictureUrl = pictureUrl,
    subscribed = subscribed
)

fun User.toDto() = UserDto(
    userId = userId,
    username = username,
    email = email,
    pictureUrl = pictureUrl,
    subscribed = subscribed
)
