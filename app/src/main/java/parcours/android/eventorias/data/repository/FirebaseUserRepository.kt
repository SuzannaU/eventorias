package parcours.android.eventorias.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import parcours.android.eventorias.data.datasource.UserDataSource
import parcours.android.eventorias.data.dto.toDomain
import parcours.android.eventorias.data.dto.toDto
import parcours.android.eventorias.domain.exceptions.AuthException
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.exceptions.UserNotFoundException
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.domain.repository.UserRepository


class FirebaseUserRepository(
    private val userDataSource: UserDataSource,
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
        return try {
            userDataSource.getCurrentUser()?.toDomain()
        } catch (e: Exception) {
            handleException(e, "Error during authentication")
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            userDataSource.getUserById(userId)?.toDomain()
        } catch (e: Exception) {
            handleException(e, "Error during user retrieval")
        }
    }

    override suspend fun createUser() {
        val user = getCurrentUser() ?: throw UserNotFoundException("Auth user not found")
        try {
            userDataSource.saveUser(user.toDto())
        } catch (e: Exception) {
            handleException(e, "Error during user creation")
        }
    }

    override suspend fun updateSubscriptionStatus(isSubscribed: Boolean) {
        val user = getCurrentUser() ?: throw UserNotFoundException("Auth user not found")
        val updatedUser = user.copy(subscribed = isSubscribed)

        try {
            userDataSource.saveUser(updatedUser.toDto())
        } catch (e: Exception) {
            handleException(e, "Error during user update")
        }
    }

    override fun signOut() {
        userDataSource.signOut()
    }

    private fun handleException(e: Exception, messagePrefix: String): Nothing {
        e.printStackTrace()
        val customException = when (e) {
            is FirebaseAuthException -> AuthException(e.message ?: "$messagePrefix (Auth)")
            is FirebaseNetworkException -> NetworkException(e.message ?: "$messagePrefix (Network)")
            is FirebaseFirestoreException -> DatabaseException(
                e.message ?: "$messagePrefix (Firestore)"
            )

            else -> Exception(e.message)
        }
        throw customException
    }
}
