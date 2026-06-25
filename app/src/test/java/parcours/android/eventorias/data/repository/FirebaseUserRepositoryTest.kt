package parcours.android.eventorias.data.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import parcours.android.eventorias.data.datasource.UserDataSource
import parcours.android.eventorias.data.dto.UserDto
import parcours.android.eventorias.domain.exceptions.AuthException
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.exceptions.UserNotFoundException

class FirebaseUserRepositoryTest {

    private val userDataSource = mockk<UserDataSource>()
    private lateinit var userRepository: FirebaseUserRepository

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        
        userRepository = FirebaseUserRepository(userDataSource)
    }

    @Test
    fun `getCurrentUser returns User when datasource returns UserDto`() = runTest {
        val userDto = UserDto(userId = "123", username = "testuser", email = "test@test.com")
        coEvery { userDataSource.getCurrentUser() } returns userDto

        val result = userRepository.getCurrentUser()

        assertEquals("123", result?.userId)
        assertEquals("testuser", result?.username)
        assertEquals("test@test.com", result?.email)
    }

    @Test
    fun `getCurrentUser returns null when datasource returns null`() = runTest {
        coEvery { userDataSource.getCurrentUser() } returns null

        val result = userRepository.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun `getUserById returns User when datasource returns UserDto`() = runTest {
        val userDto = UserDto(userId = "123", username = "testuser", email = "test@test.com")
        coEvery { userDataSource.getUserById(any()) } returns userDto

        val result = userRepository.getUserById("123")

        assertEquals("123", result?.userId)
        assertEquals("testuser", result?.username)
        assertEquals("test@test.com", result?.email)
    }

    @Test
    fun `getUserById returns null when datasource returns null`() = runTest {
        coEvery { userDataSource.getUserById(any()) } returns null

        val result = userRepository.getUserById("123")

        assertNull(result)
    }

    @Test
    fun `getCurrentUser throws AuthException when FirebaseAuthException occurs`() = runTest {
        val firebaseAuthException = mockk<FirebaseAuthException>(relaxed = true)
        coEvery { firebaseAuthException.message } returns "Auth failed"
        coEvery { userDataSource.getCurrentUser() } throws firebaseAuthException

        assertThrows<AuthException> {
            userRepository.getCurrentUser()
        }
    }

    @Test
    fun `getCurrentUser throws NetworkException when FirebaseNetworkException occurs`() = runTest {
        val networkException = mockk<FirebaseNetworkException>(relaxed = true)
        coEvery { networkException.message } returns "Network error"
        coEvery { userDataSource.getCurrentUser() } throws networkException

        assertThrows<NetworkException> {
            userRepository.getCurrentUser()
        }
    }

    @Test
    fun `getCurrentUser throws Exception when otherException occurs`() = runTest {
        val exception = mockk<Exception>(relaxed = true)
        coEvery { exception.message } returns "Unknown error"
        coEvery { userDataSource.getCurrentUser() } throws exception

        assertThrows<Exception> {
            userRepository.getCurrentUser()
        }
    }

    @Test
    fun `createUser calls userDataSource when user is authenticated`() = runTest {
        val userDto = UserDto(userId = "123", username = "testuser")
        coEvery { userDataSource.getCurrentUser() } returns userDto
        coEvery { userDataSource.saveUser(any()) } returns Unit

        userRepository.createUser()

        coVerify { userDataSource.saveUser(userDto) }
    }

    @Test
    fun `createUser throws UserNotFoundException when not authenticated`() = runTest {
        coEvery { userDataSource.getCurrentUser() } returns null

        assertThrows<UserNotFoundException> {
            userRepository.createUser()
        }
    }

    @Test
    fun `updateSubscriptionStatus success`() = runTest {
        val userDto = UserDto(userId = "123", subscribed = false)
        coEvery { userDataSource.getCurrentUser() } returns userDto
        coEvery { userDataSource.saveUser(any()) } returns Unit

        userRepository.updateSubscriptionStatus(true)

        coVerify { userDataSource.saveUser(userDto.copy(subscribed = true)) }
    }

    @Test
    fun `updateSubscriptionStatus throws DatabaseException when FirebaseFirestoreException occurs`() = runTest {
        val userDto = UserDto(userId = "123")
        coEvery { userDataSource.getCurrentUser() } returns userDto
        val firestoreException = mockk<FirebaseFirestoreException>(relaxed = true)
        coEvery { firestoreException.message } returns "Firestore error"
        coEvery { userDataSource.saveUser(any()) } throws firestoreException

        assertThrows<DatabaseException> {
            userRepository.updateSubscriptionStatus(true)
        }
    }

    @Test
    fun `signOut calls datasource signOut`() {
        coEvery { userDataSource.signOut() } returns Unit

        userRepository.signOut()

        verify { userDataSource.signOut() }
    }
}
