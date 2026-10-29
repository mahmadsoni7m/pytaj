package tj.pythontaj.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tj.pythontaj.MainViewModel
import tj.pythontaj.domain.VerdictKind

@Composable
fun ExerciseScreen(vm: MainViewModel, exerciseId: String, onBack: () -> Unit) {
    // Ҳолати охирини машқи пештара тоза мекунем.
    DisposableEffect(exerciseId) {
        vm.resetExercise()
        onDispose { vm.resetExercise() }
    }
    ContentGate(vm) { cur ->
        val ex = cur.exercise(exerciseId)
        val lesson = cur.lessonOfExercise(exerciseId)
        ScreenScaffold(ex?.title ?: T.exercises, onBack = onBack) { pad ->
            if (ex == null || lesson == null) {
                Text(T.nothingFound, Modifier.padding(pad).padding(16.dp))
                return@ScreenScaffold
            }
            var code by rememberSaveable(ex.id) { mutableStateOf(ex.starterCode) }
            var hintsShown by rememberSaveable(ex.id) { mutableStateOf(0) }
            var showSolution by rememberSaveable(ex.id) { mutableStateOf(false) }
            val state by vm.exercise.collectAsState()
            val attempts by remember(ex.id) { vm.attempts(ex.id) }.collectAsState(initial = emptyList())
            val passedBefore = attempts.any { it.passed }

            Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(ex.prompt)
                Text("${T.attempts}: ${attempts.size}" + if (passedBefore) "  •  ✓ ${T.passed}" else "", style = MaterialTheme.typography.bodySmall)
                CodeEditor(code, { code = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.submitExercise(ex, lesson.id, code) }, enabled = !state.console.running) { Text(T.check) }
                    OutlinedButton(onClick = { vm.runExercise(code) }, enabled = !state.console.running) { Text(T.run) }
                    OutlinedButton(onClick = { vm.stopExercise() }, enabled = state.console.running) { Text(T.stop) }
                    TextButton(onClick = { code = ex.starterCode }) { Text(T.reset) }
                }
                state.verdict?.let { v ->
                    val ok = v.kind == VerdictKind.CORRECT
                    AppCard {
                        Text(
                            if (ok) T.correct else T.wrong,
                            color = if (ok) Palette.Success else Palette.Error,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(v.message)
                    }
                }
                ConsoleView(state.console)

                if (hintsShown < ex.hints.size) {
                    OutlinedButton(onClick = { hintsShown++ }) { Text("${T.hint} (${hintsShown + 1}/${ex.hints.size})") }
                }
                ex.hints.take(hintsShown).forEach { Text("💡 $it") }

                val failed = attempts.count { !it.passed }
                if (!showSolution && (passedBefore || failed >= 2)) {
                    TextButton(onClick = { showSolution = true }) { Text(T.solution) }
                }
                if (showSolution) {
                    AppCard {
                        Text(T.solution, style = MaterialTheme.typography.titleSmall)
                        CodeBlock(ex.solution)
                        Spacer(Modifier.height(6.dp))
                        Text(ex.explanation)
                    }
                }
            }
        }
    }
}
