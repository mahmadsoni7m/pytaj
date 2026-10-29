package tj.pythontaj.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tj.pythontaj.MainViewModel

private data class Dest(val route: String, val label: String, val icon: ImageVector)

private val topLevel = listOf(
    Dest("home", T.home, Icons.Filled.Home),
    Dest("path", T.path, Icons.Filled.Menu),
    Dest("editor", T.editor, Icons.Filled.Edit),
    Dest("settings", T.settings, Icons.Filled.Settings),
)

private fun NavHostController.goTopLevel(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun PythonTajApp(vm: MainViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (route in topLevel.map { it.route }) {
                NavigationBar {
                    topLevel.forEach { d ->
                        NavigationBarItem(
                            selected = route == d.route,
                            onClick = { nav.goTopLevel(d.route) },
                            icon = { Icon(d.icon, contentDescription = d.label) },
                            label = { Text(d.label) },
                        )
                    }
                }
            }
        },
    ) { pad: PaddingValues ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(pad)) {
            composable("home") { HomeScreen(vm, onOpenLesson = { nav.navigate("lesson/$it") }, onGo = { nav.goTopLevel(it) }) }
            composable("path") { PathScreen(vm, onOpenModule = { nav.navigate("module/$it") }, onOpenLesson = { nav.navigate("lesson/$it") }) }
            composable("module/{id}") { b ->
                ModuleScreen(vm, b.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() }, onOpenLesson = { nav.navigate("lesson/$it") })
            }
            composable("lesson/{id}") { b ->
                LessonScreen(
                    vm, b.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onOpenExercise = { nav.navigate("exercise/$it") },
                    onOpenQuiz = { nav.navigate("quiz/$it") },
                    onOpenEditor = { nav.goTopLevel("editor") },
                )
            }
            composable("exercise/{id}") { b -> ExerciseScreen(vm, b.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() }) }
            composable("quiz/{id}") { b -> QuizScreen(vm, b.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() }) }
            composable("editor") { EditorScreen(vm) }
            composable("settings") { SettingsScreen(vm) }
        }
    }
}
