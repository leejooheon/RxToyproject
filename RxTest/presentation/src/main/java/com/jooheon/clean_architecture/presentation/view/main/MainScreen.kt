package com.jooheon.clean_architecture.presentation.view.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.jooheon.clean_architecture.presentation.theme.CustomTheme
import com.jooheon.clean_architecture.presentation.utils.showToastMessage
import com.jooheon.clean_architecture.presentation.view.custom.GithubSearchDialog
import com.jooheon.clean_architecture.presentation.view.destinations.TestScreenDestination
import com.jooheon.clean_architecture.presentation.view.main.bottom.MyBottomNavigation
import com.jooheon.clean_architecture.presentation.view.main.bottom.Screen
import com.jooheon.clean_architecture.presentation.view.main.bottom.currentScreenAsState
import com.jooheon.clean_architecture.presentation.view.main.following.FollowingScreen
import com.jooheon.clean_architecture.presentation.view.main.home.HomeScreen
import com.jooheon.clean_architecture.presentation.view.main.search.SearchScreen
import com.jooheon.clean_architecture.presentation.view.main.watched.WatchedScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val TAG = "MainScreen"

@Composable
fun sharedViewModel() = LocalContext.current as MainActivity

@OptIn(ExperimentalAnimationApi::class)
@Destination
@Composable
fun MainScreen(
    navigator: DestinationsNavigator
) {
    val viewModel: MainViewModel = hiltViewModel(sharedViewModel())
    val bottomNavController = rememberAnimatedNavController()

    val scaffoldState = rememberScaffoldState(rememberDrawerState(initialValue = DrawerValue.Closed))
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = CustomTheme.colors.uiBackground,
        snackbarHost = { state -> MySnackHost(state) },
        topBar = { TopBar(viewModel, navigator, scaffoldState, scope) },
        bottomBar = { BottomBar(bottomNavController) },
        floatingActionButton = { MyFloatingActionButton(scaffoldState, scope) },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        drawerContent = { DrawerContent(scaffoldState, scope) },
        drawerBackgroundColor = CustomTheme.colors.uiBackground,
        content = { RegisterBottomNavigation(viewModel, bottomNavController, navigator) }
    )

    RegisterBackPressedHandler(viewModel, scaffoldState, scope)
}

@Composable
fun MyFloatingActionButton(scaffoldState: ScaffoldState, scope: CoroutineScope) {
    val floatingButtonText = remember { mutableStateOf("X")}

    FloatingActionButton(
        onClick = {
            floatingButtonText.value = "+"
            scope.launch {
                val result = scaffoldState.snackbarHostState.showSnackbar(
                    message = "Jetpack Compose Snackbar",
                    actionLabel = "Ok"
                )

                when(result) {
                    SnackbarResult.Dismissed -> {
                        Log.d(TAG, "Snackbar dismissed")
                        floatingButtonText.value = "X"
                    }
                    SnackbarResult.ActionPerformed -> {
                        Log.d(TAG, "Snackbar action!")
                        floatingButtonText.value = "X"
                    }
                }
            }
        }
    ) {
        Text(
            text = floatingButtonText.value,
            color = CustomTheme.colors.textInteractive
        )
    }
}

@Composable
fun DrawerContent(scaffoldState: ScaffoldState, scope: CoroutineScope) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Spacer(Modifier.statusBarsHeight(additional = 24.dp))

        Text(
            modifier = Modifier.padding(16.dp),
            text = "drawerContent - 1"
        )
        Text(
            modifier = Modifier.padding(16.dp),
            text = "drawerContent - 2"
        )
        Text(
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                },
            text = "drawerContent - 3"
        )
    }
}

@Composable
fun RegisterBottomNavigation(
    viewModel: MainViewModel,
    navController: NavHostController,
    navigator: DestinationsNavigator
) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navigator)
        }
        composable(Screen.Following.route) {
            FollowingScreen()
        }
        composable(Screen.Watched.route) {
            WatchedScreen()
        }
        composable(Screen.Search.route) {
            SearchScreen()
        }
    }
}

@Composable
fun BottomBar(bottomNavController: NavController) {
    val currentSelectedItem by bottomNavController.currentScreenAsState()
    MyBottomNavigation(
        modifier = Modifier.fillMaxWidth(),
        selectedNavigation = currentSelectedItem,
        onNavigationSelected = { selectedScreen ->
            Log.d(TAG, "selectedScreen: $selectedScreen")

            bottomNavController.navigate(selectedScreen.route) {
                launchSingleTop = true
                restoreState = true

                popUpTo(bottomNavController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        }
    )
}

@Composable
fun TopBar(
    viewModel: MainViewModel,
    navigator: DestinationsNavigator,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
) {
    val openGithubSearchDialog = remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = "My ToyProject") },
        backgroundColor = Color.White,
        navigationIcon = {
            IconButton(onClick = {
                scope.launch { scaffoldState.drawerState.open() }
                Log.d(TAG, "Menu IconButton")
            }) {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = {
                viewModel.onFavoriteClicked()
                Log.d(TAG, "Favorite IconButton")
            }) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "first IconButton description"
                )
            }
            IconButton(onClick = {
                openGithubSearchDialog.value = true
            }) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "second IconButton description"
                )
            }
            IconButton(onClick = {
//                        viewModel.onSettingClicked()
                Log.d(TAG, "Settings IconButton")
                navigator.navigate(TestScreenDestination())
            }) {
                Icon(Icons.Filled.Settings, contentDescription = null)
            }
        }
    )

    if(openGithubSearchDialog.value) {
        GithubSearchDialog(openGithubSearchDialog, onDismiss = { owner ->
            if (!owner.isEmpty()) {
                Log.d(TAG, owner)
                viewModel.callRepositoryApi(owner)
            }
        })
    }
}

@Composable
fun MySnackHost(state: SnackbarHostState) {
    SnackbarHost(
        hostState = state,
        snackbar = { data ->
            Snackbar(
                modifier = Modifier
                    .padding(8.dp)
                    .background(CustomTheme.colors.notificationBadge, RoundedCornerShape(8.dp)),
                action = {
                    Text(
                        text = data.actionLabel?.let { it } ?: run { "hide" },
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { state.currentSnackbarData?.dismiss() },
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = CustomTheme.colors.textPrimary,
                            fontSize = 18.sp
                        )
                    )
                }
            ) {
                Text(
                    text = data.message,
                    color = CustomTheme.colors.textInteractive
                )
            }
        }
    )
}

@Composable
fun RegisterBackPressedHandler (
    viewModel: MainViewModel,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    BackHandler(
        enabled = scaffoldState.drawerState.isOpen || viewModel.isDoubleBackPressed.value
    ) {
        scope.launch {
            if(scaffoldState.drawerState.isOpen) {
                scope.launch { scaffoldState.drawerState.close() }
                return@launch
            }
            if(viewModel.isDoubleBackPressed.value) {
                viewModel.onBackPressed()
                showToastMessage(context, "Press once more to exit.")
                return@launch
            }
        }
    }
}