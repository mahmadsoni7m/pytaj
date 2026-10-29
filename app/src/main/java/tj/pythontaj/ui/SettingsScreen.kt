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

@Composable
fun SettingsScreen(vm: MainViewModel) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    ScreenScaffold(T.settings) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppCard {
                Text(T.about, style = MaterialTheme.typography.titleSmall)
                Text(T.aboutBody)
                Text("Нусха: 0.1.0", style = MaterialTheme.typography.bodySmall)
            }
            AppCard {
                Text(T.limits, style = MaterialTheme.typography.titleSmall)
                Text(T.limitsBody)
            }
            OutlinedButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) { Text(T.resetProgress) }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(T.resetConfirmTitle) },
            text = { Text(T.resetConfirmBody) },
            confirmButton = { TextButton(onClick = { vm.resetProgress(); confirm = false }) { Text(T.delete) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(T.cancel) } },
        )
    }
}
