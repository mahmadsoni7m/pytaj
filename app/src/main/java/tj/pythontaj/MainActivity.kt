package tj.pythontaj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import tj.pythontaj.ui.PythonTajApp
import tj.pythontaj.ui.PythonTajTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PythonTajTheme {
                Surface(modifier = Modifier.fillMaxSize()) { PythonTajApp(viewModel) }
            }
        }
    }
}
