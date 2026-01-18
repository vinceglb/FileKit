package io.github.vinceglb.filekit.sample.shared.ui.screens.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSelectionButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSupportCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.BookOpenText
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.BookmarkKind
import io.github.vinceglb.filekit.sample.shared.util.BookmarkStorage
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus
import kotlinx.coroutines.launch

@Composable
internal fun BookmarksRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    BookmarksScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    val storage = remember { BookmarkStorage() }
    val scope = rememberCoroutineScope()

    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var bookmarkedFile by remember { mutableStateOf<PlatformFile?>(null) }
    var bookmarkedDirectory by remember { mutableStateOf<PlatformFile?>(null) }

    val filePickerLauncher = rememberFilePickerLauncher { file ->
        scope.launch {
            if (file != null) {
                storage.save(BookmarkKind.File, file)
                bookmarkedFile = file
            }
            buttonState = AppScreenHeaderButtonState.Enabled
        }
    }
    val directoryPickerLauncher = rememberDirectoryPickerLauncher(
        directory = bookmarkedDirectory,
    ) { directory ->
        scope.launch {
            if (directory != null) {
                storage.save(BookmarkKind.Directory, directory)
                bookmarkedDirectory = directory
            }
            buttonState = AppScreenHeaderButtonState.Enabled
        }
    }

    LaunchedEffect(storage) {
        bookmarkedFile = storage.load(BookmarkKind.File)
        bookmarkedDirectory = storage.load(BookmarkKind.Directory)
    }

    val isBookmarkSupported = storage.isSupported
    val isDirectoryPickerSupported = isBookmarkSupported && directoryPickerLauncher.isSupported
    val primaryButtonText = if (isBookmarkSupported) "Pick File" else "Bookmarks Unavailable"

    fun openFilePicker() {
        if (!isBookmarkSupported) {
            return
        }
        buttonState = AppScreenHeaderButtonState.Loading
        filePickerLauncher.launch()
    }

    fun openDirectoryPicker() {
        if (!isDirectoryPickerSupported) {
            return
        }
        buttonState = AppScreenHeaderButtonState.Loading
        directoryPickerLauncher.launch()
    }

    fun clearBookmark(kind: BookmarkKind) {
        scope.launch {
            storage.save(kind, null)
            when (kind) {
                BookmarkKind.File -> bookmarkedFile = null
                BookmarkKind.Directory -> bookmarkedDirectory = null
            }
        }
    }

    val bookmarkedItems = listOfNotNull(bookmarkedFile, bookmarkedDirectory)

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = { AppUrl("https://filekit.mintlify.app/core/bookmark-data").openUrlInBrowser() },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding + PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                AppScreenHeader(
                    icon = LucideIcons.BookOpenText,
                    title = "Bookmarks",
                    subtitle = "Persist file and folder access using bookmark data",
                    documentationUrl = "https://filekit.mintlify.app/core/bookmark-data",
                    primaryButtonText = primaryButtonText,
                    primaryButtonEnabled = isBookmarkSupported,
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = ::openFilePicker,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                BookmarkSettingsCard(
                    bookmarkedFileName = bookmarkedFile?.name,
                    bookmarkedDirectoryName = bookmarkedDirectory?.name,
                    isFilePickerSupported = isBookmarkSupported,
                    isDirectoryPickerSupported = isDirectoryPickerSupported,
                    onPickFile = ::openFilePicker,
                    onPickDirectory = ::openDirectoryPicker,
                    onClearFile = { clearBookmark(BookmarkKind.File) },
                    onClearDirectory = { clearBookmark(BookmarkKind.Directory) },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            if (!isBookmarkSupported) {
                item {
                    AppPickerSupportCard(
                        text = "Bookmark data is available on Android, iOS, and desktop targets.",
                        icon = LucideIcons.BookOpenText,
                        modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                    )
                }
            }

            item {
                AppPickerResultsCard(
                    files = bookmarkedItems,
                    emptyText = "No bookmarks saved yet",
                    emptyIcon = LucideIcons.BookOpenText,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }
}

@Composable
private fun BookmarkSettingsCard(
    bookmarkedFileName: String?,
    bookmarkedDirectoryName: String?,
    isFilePickerSupported: Boolean,
    isDirectoryPickerSupported: Boolean,
    onPickFile: () -> Unit,
    onPickDirectory: () -> Unit,
    onClearFile: () -> Unit,
    onClearDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppPickerSelectionButton(
                label = "Bookmarked File",
                value = bookmarkedFileName,
                placeholder = "No file bookmarked",
                icon = LucideIcons.File,
                enabled = isFilePickerSupported,
                onClick = onPickFile,
                onClear = onClearFile,
            )

            AppPickerSelectionButton(
                label = "Bookmarked Folder",
                value = bookmarkedDirectoryName,
                placeholder = "No folder bookmarked",
                icon = LucideIcons.Folder,
                enabled = isDirectoryPickerSupported,
                onClick = onPickDirectory,
                onClear = onClearDirectory,
            )

            Text(
                text = "Tip: Bookmarks are stored in the app files directory and restored on launch.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Preview
@Composable
private fun BookmarksScreenPreview() {
    AppTheme {
        BookmarksScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
