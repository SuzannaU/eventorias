package parcours.android.eventorias.domain.service

import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val uid: String
)

interface AuthService {

    val authState: Flow<AuthUser?>
}