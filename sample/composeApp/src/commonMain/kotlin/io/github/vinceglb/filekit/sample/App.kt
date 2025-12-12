package io.github.vinceglb.filekit.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.sample.navigation.AppNavigation
import io.github.vinceglb.filekit.sample.theme.StarterKitTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    StarterKitTheme {
        val appState = rememberSampleAppState()
        AppNavigation(
            appState = appState,
            modifier = modifier,
        )
    }
}
