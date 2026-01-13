package io.github.vinceglb.filekit.sample.shared.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.MessageCircleCode
import io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.DialogsRoute
import io.github.vinceglb.filekit.sample.shared.ui.screens.filedetails.FileDetailsRoute
import io.github.vinceglb.filekit.sample.shared.ui.screens.gallerypicker.GalleryPickerRoute
import io.github.vinceglb.filekit.sample.shared.ui.screens.home.HomeRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private sealed interface TopLevelRoute : NavKey {
    val icon: ImageVector

    val label: String
        get() = this::class.simpleName ?: "Unknown"
}

@Serializable
private data object Home : NavKey

@Serializable
private data object Dialogs : TopLevelRoute {
    override val icon: ImageVector = LucideIcons.MessageCircleCode
}

@Serializable
private data object GalleryPicker : NavKey

@Serializable
private data class FileDetails(
    val file: PlatformFile,
) : NavKey

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(
    Dialogs,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppNavigation(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Dialogs::class, Dialogs.serializer())
                    subclass(FileDetails::class, FileDetails.serializer())
                    subclass(GalleryPicker::class, GalleryPicker.serializer())
                    subclass(Home::class, Home.serializer())
                }
            }
        },
        Home,
    )
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategy = bottomSheetStrategy,
        entryProvider = entryProvider {
            entry<Home> {
                HomeRoute()
            }
            entry<Dialogs> {
                DialogsRoute()
            }
            entry<GalleryPicker> {
                GalleryPickerRoute(
                    onNavigateBack = { /* backStack.removeLastOrNull() */ },
                    onDisplayFileDetails = { file ->
                        backStack.add(FileDetails(file))
                    },
                )
            }
            entry<FileDetails>(
                metadata = BottomSheetSceneStrategy.bottomSheet(),
            ) {
                FileDetailsRoute(
                    file = it.file,
                    onDeleteFile = {
                        println("File deleted, closing details")
                        backStack.removeLastOrNull()
                    },
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
    )

//    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
//        val isWide = maxWidth > 720.dp
//
//        Scaffold(
//            bottomBar = {
//                if (!isWide) {
//                    NavigationBar {
//                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
//                            val isSelected = topLevelRoute == backStack.lastOrNull()
//                            NavigationBarItem(
//                                selected = isSelected,
//                                onClick = {
//                                    backStack.remove(topLevelRoute)
//                                    backStack.add(topLevelRoute)
//                                },
//                                icon = {
//                                    Icon(
//                                        imageVector = topLevelRoute.icon,
//                                        contentDescription = null,
//                                    )
//                                },
//                                label = { Text(topLevelRoute.label) },
//                            )
//                        }
//                    }
//                }
//            },
//        ) {
//            Row {
//                if (isWide) {
//                    NavigationRail(
//                        header = {
//                            Text(
//                                text = "FileKit",
//                                style = MaterialTheme.typography.titleMedium,
//                                modifier = Modifier.padding(16.dp),
//                            )
//                        },
//                    ) {
//                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
//                            val isSelected = topLevelRoute == backStack.lastOrNull()
//                            NavigationRailItem(
//                                selected = isSelected,
//                                onClick = {
//                                    backStack.remove(topLevelRoute)
//                                    backStack.add(topLevelRoute)
//                                },
//                                icon = {
//                                    Icon(
//                                        imageVector = topLevelRoute.icon,
//                                        contentDescription = null,
//                                    )
//                                },
//                                label = { Text(topLevelRoute.label) },
//                            )
//                        }
//                    }
//                }
//
//                NavDisplay(
//                    backStack = backStack,
//                    entryDecorators = listOf(
//                        rememberSaveableStateHolderNavEntryDecorator(),
//                        rememberViewModelStoreNavEntryDecorator(),
//                    ),
//                    entryProvider = entryProvider {
//                        entry<Home> {
//                            HomeScreen(appState = appState)
//                        }
//                        entry<Dialogs> {
//                            DialogsRoute()
//                        }
//                        entry<Core> {
//                            CoreScreen(appState = appState)
//                        }
//                    },
//                    modifier = Modifier.fillMaxSize(),
//                )
//            }
//        }
//    }
}
