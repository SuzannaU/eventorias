package parcours.android.eventorias.data.datasource

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import parcours.android.eventorias.data.dto.EventDto
import java.util.UUID

private const val EVENT_COLLECTION = "events"
private const val PICTURE_PATH = "pictures"

class FirebaseEventDataSource(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : EventDataSource {

    override suspend fun getEventById(eventId: String): EventDto? {
        return firestore
            .collection(EVENT_COLLECTION)
            .document(eventId)
            .get()
            .await()
            .toObject<EventDto>()
    }

    override fun getEvents(): Flow<List<EventDto>> {
        return firestore
            .collection(EVENT_COLLECTION)
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .dataObjects<EventDto>()
    }

    override suspend fun saveEvent(event: EventDto) {
        val docRef = if (event.eventId.isEmpty()) {
            firestore.collection(EVENT_COLLECTION).document()
        } else {
            firestore.collection(EVENT_COLLECTION).document(event.eventId)
        }
        
        val eventToSave = if (event.eventId.isEmpty()) {
            event.copy(eventId = docRef.id)
        } else {
            event
        }
        
        docRef.set(eventToSave).await()
    }

    override suspend fun uploadEventPicture(pictureUri: Uri): Uri {
        val uuid = UUID.randomUUID().toString()
        val pictureRef = storage.reference.child(PICTURE_PATH).child(uuid)
        pictureRef.putFile(pictureUri).await()
        return pictureRef.downloadUrl.await()
    }
}
