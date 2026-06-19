package parcours.android.eventorias.ui.screen.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.model.Category
import parcours.android.eventorias.ui.labelRes
import parcours.android.eventorias.ui.screen.loading.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    modifier: Modifier = Modifier,
    viewModel: AddEventViewModel,
    onBackClick: () -> Unit,
    onSaveSuccessful: () -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var capturedUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedUri?.let { uri ->
                viewModel.updateUri(uri)
            }
        }
    }

    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                selectedPhotoUri = it
                viewModel.updateUri(it)
            }
        }
    )

    LaunchedEffect(saveState) {
        if (saveState == AddEventViewModel.SaveState.EventSaved) {
            onSaveSuccessful()
        }
        if (saveState is AddEventViewModel.SaveState.Error) {
            snackbarHostState.showSnackbar(
                message = getString(
                    context,
                    (saveState as AddEventViewModel.SaveState.Error).messageId
                ),
                duration = SnackbarDuration.Short
            )
            viewModel.resetSaveState()
        }
    }

    Scaffold(
        modifier = modifier,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (saveState) {
                is AddEventViewModel.SaveState.Loading -> {
                    LoadingScreen()
                }

                else -> {
                    AddEventScreenContent(
                        uiState = uiState,
                        onTitleChange = viewModel::updateTitle,
                        onDescriptionChange = viewModel::updateDescription,
                        onCategoryChange = viewModel::updateCategory,
                        onLocationChange = viewModel::updateLocation,
                        onDateChange = viewModel::updateDate,
                        onHourChange = viewModel::updateHour,
                        onMinuteChange = viewModel::updateMinute,
                        onOpenCameraClick = {
                            val uri = viewModel.generateImageUri(context)
                            capturedUri = uri
                            cameraLauncher.launch(uri)
                        },
                        onSelectPhotoClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onValidateClick = viewModel::addEvent,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreenContent(
    uiState: AddEventViewModel.FromUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateChange: (Long?) -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onOpenCameraClick: () -> Unit,
    onSelectPhotoClick: () -> Unit,
    onValidateClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                label = stringResource(R.string.title),
                value = uiState.title,
                onValueChange = onTitleChange,
                placeholder = stringResource(R.string.title_placeholder),
                error =
                    if (uiState.formErrors.titleError) stringResource(R.string.title_is_required)
                    else if (uiState.formErrors.titleLengthError) stringResource(
                        R.string.title_must_be_25_characters_or_less
                    )
                    else null,
            )

            CustomTextField(
                label = stringResource(R.string.description),
                value = uiState.description,
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.description_placeholder),
                isSingleLine = false,
                error = if (uiState.formErrors.descriptionError) stringResource(R.string.description_is_required) else null,
            )

            CategoryDropdownField(
                label = stringResource(R.string.category),
                selectedCategory = uiState.category,
                onCategorySelected = onCategoryChange,
                error = if (uiState.formErrors.categoryError) stringResource(R.string.category_is_required) else null,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DatePickerField(
                    label = stringResource(R.string.date),
                    value = uiState.formattedDate,
                    onDateValueChange = onDateChange,
                    placeholder = stringResource(R.string.date_placeholder),
                    modifier = Modifier.weight(1f),
                    error = if (uiState.formErrors.dateError) stringResource(R.string.date_is_required) else null,
                )
                TimePickerField(
                    label = stringResource(R.string.time),
                    value = uiState.formattedTime,
                    onHourValueChange = onHourChange,
                    onMinuteValueChange = onMinuteChange,
                    placeholder = stringResource(R.string.time_placeholder),
                    modifier = Modifier.weight(1f),
                    error = if (uiState.formErrors.timeError) stringResource(R.string.time_is_required) else null,
                )
            }

            CustomTextField(
                label = stringResource(R.string.address),
                value = uiState.location,
                onValueChange = onLocationChange,
                placeholder = stringResource(R.string.address_placeholder),
                error = if (uiState.formErrors.locationError) stringResource(R.string.location_is_required) else null,
            )

            if (uiState.uri != null) {
                AsyncImage(
                    model = uiState.uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .padding(top = 16.dp),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { onOpenCameraClick() },
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.PhotoCamera,
                            contentDescription = stringResource(R.string.open_camera),
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                Surface(
                    onClick = { onSelectPhotoClick() },
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = stringResource(R.string.open_gallery),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

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
    error: String? = null,
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .defaultMinSize(minHeight = minHeight)
                .semantics(mergeDescendants = true) { },
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
                isError = error != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = (-16).dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                singleLine = isSingleLine,
                maxLines = maxLines,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
        }
    }
    if (error != null) {
        Text(
            text = error,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 4.dp, top = 0.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onDateValueChange: (Long?) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    error: String? = null,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    if (showDatePicker) {
        EventDatePicker(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let {
                    onDateValueChange(selectedDateMillis)
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
        error = error,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: String,
    onHourValueChange: (Int) -> Unit,
    onMinuteValueChange: (Int) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    error: String? = null,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    if (showTimePicker) {
        EventTimePicker(
            onConfirm = { state ->
                onHourValueChange(state.hour)
                onMinuteValueChange(state.minute)
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
        error = error,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownField(
    label: String,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedCategory?.let { stringResource(it.labelRes) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            stringResource(R.string.category_placeholder),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    isError = error != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                        .offset(x = (-16).dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Category.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = stringResource(category.labelRes)) },
                            onClick = {
                                onCategorySelected(category)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
    if (error != null) {
        Text(
            text = error,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxWidth()
        )
    }
}

//@Preview
//@Composable
//fun AddEventScreenPreview() {
//    EventoriasTheme {
//        AddEventScreenContent(
//            onTitleChange = {},
//            onDescriptionChange = {},
//            onLocationChange = {},
//            onDateChange = {},
//            onTimeChange = {},
//            onBackClick = {},
//            onValidateClick = {},
//            uiState = AddEventViewModel.AddEventUiState()
//        )
//    }
//}
