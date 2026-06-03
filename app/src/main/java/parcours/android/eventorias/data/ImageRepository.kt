package parcours.android.eventorias.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class ImageRepository {

    fun createImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, "camera_photos")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File.createTempFile(
            "captured_photo_",
            ".jpg",
            directory
        )

        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}