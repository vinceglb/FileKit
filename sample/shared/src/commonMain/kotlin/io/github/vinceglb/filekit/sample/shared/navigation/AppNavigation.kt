package io.github.vinceglb.filekit.sample.shared.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.vinceglb.filekit.sample.shared.SampleAppState
import io.github.vinceglb.filekit.sample.shared.ui.icons.Home
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.MessageCircleCode
import io.github.vinceglb.filekit.sample.shared.ui.icons.ScanFace
import io.github.vinceglb.filekit.sample.shared.ui.screens.core.CoreScreen
import io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.DialogsScreen
import io.github.vinceglb.filekit.sample.shared.ui.screens.home.HomeScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private sealed interface TopLevelRoute : NavKey {
    val icon: ImageVector

    val label: String
        get() = this::class.simpleName ?: "Unknown"
}

@Serializable
internal data object Home : TopLevelRoute {
    override val icon: ImageVector = LucideIcons.Home
}

@Serializable
internal data object Dialogs : TopLevelRoute {
    override val icon: ImageVector = LucideIcons.MessageCircleCode
}

@Serializable
internal data object Core : TopLevelRoute {
    override val icon: ImageVector = LucideIcons.ScanFace
}

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(
    Home,
    Dialogs,
    Core,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppNavigation(
    appState: SampleAppState,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Home::class, Home.serializer())
                    subclass(Dialogs::class, Dialogs.serializer())
                    subclass(Core::class, Core.serializer())
                }
            }
        },
        Home,
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth > 720.dp

        Scaffold(
            bottomBar = {
                if (!isWide) {
                    NavigationBar {
                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                            val isSelected = topLevelRoute == backStack.lastOrNull()
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    backStack.remove(topLevelRoute)
                                    backStack.add(topLevelRoute)
                                },
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(topLevelRoute.label) },
                            )
                        }
                    }
                }
            },
        ) {
            Row {
                if (isWide) {
                    NavigationRail(
                        header = {
                            Text(
                                text = "FileKit",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp),
                            )
                        },
                    ) {
                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                            val isSelected = topLevelRoute == backStack.lastOrNull()
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = {
                                    backStack.remove(topLevelRoute)
                                    backStack.add(topLevelRoute)
                                },
                                icon = {
                                    Icon(
                                        imageVector = topLevelRoute.icon,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(topLevelRoute.label) },
                            )
                        }
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        entry<Home> {
                            HomeScreen(appState = appState)
                        }
                        entry<Dialogs> {
                            DialogsScreen(appState = appState)
                        }
                        entry<Core> {
                            CoreScreen(appState = appState)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
