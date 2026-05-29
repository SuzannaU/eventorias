package parcours.android.eventorias.data

import android.net.Uri
import com.google.android.gms.tasks.Task
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

    fun addEvent(event: Event, pictureUri: Uri?) {
        if (pictureUri != null) {
            uploadPicture(pictureUri).addOnSuccessListener { uri ->
                val eventToSave = event.copy(
                    pictureUrl = uri.toString()
                )
                firestore.collection(EVENT_COLLECTION).document(event.eventId)
                    .set(eventToSave)
            }
        } else {
            firestore.collection(EVENT_COLLECTION).document(event.eventId)
                .set(event)
        }
    }

    private fun uploadPicture(pictureUri: Uri): Task<Uri> {

        val uuid = UUID.randomUUID().toString()
        val storageRef = storage.reference
        val pictureRef = storageRef.child(PICTURE_PATH).child(uuid)

        val uploadTask = pictureRef.putFile(pictureUri)
        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            pictureRef.downloadUrl
        }
        return urlTask
    }
}