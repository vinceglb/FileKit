package io.github.vinceglb.filekit.sample.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.sample.shared.navigation.AppNavigation
import io.github.vinceglb.filekit.sample.shared.theme.AppTheme

@Composable
public fun App(modifier: Modifier = Modifier) {
    AppTheme {
        AppNavigation(
            modifier = modifier,
        )
    }
}
