package tj.pythontaj.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import tj.pythontaj.MainViewModel

@Composable
fun EditorScreen(vm: MainViewModel) {
    val code by vm.editorCode.collectAsState()
    val console by vm.console.collectAsState()
    val snippets by vm.snippets.collectAsState()
    val context = LocalContext.current

    ScreenScaffold(T.editor) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CodeEditor(code, { vm.editorCode.value = it })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.runEditor() }, enabled = !console.running) { Text(T.run) }
                OutlinedButton(onClick = { vm.stop() }, enabled = console.running) { Text(T.stop) }
                TextButton(onClick = { vm.editorCode.value = ""; vm.clearConsole() }) { Text(T.reset) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("PythonTAJ", code))
                }) { Text(T.copy) }
                OutlinedButton(onClick = { vm.saveSnippet() }) { Text(T.save) }
            }
            ConsoleView(console)
            SectionTitle(T.mySnippets)
            if (snippets.isEmpty()) Text(T.noSnippets, color = MaterialTheme.colorScheme.onSurfaceVariant)
            snippets.forEach { s ->
                AppCard {
                    Text(s.title, style = MaterialTheme.typography.titleSmall)
                    Row {
                        TextButton(onClick = { vm.editorCode.value = s.code }) { Text(T.openInEditor) }
                        TextButton(onClick = { vm.deleteSnippet(s.id) }) { Text(T.delete) }
                    }
                }
            }
        }
    }
}
