package dev.orestegabo.sequo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.orestegabo.sequo.theme.sequoColorScheme
import dev.orestegabo.sequo.ui.app.SequoShell

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = sequoColorScheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SequoShell()
        }
    }
}
