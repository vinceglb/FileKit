package io.github.vinceglb.filekit.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.launch

@Composable
actual fun WebDownloadSection(
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf("hello.txt") }
    var content by remember { mutableStateOf("Hello, web!") }
    var status by remember { mutableStateOf<String?>(null) }

    FeatureCard(title = "Web download", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("File name") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Bytes content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Button(
                onClick = {
                    scope.launch {
                        FileKit.download(content.encodeToByteArray(), fileName.ifBlank { "file.txt" })
                        status = "Download triggered"
                    }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Download")
            }

            if (status != null) {
                Text(status.orEmpty())
            }
        }
    }
}
