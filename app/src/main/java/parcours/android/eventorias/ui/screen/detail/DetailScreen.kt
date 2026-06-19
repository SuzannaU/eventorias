package parcours.android.eventorias.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.model.Event
import parcours.android.eventorias.ui.formatEventDate
import parcours.android.eventorias.ui.formatEventTime
import parcours.android.eventorias.ui.getStaticMapUrl
import parcours.android.eventorias.ui.screen.ErrorImageBox
import parcours.android.eventorias.ui.screen.PlaceholderBox
import parcours.android.eventorias.ui.screen.error.ErrorScreen
import parcours.android.eventorias.ui.screen.loading.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val event = (uiState as? DetailViewModel.DetailUiState.Success)?.event
                    Text(
                        text = event?.title ?: stringResource(R.string.no_event),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DetailViewModel.DetailUiState.Loading -> {
                    LoadingScreen()
                }

                is DetailViewModel.DetailUiState.Success -> {
                    DetailContent(event = state.event)
                }

                is DetailViewModel.DetailUiState.Error -> {
                    ErrorScreen(
                        errorMessage = stringResource(state.errorMessageId),
                        onRetry = { viewModel.loadEvent() },
                    )
                }
            }

        }

    }

}

@Composable
fun DetailContent(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AsyncImage(
            model = event.pictureUrl,
            contentDescription = stringResource(R.string.event_image_preview),
            modifier = Modifier
                .fillMaxHeight()
                .height(340.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.outline_photo_24),
            error = painterResource(id = R.drawable.outline_broken_image_80)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = event.dateTime?.formatEventDate() ?: stringResource(R.string.no_date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = event.dateTime?.formatEventTime() ?: stringResource(R.string.no_time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            AsyncImage(
                model = event.author?.pictureUrl ?: R.drawable.baseline_face_24,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.baseline_face_24)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = event.description ?: stringResource(R.string.no_description),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = event.location ?: stringResource(R.string.no_location),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(16.dp))

            val mapUrl = remember(event.location) { getStaticMapUrl(event.location) }

            SubcomposeAsyncImage(
                model = mapUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(width = 150.dp, height = 70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                loading = { PlaceholderBox() },
                error = { ErrorImageBox() },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
