import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.core.IPlatformFile

@Composable
fun PhotoItem(
    file: IPlatformFile,
    onSaveFile: (IPlatformFile) -> Unit,
) {
    var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }
    var showName by remember { mutableStateOf(false) }

    LaunchedEffect(file) {
        bytes = file.openInputStream()?.use {
            ByteArray(file.getLength().toInt()).apply {
                it.readAtMostTo(this)
            }
        }
    }

    Surface(
        onClick = { showName = !showName },
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape = MaterialTheme.shapes.medium)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            bytes?.let {
                AsyncImage(
                    bytes,
                    contentDescription = file.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()

                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                IconButton(
                    onClick = { onSaveFile(file) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Default.Check,
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = showName,
                modifier = Modifier.padding(4.dp).align(Alignment.BottomStart)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        file.name,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}
