package parcours.android.eventorias.ui.screen.profile

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import parcours.android.eventorias.R
import parcours.android.eventorias.data.repository.FirebaseUserRepository
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.domain.service.NotificationService
import parcours.android.eventorias.ui.DispatcherProvider
import parcours.android.eventorias.ui.MainDispatcherExtension

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val dispatcherProvider = mockk<DispatcherProvider>()
    private val userRepository = mockk<FirebaseUserRepository>(relaxed = true)
    private val notificationService = mockk<NotificationService>(relaxed = true)
    private lateinit var viewModel: ProfileViewModel

    @BeforeEach

    fun setUp() {
        every { dispatcherProvider.io } returns mainDispatcherExtension.testDispatcher
        every { dispatcherProvider.main } returns mainDispatcherExtension.testDispatcher

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun `loadUserProfile success should update state with user`() = runTest {
        val mockUser = mockk<User>(relaxed = true)
        coEvery { userRepository.getCurrentUser() } returns mockUser

        viewModel = ProfileViewModel(dispatcherProvider, userRepository, notificationService)

        assertTrue(viewModel.profileScreenState.value is ProfileViewModel.ProfileScreenState.UserFound)
        assertEquals(mockUser, (viewModel.profileScreenState.value as ProfileViewModel.ProfileScreenState.UserFound).user)
    }
    
    @Test
    fun `loadUserProfile failure should update state to NoUserFound`() = runTest {
        coEvery { userRepository.getCurrentUser() } returns null

        viewModel = ProfileViewModel(dispatcherProvider, userRepository, notificationService)

        assertTrue(viewModel.profileScreenState.value is ProfileViewModel.ProfileScreenState.NoUserFound)
    }

    @Test
    fun `loadUserProfile network exception should update state to error`() = runTest {
        coEvery { userRepository.getCurrentUser() } throws NetworkException("No network")

        viewModel = ProfileViewModel(dispatcherProvider, userRepository, notificationService)

        assertTrue(viewModel.profileScreenState.value is ProfileViewModel.ProfileScreenState.Error)
        assertEquals(R.string.network_error, (viewModel.profileScreenState.value as ProfileViewModel.ProfileScreenState.Error).errorMessageId)
    }

    @Test
    fun `loadUserProfile database exception should update state to error`() = runTest {
        coEvery { userRepository.getCurrentUser() } throws DatabaseException("Database exception")

        viewModel = ProfileViewModel(dispatcherProvider, userRepository, notificationService)

        assertTrue(viewModel.profileScreenState.value is ProfileViewModel.ProfileScreenState.Error)
        assertEquals(R.string.database_error, (viewModel.profileScreenState.value as ProfileViewModel.ProfileScreenState.Error).errorMessageId)
    }

    @Test
    fun `onNotificationsToggle true should subscribe to topic`() = runTest {
        viewModel = ProfileViewModel(dispatcherProvider, userRepository, notificationService)
        
        viewModel.onNotificationsToggle(true)

        verify { notificationService.subscribeToTopic(any()) }
        coVerify { userRepository.updateSubscriptionStatus(true) }
        assertTrue(viewModel.notificationsEnabled.value)
    }
    
    @Test
    fun `onNotificationsToggle false should unsubscribe from topic`() = runTest {
        viewModel = ProfileViewModel(dispatcherProvider, userRepository, notificationService)
        
        viewModel.onNotificationsToggle(false)

        verify { notificationService.unsubscribeFromTopic(any()) }
        coVerify { userRepository.updateSubscriptionStatus(false) }
        assertTrue(!viewModel.notificationsEnabled.value)
    }

    @Test
    fun `signOut should call repository signOut`() = runTest {
        viewModel = ProfileViewModel(dispatcherProvider, userRepository, notificationService)
        
        viewModel.signOut()

        verify { userRepository.signOut() }
    }
}
