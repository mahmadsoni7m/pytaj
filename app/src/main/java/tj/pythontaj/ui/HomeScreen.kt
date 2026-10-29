package tj.pythontaj.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.pythontaj.MainViewModel
import tj.pythontaj.domain.ProgressCalculator

@Composable
fun HomeScreen(vm: MainViewModel, onOpenLesson: (String) -> Unit, onGo: (String) -> Unit) {
    ScreenScaffold(T.appName) { pad ->
        ContentGate(vm, Modifier.padding(pad)) { cur ->
            val done by vm.completedLessons.collectAsState()
            val streak by vm.streak.collectAsState()
            val doneCount = cur.lessons.count { it.id in done }
            val percent = ProgressCalculator.percent(doneCount, cur.lessons.size)
            val (levelNo, levelName) = ProgressCalculator.level(percent)
            val next = cur.lessons.firstOrNull { it.id !in done }

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(56.dp).background(Palette.Accent, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Text("Py", color = Palette.Background, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(T.welcome, style = MaterialTheme.typography.titleMedium)
                        Text(T.welcomeSub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                AppCard {
                    Text("${T.level} $levelNo: $levelName", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text("${T.progress}: $percent%  •  ${T.completedLessons}: $doneCount/${cur.lessons.size}")
                    Text("${T.streak}: $streak ${T.days}")
                }
                if (next != null) {
                    Button(onClick = { onOpenLesson(next.id) }, modifier = Modifier.fillMaxWidth()) { Text("${T.continueLearning}: ${next.title}") }
                } else {
                    Text(T.allDone, color = Palette.Success)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { onGo("path") }, modifier = Modifier.weight(1f)) { Text(T.path) }
                    OutlinedButton(onClick = { onGo("editor") }, modifier = Modifier.weight(1f)) { Text(T.editor) }
                    OutlinedButton(onClick = { onGo("settings") }, modifier = Modifier.weight(1f)) { Text(T.settings) }
                }
            }
        }
    }
}
