package parcours.android.eventorias.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import parcours.android.eventorias.R
import parcours.android.eventorias.ui.theme.EventoriasTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddEventScreen(
    viewModel: AddEventViewModel,
    onBackClick: () -> Unit,
    onValidateClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddEventScreenContent(
        uiState = uiState,
        onTitleChange = viewModel::updateTitle,
        onDescriptionChange = viewModel::updateDescription,
        onLocationChange = viewModel::updateLocation,
        onDateChange = viewModel::updateDate,
        onTimeChange = viewModel::updateTime,
        onBackClick = onBackClick,
        onValidateClick = onValidateClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreenContent(
    uiState: AddEventViewModel.AddEventUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onValidateClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.event_creation),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.navigate_back
                            )
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
        bottomBar = {
            Button(
                onClick = onValidateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ) {
                Text(stringResource(R.string.validate), fontWeight = FontWeight.Bold)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                label = stringResource(R.string.title),
                value = uiState.event.title,
                onValueChange = onTitleChange,
                placeholder = stringResource(R.string.title_placeholder)
            )

            CustomTextField(
                label = stringResource(R.string.description),
                value = uiState.event.description ?: "",
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.description_placeholder),
                isSingleLine = false,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DatePickerField(
                    label = stringResource(R.string.date),
                    value = uiState.date,
                    onValueChange = onDateChange,
                    placeholder = stringResource(R.string.date_placeholder),
                    modifier = Modifier.weight(1f)
                )
                TimePickerField(
                    label = stringResource(R.string.time),
                    value = uiState.time,
                    onValueChange = onTimeChange,
                    placeholder = stringResource(R.string.time_placeholder),
                    modifier = Modifier.weight(1f)
                )
            }

            CustomTextField(
                label = stringResource(R.string.address),
                value = uiState.event.location ?: "",
                onValueChange = onLocationChange,
                placeholder = stringResource(R.string.address_placeholder)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { /* TODO */ },
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.PhotoCamera,
                            contentDescription = "Camera",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                Surface(
                    onClick = { /* TODO */ },
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Attachment",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    isSingleLine: Boolean = true,
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .defaultMinSize(minHeight = minHeight)
        ) {

            var maxLines = 1
            if (!isSingleLine) {
                maxLines = 5
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        placeholder,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = (-16).dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = isSingleLine,
                maxLines = maxLines,
            )
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    if (showDatePicker) {
        EventDatePicker(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let {
                    val date = Date(it)
                    val formattedDate =
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                    onValueChange(formattedDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    PickerCard(
        label = label,
        value = value,
        placeholder = placeholder,
        modifier = modifier,
        showPicker = { showDatePicker = it },
        minHeight = minHeight,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    if (showTimePicker) {
        EventTimePicker(
            onConfirm = { state ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, state.hour)
                cal.set(Calendar.MINUTE, state.minute)
                onValueChange(formatter.format(cal.time))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    PickerCard(
        label = label,
        value = value,
        placeholder = placeholder,
        modifier = modifier,
        showPicker = { showTimePicker = it },
        minHeight = minHeight,
    )
}

@Preview
@Composable
fun AddEventScreenPreview() {
    EventoriasTheme {
        AddEventScreenContent(
            onTitleChange = {},
            onDescriptionChange = {},
            onLocationChange = {},
            onDateChange = {},
            onTimeChange = {},
            onBackClick = {},
            onValidateClick = {},
            uiState = AddEventViewModel.AddEventUiState()
        )
    }
}
