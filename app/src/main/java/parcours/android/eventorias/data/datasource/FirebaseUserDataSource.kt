package parcours.android.eventorias.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import parcours.android.eventorias.data.dto.UserDto

private const val USER_COLLECTION = "users"

class FirebaseUserDataSource(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : UserDataSource {

    override suspend fun getCurrentUser(): UserDto? {
        val authUser = firebaseAuth.currentUser
        val uid = authUser?.uid ?: return null
        return firestore.collection(USER_COLLECTION).document(uid)
            .get()
            .await()
            .toObject<UserDto>()
    }

    override suspend fun getUserById(userId: String): UserDto? {
        return firestore.collection(USER_COLLECTION).document(userId)
            .get()
            .await()
            .toObject<UserDto>()
    }

    override suspend fun saveUser(user: UserDto) {
        firestore.collection(USER_COLLECTION).document(user.userId)
            .set(user).await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
