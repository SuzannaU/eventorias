package parcours.android.eventorias.data

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import parcours.android.eventorias.domain.exceptions.AuthException
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.exceptions.UserNotFoundException
import parcours.android.eventorias.domain.model.User

const val USER_COLLECTION = "users"
private const val TAG = "TAG UserRepository"

class UserRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    suspend fun getCurrentUser(): User? {
        try {
            val authUser = firebaseAuth.currentUser
            val uid = authUser?.uid ?: return null
            val user = firestore.collection(USER_COLLECTION).document(uid)
                .get()
                .await()
                .toObject<User>()
            return user
        } catch (e: Exception) {
            e.printStackTrace()
            val customException = when (e) {
                is FirebaseAuthException -> AuthException(e.message ?: "Auth Error during authentication")
                is FirebaseNetworkException -> NetworkException(e.message ?: "Network error during authentication")
                is FirebaseFirestoreException -> DatabaseException(e.message ?: "Firestore Error during user update")
                else -> Exception(e.message)
            }
            throw customException
        }
    }

    suspend fun createUser() {
        val user = getCurrentUser() ?: throw UserNotFoundException("Auth user not found")
        val uid = user.userId

        try {
            firestore.collection(USER_COLLECTION).document(uid)
                .set(user).await()
        } catch (e: Exception) {
            e.printStackTrace()
            val customException = when (e) {
                is FirebaseAuthException -> AuthException(e.message ?: "Auth Error during user creation")
                is FirebaseNetworkException -> NetworkException(e.message ?: "Network error during user creation")
                is FirebaseFirestoreException -> DatabaseException(e.message ?: "Firestore Error during user update")
                else -> Exception(e.message)
            }
            throw customException
        }
    }

    suspend fun updateSubscriptionStatus(isSubscribed: Boolean) {
        val user = getCurrentUser() ?: throw UserNotFoundException("Auth user not found")
        val uid = user.userId
        val updatedUser = user.copy(subscribed = isSubscribed)

        try {
            firestore.collection(USER_COLLECTION).document(uid)
                .set(updatedUser).await()
            Log.i(TAG, "user updated in firestore")
        } catch (e: Exception) {
            Log.w(TAG, "user NOT updated in firestore")
            e.printStackTrace()
            val customException = when (e) {
                is FirebaseAuthException -> AuthException(e.message ?: "Auth Error during user update")
                is FirebaseNetworkException -> NetworkException(e.message ?: "Network error during user update")
                is FirebaseFirestoreException -> DatabaseException(e.message ?: "Firestore Error during user update")
                else -> Exception(e.message)
            }
            throw customException
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}