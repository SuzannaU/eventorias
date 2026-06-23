package parcours.android.eventorias

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.ui.screen.profile.ProfileScreen
import parcours.android.eventorias.ui.screen.profile.ProfileViewModel
import parcours.android.eventorias.ui.theme.EventoriasTheme

class ProfileInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileDetailsShouldBeDisplayed() {
        val mockUser = User(
            userId = "123",
            username = "John Doe",
            email = "john.doe@example.com",
            subscribed = true
        )
        val viewModel = mockk<ProfileViewModel>(relaxed = true)
        every { viewModel.profileScreenState } returns MutableStateFlow(ProfileViewModel.ProfileScreenState.UserFound(mockUser))
        every { viewModel.notificationsEnabled } returns MutableStateFlow(true)

        composeTestRule.setContent {
            EventoriasTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onEventsClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john.doe@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notifications switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notifications switch").assertIsOn()
        composeTestRule.onNodeWithTag("profile button").assertIsSelected()
    }

    @Test
    fun noUserFoundShouldDisplayMessage() {
        val viewModel = mockk<ProfileViewModel>(relaxed = true)
        every { viewModel.profileScreenState } returns MutableStateFlow(ProfileViewModel.ProfileScreenState.NoUserFound)
        every { viewModel.notificationsEnabled } returns MutableStateFlow(false)

        composeTestRule.setContent {
            EventoriasTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onEventsClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("User Not Found").assertIsDisplayed()
    }

    @Test
    fun errorDuringLoadingShouldDisplayErrorScreen() {
        val viewModel = mockk<ProfileViewModel>(relaxed = true)
        every { viewModel.profileScreenState } returns MutableStateFlow(ProfileViewModel.ProfileScreenState.Error(R.string.unknown_error))
        every { viewModel.notificationsEnabled } returns MutableStateFlow(false)

        composeTestRule.setContent {
            EventoriasTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onEventsClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("An unknown error occurred.").assertIsDisplayed()
    }
}
