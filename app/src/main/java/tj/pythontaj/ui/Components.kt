package tj.pythontaj.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.pythontaj.ConsoleState
import tj.pythontaj.ContentState
import tj.pythontaj.MainViewModel
import tj.pythontaj.content.Curriculum
import tj.pythontaj.domain.RunStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = T.back) }
                    }
                },
                actions = actions,
            )
        },
        content = content,
    )
}

/** Ҳолатҳои боркунӣ / хато / тайёр барои мазмуни курс. */
@Composable
fun ContentGate(vm: MainViewModel, modifier: Modifier = Modifier, ready: @Composable (Curriculum) -> Unit) {
    val state by vm.content.collectAsState()
    when (val s = state) {
        is ContentState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text(T.loading)
            }
        }
        is ContentState.Error -> Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(s.message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { vm.loadContent() }) { Text(T.retry) }
        }
        is ContentState.Ready -> Box(modifier) { ready(s.curriculum) }
    }
}

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Palette.Card),
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
}

@Composable
fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    Surface(color = Palette.Code, shape = RoundedCornerShape(8.dp), modifier = modifier.fillMaxWidth()) {
        SelectionContainer {
            androidx.compose.material3.Text(
                code,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Palette.CodeText,
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(12.dp),
            )
        }
    }
}

@Composable
fun CodeEditor(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        minLines = 8,
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
        label = { Text("Python") },
    )
}

private fun statusLabel(s: RunStatus): String = when (s) {
    RunStatus.OK -> "Иҷро шуд"
    RunStatus.SYNTAX_ERROR -> "Хатои синтаксис"
    RunStatus.RUNTIME_ERROR -> "Хатои иҷро"
    RunStatus.TIMEOUT -> "Вақт тамом шуд: барнома хеле дароз кор кард"
    RunStatus.STOPPED -> "Қатъ карда шуд"
    RunStatus.OUTPUT_LIMIT -> "Баромад аз ҳад зиёд аст ва бурида шуд"
    RunStatus.UNSUPPORTED -> "Ин имконият дастгирӣ намешавад"
    RunStatus.ENGINE_ERROR -> "Хатои муҳаррики Python"
}

@Composable
fun ConsoleView(state: ConsoleState, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Text(T.output, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        val r = state.result
        when {
            state.running -> Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp)); Text(T.running)
            }
            r == null -> Text(T.consoleEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> {
                Text(statusLabel(r.status), color = if (r.status == RunStatus.OK) Palette.Success else Palette.Error)
                if (r.stdout.isNotEmpty()) { Spacer(Modifier.height(6.dp)); CodeBlock(r.stdout) }
                if (r.stderr.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(r.stderr, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Palette.Error)
                }
                state.help?.let { h ->
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(h.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(h.explanation)
                    h.causes.forEach { Text("• $it") }
                    if (h.badExample.isNotBlank()) {
                        Spacer(Modifier.height(6.dp)); Text("Нодуруст:"); CodeBlock(h.badExample)
                        Spacer(Modifier.height(4.dp)); Text("Дуруст:"); CodeBlock(h.goodExample)
                    }
                }
            }
        }
    }
}
