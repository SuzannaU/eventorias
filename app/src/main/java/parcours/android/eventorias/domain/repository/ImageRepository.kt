package parcours.android.eventorias.domain.repository

import android.content.Context
import android.net.Uri

interface ImageRepository {

    fun createImageUri(context: Context): Uri
}