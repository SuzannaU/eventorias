package parcours.android.eventorias.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import parcours.android.eventorias.R
import parcours.android.eventorias.domain.service.NotificationService
import parcours.android.eventorias.ui.MainActivity
import parcours.android.eventorias.ui.screen.profile.FCM_ALL_TOPICS

private const val TAG = "TAG NotificationService"

class FcmNotificationService(
    private val firebaseMessaging: FirebaseMessaging,
) : FirebaseMessagingService(), NotificationService {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let {
            it.body?.let { msg -> Log.i(TAG, msg) }
            sendVisualNotification(it)
        }
    }

    private fun sendVisualNotification(notification: RemoteMessage.Notification) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    getString(R.string.eventorias_messages),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )

            notificationManager.createNotificationChannel(channel)
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun subscribeToTopic(topic: String) {
        firebaseMessaging.subscribeToTopic(FCM_ALL_TOPICS)
    }

    override fun unsubscribeFromTopic(topic: String) {
        firebaseMessaging.unsubscribeFromTopic(FCM_ALL_TOPICS)
    }
}
