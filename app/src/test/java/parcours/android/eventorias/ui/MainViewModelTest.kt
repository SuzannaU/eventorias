package parcours.android.eventorias.ui

import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import parcours.android.eventorias.data.UserRepository

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setUp() {
        viewModel = MainViewModel(userRepository, firebaseAuth)

        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun `init should observe auth state`() {
        verify { firebaseAuth.addAuthStateListener(any()) }
    }

    @Test
    fun `when user is authenticated, uiState should reflect it`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        verify { firebaseAuth.addAuthStateListener(capture(listenerSlot)) }

        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "test_uid"
        every { firebaseAuth.currentUser } returns mockUser

        listenerSlot.captured.onAuthStateChanged(firebaseAuth)

        val state = viewModel.uiState.value
        assertTrue(state.isUserAuthenticated)
        assertEquals("test_uid", state.userId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `when user is NOT authenticated, uiState should reflect it`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        verify { firebaseAuth.addAuthStateListener(capture(listenerSlot)) }

        every { firebaseAuth.currentUser } returns null

        listenerSlot.captured.onAuthStateChanged(firebaseAuth)

        val state = viewModel.uiState.value
        assertFalse(state.isUserAuthenticated)
        assertEquals(null, state.userId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `createUser should call repository`() = runTest {
        viewModel.createUser()
        coVerify { userRepository.createUser() }
    }

    @Test
    fun `createUser network failure should update errorMessageId`() = runTest {
        io.mockk.coEvery { userRepository.createUser() } throws com.google.firebase.FirebaseNetworkException("No network")

        viewModel.createUser()

        assertEquals(parcours.android.eventorias.R.string.network_error, viewModel.uiState.value.errorMessageId)
    }

    @Test
    fun `createUser auth failure should update errorMessageId`() = runTest {
        io.mockk.coEvery { userRepository.createUser() } throws mockk<com.google.firebase.auth.FirebaseAuthException>(relaxed = true)

        viewModel.createUser()

        assertEquals(parcours.android.eventorias.R.string.auth_error, viewModel.uiState.value.errorMessageId)
    }
}
