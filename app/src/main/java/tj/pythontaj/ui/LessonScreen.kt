package tj.pythontaj.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tj.pythontaj.MainViewModel

@Composable
fun LessonScreen(
    vm: MainViewModel,
    lessonId: String,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenEditor: () -> Unit,
) {
    ContentGate(vm) { cur ->
        val lesson = cur.lesson(lessonId)
        val marks by vm.bookmarks.collectAsState()
        val done by vm.completedLessons.collectAsState()
        val passed by vm.passedExercises.collectAsState()
        ScreenScaffold(
            lesson?.title ?: T.path,
            onBack = onBack,
            actions = {
                if (lesson != null) {
                    IconButton(onClick = { vm.toggleBookmark(lesson.id) }) {
                        Icon(
                            if (lesson.id in marks) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = T.bookmark,
                        )
                    }
                }
            },
        ) { pad ->
            if (lesson == null) {
                Text(T.nothingFound, Modifier.padding(pad).padding(16.dp))
                return@ScreenScaffold
            }
            LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    if (lesson.id in done) Text("✓ ${T.lessonDone}", color = Palette.Success)
                    SectionTitle(T.objectives)
                    lesson.objectives.forEach { Text("• $it") }
                    if (lesson.prerequisites.isNotEmpty()) {
                        SectionTitle(T.prerequisites)
                        lesson.prerequisites.forEach { Text("• " + (cur.lesson(it)?.title ?: it)) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(lesson.introduction)
                }
                lesson.sections.forEach { s ->
                    item {
                        SectionTitle(s.heading)
                        Text(s.body)
                    }
                }
                item { SectionTitle(T.examples) }
                lesson.examples.forEach { e ->
                    item {
                        AppCard {
                            Text(e.title, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            CodeBlock(e.code)
                            Spacer(Modifier.height(6.dp))
                            Text(e.explanation)
                            Spacer(Modifier.height(6.dp))
                            Text(T.expectedOutput, style = MaterialTheme.typography.labelLarge)
                            CodeBlock(e.expectedOutput)
                            TextButton(onClick = { vm.editorCode.value = e.code; onOpenEditor() }) { Text(T.openInEditor) }
                        }
                    }
                }
                item {
                    SectionTitle(T.commonMistakes)
                    lesson.commonMistakes.forEach { Text("• $it") }
                    SectionTitle(T.summary)
                    Text(lesson.summary)
                    SectionTitle(T.completion)
                    Text(lesson.completionCriteria)
                    SectionTitle(T.exercises)
                }
                lesson.exercises.forEach { ex ->
                    item {
                        OutlinedButton(onClick = { onOpenExercise(ex.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text((if (ex.id in passed) "✓ " else "") + ex.title)
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    if (lesson.quiz.isNotEmpty()) {
                        Button(onClick = { onOpenQuiz(lesson.id) }, modifier = Modifier.fillMaxWidth()) { Text(T.quiz) }
                    }
                    if (lesson.exercises.isEmpty() && lesson.id !in done) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.completeLesson(lesson.id) }, modifier = Modifier.fillMaxWidth()) { Text(T.finish) }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
