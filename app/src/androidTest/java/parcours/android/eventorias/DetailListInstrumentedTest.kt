package parcours.android.eventorias

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.domain.model.EventWithAuthor
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.ui.screen.detail.DetailScreen
import parcours.android.eventorias.ui.screen.detail.DetailViewModel
import parcours.android.eventorias.ui.theme.EventoriasTheme
import java.util.Date

class DetailListInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun eventDetailsShouldBeDisplayed() {
        val mockEvent = Event(
            title = "Awesome Concert",
            description = "A very cool concert description.",
            location = "Paris, France",
            dateTime = Date()
        )
        val mockUser = User(
            userId = "user123",
        )
        val mockEventWithAuthor = EventWithAuthor(mockEvent, mockUser)
        val viewModel = mockk<DetailViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DetailViewModel.DetailUiState.Success(mockEventWithAuthor))

        composeTestRule.setContent {
            EventoriasTheme {
                DetailScreen(
                    viewModel = viewModel,
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Awesome Concert").assertIsDisplayed()
        composeTestRule.onNodeWithText("A very cool concert description.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paris, France")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun noEventShouldDisplayMessage() {
        val viewModel = mockk<DetailViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DetailViewModel.DetailUiState.Error(R.string.event_not_found))

        composeTestRule.setContent {
            EventoriasTheme {
                DetailScreen(
                    viewModel = viewModel,
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Event not found").assertIsDisplayed()
        composeTestRule.onNodeWithText("No event").assertIsDisplayed()
    }

    @Test
    fun errorDuringLoadingShouldDisplayErrorScreen() {
        val viewModel = mockk<DetailViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(DetailViewModel.DetailUiState.Error(R.string.unknown_error))

        composeTestRule.setContent {
            EventoriasTheme {
                DetailScreen(
                    viewModel = viewModel,
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("An unknown error occurred.").assertIsDisplayed()
    }
}
