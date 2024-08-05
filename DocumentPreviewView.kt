import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

@Composable
fun DocumentPreviewView(uri: Uri) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uri.toFile().extension.lowercase()) {
            "pdf" -> PDFKitView(uri = uri)
            "jpg", "jpeg", "png", "gif" -> Image(
                bitmap = BitmapFactory.decodeFile(uri.path).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
            else -> {
                Text(text = "Unsupported file type", modifier = Modifier.padding(16.dp))
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show()
            }
        }

        Button(
            onClick = {
                (context as? Activity)?.finish()
            },
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Close")
        }
    }
}

@Composable
fun PDFKitView(uri: Uri) {
    AndroidView(factory = { context ->
        PDFView(context, null).apply {
            fromUri(uri).load()
        }
    }, update = { pdfView ->
        pdfView.fromUri(uri).load()
    })
}

fun Uri.toFile(): File = File(this.path ?: "")
