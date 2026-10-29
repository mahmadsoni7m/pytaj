package tj.pythontaj.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tj.pythontaj.MainViewModel
import tj.pythontaj.domain.Lesson

@Composable
fun LessonRow(l: Lesson, done: Boolean, bookmarked: Boolean, onClick: () -> Unit) {
    AppCard(Modifier.clickable(onClick = onClick)) {
        Text((if (done) "✓ " else "") + l.title + (if (bookmarked) " ★" else ""), style = MaterialTheme.typography.titleSmall)
        Text("Душворӣ: ${l.difficulty}  •  Машқҳо: ${l.exercises.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PathScreen(vm: MainViewModel, onOpenModule: (String) -> Unit, onOpenLesson: (String) -> Unit) {
    ScreenScaffold(T.path) { pad ->
        ContentGate(vm, Modifier.padding(pad)) { cur ->
            val done by vm.completedLessons.collectAsState()
            val marks by vm.bookmarks.collectAsState()
            var query by rememberSaveable { mutableStateOf("") }
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), singleLine = true, label = { Text(T.search) })
                Spacer(Modifier.height(8.dp))
                if (query.isNotBlank()) {
                    val found = cur.search(query)
                    if (found.isEmpty()) Text(T.nothingFound)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(found, key = { it.id }) { l -> LessonRow(l, l.id in done, l.id in marks) { onOpenLesson(l.id) } }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cur.modules.sortedBy { it.order }, key = { it.id }) { m ->
                            val ls = cur.lessonsOf(m.id)
                            AppCard(Modifier.clickable { onOpenModule(m.id) }) {
                                Text("${m.order}. ${m.title}", style = MaterialTheme.typography.titleSmall)
                                Text(m.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text(if (ls.isEmpty()) "Дарсҳо ҳанӯз нестанд" else "${ls.count { it.id in done }}/${ls.size} дарс анҷом ёфт")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleScreen(vm: MainViewModel, moduleId: String, onBack: () -> Unit, onOpenLesson: (String) -> Unit) {
    ContentGate(vm) { cur ->
        val module = cur.modules.firstOrNull { it.id == moduleId }
        val done by vm.completedLessons.collectAsState()
        val marks by vm.bookmarks.collectAsState()
        ScreenScaffold(module?.title ?: T.path, onBack = onBack) { pad ->
            val lessons = cur.lessonsOf(moduleId)
            if (lessons.isEmpty()) {
                Text(T.noLessonsInModule, Modifier.padding(pad).padding(16.dp))
            } else {
                LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text(module?.description.orEmpty(), Modifier.padding(vertical = 8.dp)) }
                    items(lessons, key = { it.id }) { l -> LessonRow(l, l.id in done, l.id in marks) { onOpenLesson(l.id) } }
                }
            }
        }
    }
}
