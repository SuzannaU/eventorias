package parcours.android.eventorias.ui

import android.text.TextUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import parcours.android.eventorias.data.repository.FirebaseUserRepository
import parcours.android.eventorias.domain.exceptions.AuthException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.service.AuthService
import parcours.android.eventorias.domain.service.AuthUser

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val userRepository = mockk<FirebaseUserRepository>(relaxed = true)
    private val authService = mockk<AuthService>(relaxed = true)
    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setUp() {
        viewModel = MainViewModel(userRepository, authService)

        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun `init should observe auth state`() {
        every { authService.authState } returns flowOf(null)

        viewModel = MainViewModel(userRepository, authService)

        verify { authService.authState }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `when user is authenticated, uiState should reflect it`() = runTest {

        val mockUser = mockk<AuthUser>()
        every { mockUser.uid } returns "test_uid"
        every { authService.authState } returns flowOf(mockUser)
        viewModel = MainViewModel(userRepository, authService)

        val state = viewModel.uiState.value

        assertTrue(state.isUserAuthenticated)
        assertEquals("test_uid", state.userId)
        assertTrue(state.isAuthConnected)
        assertFalse(state.isLoading)
    }

    @Test
    fun `when user is NOT authenticated, uiState should reflect it`() = runTest {

        every { authService.authState } returns flowOf(null)
        viewModel = MainViewModel(userRepository, authService)

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
        coEvery { userRepository.createUser() } throws NetworkException("No network")

        viewModel.createUser()

        assertEquals(
            parcours.android.eventorias.R.string.network_error,
            viewModel.uiState.value.errorMessageId
        )
    }

    @Test
    fun `createUser auth failure should update errorMessageId`() = runTest {
        coEvery { userRepository.createUser() } throws AuthException("auth exception")

        viewModel.createUser()

        assertEquals(
            parcours.android.eventorias.R.string.auth_error,
            viewModel.uiState.value.errorMessageId
        )
    }
}
