package parcours.android.eventorias.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import parcours.android.eventorias.domain.model.User

const val USER_COLLECTION = "users"

class UserRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    fun getCurrentUser(): User? {
        val authUser = firebaseAuth.currentUser
        val user = authUser?.let {
            User(
                userId = it.uid,
                pictureUrl = it.photoUrl?.toString(),
                username = it.displayName ?: "",
            )
        }
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

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun deleteAccount(): Boolean {
        val authUser = firebaseAuth.currentUser ?: return false

        try {
            firestore.collection(USER_COLLECTION).document(authUser.uid)
                .delete().await()
            Log.i("TAG", "user deleted from Firestore")
            authUser.delete().await()
            Log.i("TAG", "user deleted from Firebase Auth")
            return true
        } catch (e: Exception) {
            Log.w("TAG", "user not deleted from Auth: ${e.message}")
            return false
        }
    }
}