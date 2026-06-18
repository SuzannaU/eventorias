package parcours.android.eventorias.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import parcours.android.eventorias.domain.model.Event
import java.util.UUID

const val EVENT_COLLECTION = "events"
const val PICTURE_PATH = "pictures"

class EventRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    suspend fun getEventById(postId: String): Event? {
        return firestore
            .collection(EVENT_COLLECTION)
            .document(postId)
            .get()
            .await()
            .toObject<Event>()
    }

    fun getEvents(): Flow<List<Event>> {
        val events = firestore
            .collection(EVENT_COLLECTION)
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .dataObjects<Event>()
        return events
    }

    suspend fun addEvent(event: Event, pictureUri: Uri?) {

        val newDocRef = firestore.collection(EVENT_COLLECTION).document()
        val generatedId = newDocRef.id
        var eventToSave = event.copy(eventId = generatedId)

        if (pictureUri != null) {
            val downloadUri = uploadPicture(pictureUri)
            eventToSave = eventToSave.copy(pictureUrl = downloadUri.toString())
        }

        newDocRef.set(eventToSave).await()
    }

    private suspend fun uploadPicture(pictureUri: Uri): Uri {

        val uuid = UUID.randomUUID().toString()
        val pictureRef = storage.reference.child(PICTURE_PATH).child(uuid)

        pictureRef.putFile(pictureUri).await()
        return pictureRef.downloadUrl.await()
    }
}