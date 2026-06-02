package parcours.android.eventorias

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import parcours.android.eventorias.data.ImageRepository

class MainViewModel(
    private val imageRepository: ImageRepository
): ViewModel() {

    fun generateImageUri(context: Context): Uri {
        return imageRepository.createImageUri(context)
    }
}