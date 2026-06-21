package parcours.android.eventorias.domain.service

interface NotificationService {

    fun subscribeToTopic(topic: String)
    fun unsubscribeFromTopic(topic: String)
}