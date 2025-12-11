package io.github.vinceglb.filekit.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(modifier: Modifier = Modifier) {
    MaterialTheme {
        val appState = rememberSampleAppState()
        var currentScreen by rememberSaveable { mutableStateOf(SampleScreen.Home) }

        BoxWithConstraints(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
            val isWide = maxWidth > 720.dp

            if (isWide) {
                Row(Modifier.fillMaxSize()) {
                    NavigationRail(
                        header = {
                            Text(
                                text = "FileKit",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp),
                            )
                        },
                    ) {
                        SampleScreen.entries.forEach { screen ->
                            NavigationRailItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = {},
                                label = { Text(screen.title) },
                            )
                        }
                    }

                    Surface(Modifier.weight(1f)) {
                        ScreenContent(
                            screen = currentScreen,
                            appState = appState,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            } else {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("FileKit Sample") },
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            SampleScreen.entries.forEach { screen ->
                                NavigationBarItem(
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen },
                                    icon = {},
                                    label = { Text(screen.title) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                    ) {
                        ScreenContent(
                            screen = currentScreen,
                            appState = appState,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

private enum class SampleScreen(
    val title: String,
) {
    Home("Home"),
    Dialogs("Dialogs"),
    Core("Core"),
}

@Composable
private fun ScreenContent(
    screen: SampleScreen,
    appState: SampleAppState,
    modifier: Modifier = Modifier,
) {
    when (screen) {
        SampleScreen.Home -> HomeScreen(appState = appState, modifier = modifier)
        SampleScreen.Dialogs -> DialogsScreen(appState = appState, modifier = modifier)
        SampleScreen.Core -> CoreScreen(appState = appState, modifier = modifier)
    }
}
