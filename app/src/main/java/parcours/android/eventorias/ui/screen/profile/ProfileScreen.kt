package parcours.android.eventorias.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.model.User
import parcours.android.eventorias.ui.screen.error.ErrorScreen

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEventsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.profileScreenState.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            ProfileBottomBar(
                currentRoute = "profile",
                onEventsClick = onEventsClick,
                onProfileClick = {}
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (uiState) {
                is ProfileViewModel.ProfileScreenState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is ProfileViewModel.ProfileScreenState.NoUserFound -> {
                    ErrorScreen(
                        errorMessage = "User Not Found",
                        onRetry = {},
                    )
                }

                is ProfileViewModel.ProfileScreenState.UserFound -> {
                    ProfileContent(
                        user = (uiState as ProfileViewModel.ProfileScreenState.UserFound).user,
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggle = { viewModel.onNotificationsToggle(it) }
                    )

                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.user_profile),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.pictureUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        ProfileField(
            label = stringResource(R.string.name),
            value = user.username ?: "Not specified"
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileField(
            label = stringResource(R.string.email),
            value = user.email ?: "Not specified"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { onNotificationsToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.Red,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.notifications),
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF323135), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfileBottomBar(
    currentRoute: String,
    onEventsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1C1B1F),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            selected = currentRoute == "events",
            onClick = onEventsClick,
            icon = { Icon(Icons.Default.Event, contentDescription = null) },
            label = { Text(stringResource(R.string.events)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.White,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.profile)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.White,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF323135)
            )
        )
    }
}

