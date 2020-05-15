package it.polito.mad.mhackeroni

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class MessagingService : FirebaseMessagingService() {

    private   val channelId = "reuseitappchannelid"

    override fun onNewToken(token: String) {
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE
        repo.updateUserToken(repo.getID(this), token)

         Log.d("MAD2020", "Token: ${token}")
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("MAD2020", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d("MAD2020", "Message data payload: " + remoteMessage.data)

            /*
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }

             */
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("MAD2020", "Message Notification Body: ${it.body}")

            createNotificationChannel()


            var builder = NotificationCompat.Builder(this, it.channelId ?: channelId)
                .setSmallIcon(R.drawable.ic_shopping_cart)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(it.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(9009, builder.build())
            }

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channelName)
            val descriptionText = getString(R.string.channelDesc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}