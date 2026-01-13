package io.github.vinceglb.filekit.sample.shared.ui.screens.home

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme

@Composable
internal fun HomeRoute() {
    HomeScreen()
}

@Composable
private fun HomeScreen() {
    Scaffold {
        Text("Home Screen")
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen()
    }
}
