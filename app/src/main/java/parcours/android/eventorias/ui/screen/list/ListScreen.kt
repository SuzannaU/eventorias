package parcours.android.eventorias.ui.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.ui.ErrorImageBox
import parcours.android.eventorias.ui.PlaceholderBox
import parcours.android.eventorias.ui.formatEventDate
import parcours.android.eventorias.ui.screen.LIST_ROUTE
import parcours.android.eventorias.ui.screen.error.ErrorScreen
import parcours.android.eventorias.ui.screen.profile.ProfileBottomBar
import parcours.android.eventorias.ui.theme.EventoriasTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit,
    onEventClick: (String) -> Unit,
) {

    val uiState by viewModel.listScreenState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSearchMode by rememberSaveable { mutableStateOf(false) }
    var sortDropdownDisplayed by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (isSearchMode) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text(stringResource(R.string.search)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchMode = false
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChange("") },
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.event_list),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { isSearchMode = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.outlineVariant,
                            )
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search)
                            )
                        }
                        IconButton(
                            onClick = { sortDropdownDisplayed = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.outlineVariant,
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.sort)
                            )
                        }

                        DropdownMenu(
                            expanded = sortDropdownDisplayed,
                            onDismissRequest = { sortDropdownDisplayed = false }
                        ) {
                            viewModel.sortOptions.forEachIndexed { index, label ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.sortEventsBy(index)
                                        selectedIndex = index
                                        sortDropdownDisplayed = false
                                    },
                                    leadingIcon = {
                                        if (index == selectedIndex) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick() },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_event))
            }
        },
        bottomBar = {
            ProfileBottomBar(
                currentRoute = LIST_ROUTE,
                onEventsClick = {},
                onProfileClick = onProfileClick,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = uiState) {
                is ListViewModel.ListScreenState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is ListViewModel.ListScreenState.EventsLoaded -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentState.events) { event ->
                            EventCell(
                                event = event,
                                onClick = { onEventClick(event.eventId) }
                            )
                        }
                    }
                }

                is ListViewModel.ListScreenState.NoEvents -> {
                    Text(
                        stringResource(R.string.no_events_found),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is ListViewModel.ListScreenState.NoResultsFound -> {
                    Text(
                        stringResource(R.string.no_results_found),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is ListViewModel.ListScreenState.Error -> {
                    ErrorScreen(
                        errorMessage = currentState.errorMessage
                            ?: stringResource(R.string.error_message),
                        onRetry = { viewModel.onRetry() }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCell(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(12.dp))

            AsyncImage(
                model = event.author?.pictureUrl ?: R.drawable.baseline_face_24,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.baseline_face_24),
                error = painterResource(R.drawable.baseline_face_24),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.dateTime?.formatEventDate() ?: stringResource(R.string.no_date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            SubcomposeAsyncImage(
                model = event.pictureUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(150.dp)
                    .clip(
                        RoundedCornerShape(
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            topStart = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                contentScale = ContentScale.FillWidth,
                loading = { PlaceholderBox() },
                error = { ErrorImageBox() }
            )
        }
    }
}

@Preview
@Composable
fun EventCellPreview() {
    EventoriasTheme {
        EventCell(
            event = Event(
                title = "Art exhibition",
                dateTime = Timestamp.now(),
                pictureUrl = "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            onClick = {}
        )
    }
}
