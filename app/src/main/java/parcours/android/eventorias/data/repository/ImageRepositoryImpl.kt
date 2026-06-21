package parcours.android.eventorias.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import parcours.android.eventorias.domain.repository.ImageRepository
import java.io.File

class ImageRepositoryImpl : ImageRepository {

    override fun createImageUri(context: Context): Uri {
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