package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs

import androidx.lifecycle.ViewModel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class DialogsViewModel : ViewModel() {
    private val _platformFiles = MutableStateFlow(emptyList<PlatformFile>())
    val platformFiles get() = _platformFiles.asStateFlow()

    fun addPlatformFiles(platformFiles: List<PlatformFile>) {
        _platformFiles.update { it + platformFiles }
    }
}
