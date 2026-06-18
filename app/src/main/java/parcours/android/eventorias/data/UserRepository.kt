package parcours.android.eventorias.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import parcours.android.eventorias.domain.model.User

const val USER_COLLECTION = "users"

class UserRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    suspend fun getCurrentUser(): User? {
        val authUser = firebaseAuth.currentUser
        val uid = authUser?.uid ?: return null
        val user = firestore.collection(USER_COLLECTION).document(uid)
                .get()
                .await()
                .toObject<User>()
        return user
    }

    suspend fun createUser(): Boolean {
        val user = getCurrentUser() ?: return false
        val uid = user.userId

        try {
            firestore.collection(USER_COLLECTION).document(uid)
                .set(user).await()
            Log.i("TAG", "user inserted in firestore")
            return true
        } catch (e: Exception) {
            Log.w("TAG", "user NOT inserted in firestore")
            return false
        }
    }

    suspend fun updateSubscriptionStatus(isSubscribed: Boolean): Boolean {
        val user = getCurrentUser() ?: return false
        val uid = user.userId
        val updatedUser = user.copy(subscribed = isSubscribed)

        try {
            firestore.collection(USER_COLLECTION).document(uid)
                .set(updatedUser).await()
            Log.i("TAG", "user updated in firestore")
            return true
        } catch (e: Exception) {
            Log.w("TAG", "user NOT inserted in firestore")
            return false
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}