package parcours.android.eventorias.domain.repository

import parcours.android.eventorias.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun createUser()
    suspend fun updateSubscriptionStatus(isSubscribed: Boolean)
    fun signOut()
}