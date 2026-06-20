package parcours.android.eventorias.data

import android.net.Uri
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import parcours.android.eventorias.domain.exceptions.DatabaseException
import parcours.android.eventorias.domain.exceptions.NetworkException
import parcours.android.eventorias.domain.model.Event
import java.util.UUID

const val EVENT_COLLECTION = "events"
const val PICTURE_PATH = "pictures"

class EventRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    suspend fun getEventById(postId: String): Event? {
        try {
            return firestore
                .collection(EVENT_COLLECTION)
                .document(postId)
                .get()
                .await()
                .toObject<Event>()
        } catch (e: Exception) {
            e.printStackTrace()
            val customException = when (e) {
                is FirebaseNetworkException -> NetworkException(
                    e.message ?: "Network error during authentication"
                )

                is FirebaseFirestoreException -> DatabaseException(
                    e.message ?: "Firestore Error during user update"
                )

                else -> Exception(e.message)
            }
            throw customException
        }
    }

    fun getEvents(): Flow<List<Event>> {
        try {
            val events = firestore
                .collection(EVENT_COLLECTION)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .dataObjects<Event>()
            return events
        } catch (e: Exception) {
            e.printStackTrace()
            val customException = when (e) {
                is FirebaseNetworkException -> NetworkException(
                    e.message ?: "Network error during authentication"
                )

                is FirebaseFirestoreException -> DatabaseException(
                    e.message ?: "Firestore Error during user update"
                )

                else -> Exception(e.message)
            }
            throw customException
        }
    }

    suspend fun addEvent(event: Event, pictureUri: Uri?) {
        try {
            val newDocRef = firestore.collection(EVENT_COLLECTION).document()
            val generatedId = newDocRef.id
            var eventToSave = event.copy(eventId = generatedId)

            if (pictureUri != null) {
                val downloadUri = uploadPicture(pictureUri)
                eventToSave = eventToSave.copy(pictureUrl = downloadUri.toString())
            }

            newDocRef.set(eventToSave).await()
        } catch (e: Exception) {
            e.printStackTrace()
            val customException = when (e) {
                is FirebaseNetworkException -> NetworkException(
                    e.message ?: "Network error during authentication"
                )

                is FirebaseFirestoreException -> DatabaseException(
                    e.message ?: "Firestore Error during user update"
                )

                else -> Exception(e.message)
            }
            throw customException
        }
    }

    private suspend fun uploadPicture(pictureUri: Uri): Uri {
        val uuid = UUID.randomUUID().toString()
        val pictureRef = storage.reference.child(PICTURE_PATH).child(uuid)

        pictureRef.putFile(pictureUri).await()
        return pictureRef.downloadUrl.await()
    }
}