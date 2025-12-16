package io.github.vinceglb.filekit.sample.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.sample.shared.navigation.AppNavigation
import io.github.vinceglb.filekit.sample.shared.theme.StarterKitTheme

@Composable
public fun App(modifier: Modifier = Modifier) {
    StarterKitTheme {
        val appState = rememberSampleAppState()
        AppNavigation(
            appState = appState,
            modifier = modifier,
        )
    }
}
