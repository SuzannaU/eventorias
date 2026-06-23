package parcours.android.eventorias

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import parcours.android.eventorias.ui.screen.add.AddEventScreen
import parcours.android.eventorias.ui.screen.add.AddEventViewModel
import parcours.android.eventorias.ui.theme.EventoriasTheme

class AddEventInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addEventFieldsShouldBeDisplayed() {
        val viewModel = mockk<AddEventViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(AddEventViewModel.FromUiState())
        every { viewModel.saveState } returns MutableStateFlow(AddEventViewModel.SaveState.Idle)

        composeTestRule.setContent {
            EventoriasTheme {
                AddEventScreen(
                    viewModel = viewModel,
                    onBackClick = {},
                    onSaveSuccessful = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("Address").assertIsDisplayed()

        composeTestRule.onNodeWithText("Validate").assertIsDisplayed()
    }

    @Test
    fun validationErrorsShouldBeDisplayed() {
        val viewModel = mockk<AddEventViewModel>(relaxed = true)
        val errorState = AddEventViewModel.FormErrorState(
            titleError = true,
            descriptionError = true,
            locationError = true
        )
        every { viewModel.uiState } returns MutableStateFlow(AddEventViewModel.FromUiState(formErrors = errorState))
        every { viewModel.saveState } returns MutableStateFlow(AddEventViewModel.SaveState.Idle)

        composeTestRule.setContent {
            EventoriasTheme {
                AddEventScreen(
                    viewModel = viewModel,
                    onBackClick = {},
                    onSaveSuccessful = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location is required").assertIsDisplayed()
    }

    @Test
    fun errorDuringSavingShouldDisplayErrorScreen() {
        val viewModel = mockk<AddEventViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(AddEventViewModel.FromUiState())
        every { viewModel.saveState } returns MutableStateFlow(AddEventViewModel.SaveState.Error(R.string.unknown_error))

        composeTestRule.setContent {
            EventoriasTheme {
                AddEventScreen(
                    viewModel = viewModel,
                    onBackClick = {},
                    onSaveSuccessful = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("An unknown error occurred.").assertIsDisplayed()
    }
}
