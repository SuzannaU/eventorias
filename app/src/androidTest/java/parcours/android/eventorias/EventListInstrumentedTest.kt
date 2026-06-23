package parcours.android.eventorias

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.ui.screen.list.ListScreen
import parcours.android.eventorias.ui.screen.list.ListViewModel
import parcours.android.eventorias.ui.theme.EventoriasTheme

class EventListInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadedEventsShouldBeDisplayed() {
        val fakeEvents = listOf(
            Event(title = "Art exhibition", eventId = "1"),
            Event(title = "Tech conference", eventId = "2"),
        )

        val viewModel = mockk<ListViewModel>(relaxed = true)
        every { viewModel.listScreenState } returns MutableStateFlow(ListViewModel.ListScreenState.EventsLoaded(fakeEvents))
        every { viewModel.searchQuery } returns MutableStateFlow("")

        composeTestRule.setContent {
            EventoriasTheme {
                ListScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onProfileClick = {},
                    onEventClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Art exhibition").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tech conference").assertIsDisplayed()
        composeTestRule.onNodeWithTag("events button").assertIsSelected()
    }

    @Test
    fun noLoadedEventsShouldDisplayMessage() {

        val viewModel = mockk<ListViewModel>(relaxed = true)
        every { viewModel.listScreenState } returns MutableStateFlow(ListViewModel.ListScreenState.NoEvents)
        every { viewModel.searchQuery } returns MutableStateFlow("")

        composeTestRule.setContent {
            EventoriasTheme {
                ListScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onProfileClick = {},
                    onEventClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No events found").assertIsDisplayed()
    }

    @Test
    fun errorDuringLoadingShouldDisplayErrorScreen() {

        val viewModel = mockk<ListViewModel>(relaxed = true)
        every { viewModel.listScreenState } returns MutableStateFlow(ListViewModel.ListScreenState.Error(R.string.unknown_error))
        every { viewModel.searchQuery } returns MutableStateFlow("")

        composeTestRule.setContent {
            EventoriasTheme {
                ListScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onProfileClick = {},
                    onEventClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("An unknown error occurred.").assertIsDisplayed()
    }

    @Test
    fun searchFieldInputShouldTriggerViewModel() {
        val fakeEvents = listOf(
            Event(title = "Art exhibition", eventId = "1"),
            Event(title = "Tech conference", eventId = "2"),
        )

        val viewModel = mockk<ListViewModel>(relaxed = true)
        every { viewModel.listScreenState } returns MutableStateFlow(ListViewModel.ListScreenState.EventsLoaded(fakeEvents))
        every { viewModel.searchQuery } returns MutableStateFlow("")

        composeTestRule.setContent {
            EventoriasTheme {
                ListScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onProfileClick = {},
                    onEventClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("search field").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("search button").performClick()
        composeTestRule.onNodeWithTag("search field").performTextInput("conf")

        verify { viewModel.onSearchQueryChange("conf")}
    }

    @Test
    fun sortingMenuShouldTriggerViewModel() {
        val viewModel = mockk<ListViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow(ListViewModel.ListScreenState.EventsLoaded(emptyList()))
        val searchQueryFlow = MutableStateFlow("")

        every { viewModel.listScreenState } returns stateFlow
        every { viewModel.searchQuery } returns searchQueryFlow
        every { viewModel.sortOptions } returns listOf(
            R.string.date_soonest_first,
            R.string.date_latest_first,
            R.string.category_a_z,
            R.string.category_z_a,
        )

        composeTestRule.setContent {
            EventoriasTheme {
                ListScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onProfileClick = {},
                    onEventClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("sorting menu").assertIsNotDisplayed()

        composeTestRule.onNodeWithContentDescription("Sort").performClick()
        composeTestRule.onNodeWithText("Date (Latest first)").performClick()
        composeTestRule.onNodeWithTag("sorting menu").assertIsNotDisplayed()
        verify { viewModel.sortEventsBy(1) }

        composeTestRule.onNodeWithContentDescription("Sort").performClick()
        composeTestRule.onNodeWithText("Category (A-Z)").performClick()
        composeTestRule.onNodeWithTag("sorting menu").assertIsNotDisplayed()
        verify { viewModel.sortEventsBy(2) }

        composeTestRule.onNodeWithContentDescription("Sort").performClick()
        composeTestRule.onNodeWithText("Category (Z-A)").performClick()
        composeTestRule.onNodeWithTag("sorting menu").assertIsNotDisplayed()
        verify { viewModel.sortEventsBy(3) }

        composeTestRule.onNodeWithContentDescription("Sort").performClick()
        composeTestRule.onNodeWithText("Date (Soonest first)").performClick()
        composeTestRule.onNodeWithTag("sorting menu").assertIsNotDisplayed()
        verify { viewModel.sortEventsBy(0) }
    }
}
