package tj.pythontaj.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tj.pythontaj.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(vm: MainViewModel, lessonId: String, onBack: () -> Unit) {
    ContentGate(vm) { cur ->
        val lesson = cur.lesson(lessonId)
        ScreenScaffold(T.quiz, onBack = onBack) { pad ->
            if (lesson == null || lesson.quiz.isEmpty()) {
                Text(T.nothingFound, Modifier.padding(pad).padding(16.dp))
                return@ScreenScaffold
            }
            val questions = lesson.quiz
            var index by rememberSaveable(lessonId) { mutableStateOf(0) }
            var selected by rememberSaveable(lessonId) { mutableStateOf(-1) }
            var score by rememberSaveable(lessonId) { mutableStateOf(0) }
            var finished by rememberSaveable(lessonId) { mutableStateOf(false) }

            Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (finished) {
                    LaunchedEffect(lessonId) { vm.recordQuiz(lessonId, score, questions.size) }
                    AppCard {
                        Text(T.quizResult, style = MaterialTheme.typography.titleMedium)
                        Text("$score / ${questions.size}")
                    }
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(T.back) }
                } else {
                    val q = questions[index]
                    Text("${index + 1} / ${questions.size}", style = MaterialTheme.typography.labelLarge)
                    Text(q.question, style = MaterialTheme.typography.titleMedium)
                    q.options.forEachIndexed { i, option ->
                        val answered = selected >= 0
                        val color = when {
                            !answered -> Palette.Card
                            i == q.answerIndex -> Color(0xFF1F4D36)
                            i == selected -> Color(0xFF5A2A2A)
                            else -> Palette.Card
                        }
                        Card(
                            onClick = { if (!answered) { selected = i; if (i == q.answerIndex) score++ } },
                            colors = CardDefaults.cardColors(containerColor = color),
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(option, Modifier.padding(14.dp)) }
                    }
                    if (selected >= 0) {
                        Text(if (selected == q.answerIndex) T.correct else T.wrong, color = if (selected == q.answerIndex) Palette.Success else Palette.Error)
                        Text(q.explanation)
                        Button(onClick = {
                            if (index + 1 < questions.size) { index++; selected = -1 } else finished = true
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (index + 1 < questions.size) T.next else T.finish) }
                    }
                }
            }
        }
    }
}


