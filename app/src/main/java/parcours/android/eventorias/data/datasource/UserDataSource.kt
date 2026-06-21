package parcours.android.eventorias.data.datasource

import parcours.android.eventorias.data.dto.UserDto

interface UserDataSource {
    suspend fun getCurrentUser(): UserDto?
    suspend fun saveUser(user: UserDto)
    fun signOut()
}
