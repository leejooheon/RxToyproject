package com.jooheon.clean_architecture.presentation.view.home

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.jooheon.clean_architecture.presentation.R
import com.jooheon.clean_architecture.presentation.theme.AppBarAlphas
import com.jooheon.clean_architecture.presentation.view.custom.GithubSearchDialog
import kotlinx.coroutines.flow.collect

private const val TAG = "Home"

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun Home(
    onOpenSettings: () -> Unit,
) {
    val navController = rememberAnimatedNavController()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect {
            Log.d(TAG, "id: ${it.id}, dest: ${it.destination.displayName}")
        }
    }

    val configuration = LocalConfiguration.current

    Scaffold(
        bottomBar = {
            val currentSelectedItem by navController.currentScreenAsState()
            HomeBottomNavigation(
                selectedNavigation = currentSelectedItem,
                onNavigationSelected = { selectedScreen ->
                    navController.navigate(selectedScreen.route) {
                        launchSingleTop = true
                        restoreState = true

                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        topBar = {
            TopAppBar(
                title = { Text(text = "My ToyProject") },
                backgroundColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = {
//                        viewModel.onNavigationClicked()
                        Log.d(TAG, "Menu IconButton")
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = null)
                    }
                },
                actions = {
                    val openDialog = remember { mutableStateOf(false) }
                    if(openDialog.value) {
                        GithubSearchDialog(openDialog = openDialog, onDismiss = { owner ->
                            if (!owner.isEmpty()) {
                                Log.d(TAG, owner)
//                                viewModel.callRepositoryApi(owner)
                            }
                        })
                    }

                    IconButton(onClick = {
//                        viewModel.onFavoriteClicked()
                        Log.d(TAG, "Favorite IconButton")
                    }) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "first IconButton description"
                        )
                    }
                    IconButton(onClick = {
                        openDialog.value = true
                    }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "second IconButton description"
                        )
                    }
                    IconButton(onClick = {
//                        viewModel.onSettingClicked()
                        Log.d(TAG, "Settings IconButton")
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = null)
                    }
                }
            )
        },
        drawerContent = {
            Text(text = "drawerContent")
        }
    ) {

    }
}

internal sealed class Screen(val route: String) {
    object Home : Screen("Home")
    object Following : Screen("following")
    object Watched : Screen("watched")
    object Search : Screen("search")
}

@Stable
@Composable
private fun NavController.currentScreenAsState(): State<Screen> {
    val selectedItem = remember { mutableStateOf<Screen>(Screen.Home) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.route == Screen.Home.route } -> {
                    selectedItem.value = Screen.Home
                }
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

@Composable
internal fun HomeBottomNavigation(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = AppBarAlphas.translucentBarAlpha()),
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        modifier = modifier
    ) {
        HomeNavigationItems.forEach { item ->
            BottomNavigationItem(
                icon = {
                    HomeNavigationItemIcon(
                        item = item,
                        selected = selectedNavigation == item.screen
                    )
                },
                label = { Text(text = stringResource(item.labelResId)) },
                selected = selectedNavigation == item.screen,
                onClick = { onNavigationSelected(item.screen) },
            )
        }
    }
}

@Composable
private fun HomeNavigationItemIcon(item: HomeNavigationItem, selected: Boolean) {
    val painter = when (item) {
        is HomeNavigationItem.ResourceIcon -> painterResource(item.iconResId)
        is HomeNavigationItem.ImageVectorIcon -> rememberVectorPainter(item.iconImageVector)
    }
    val selectedPainter = when (item) {
        is HomeNavigationItem.ResourceIcon -> item.selectedIconResId?.let { painterResource(it) }
        is HomeNavigationItem.ImageVectorIcon -> item.selectedImageVector?.let { rememberVectorPainter(it) }
    }

    if (selectedPainter != null) {
        Crossfade(targetState = selected) {
            Icon(
                painter = if (it) selectedPainter else painter,
                contentDescription = stringResource(item.contentDescriptionResId),
            )
        }
    } else {
        Icon(
            painter = painter,
            contentDescription = stringResource(item.contentDescriptionResId),
        )
    }
}

private sealed class HomeNavigationItem(
    val screen: Screen,
    @StringRes val labelResId: Int,
    @StringRes val contentDescriptionResId: Int,
) {
    class ResourceIcon(
        screen: Screen,
        @StringRes labelResId: Int,
        @StringRes contentDescriptionResId: Int,
        @DrawableRes val iconResId: Int,
        @DrawableRes val selectedIconResId: Int? = null,
    ) : HomeNavigationItem(screen, labelResId, contentDescriptionResId)

    class ImageVectorIcon(
        screen: Screen,
        @StringRes labelResId: Int,
        @StringRes contentDescriptionResId: Int,
        val iconImageVector: ImageVector,
        val selectedImageVector: ImageVector? = null,
    ) : HomeNavigationItem(screen, labelResId, contentDescriptionResId)
}

private val HomeNavigationItems = listOf(
    HomeNavigationItem.ImageVectorIcon(
        screen = Screen.Home,
        labelResId = R.string.home_title,
        contentDescriptionResId = R.string.cd_home_title,
        iconImageVector = Icons.Outlined.Weekend,
        selectedImageVector = Icons.Default.Weekend,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = Screen.Following,
        labelResId = R.string.following_title,
        contentDescriptionResId = R.string.cd_following_title,
        iconImageVector = Icons.Default.FavoriteBorder,
        selectedImageVector = Icons.Default.Favorite,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = Screen.Watched,
        labelResId = R.string.watched_title,
        contentDescriptionResId = R.string.cd_watched_title,
        iconImageVector = Icons.Outlined.Visibility,
        selectedImageVector = Icons.Default.Visibility,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = Screen.Search,
        labelResId = R.string.search_title,
        contentDescriptionResId = R.string.cd_search_title,
        iconImageVector = Icons.Default.Search,
    ),
)
