package it.polito.mad.mhackeroni.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.view.MainActivity

class MessagingService : FirebaseMessagingService() {

    private   val channelId = "reuseitappchannelid"

    override fun onNewToken(token: String) {
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE
        var id = repo.getID(this)

        if(!id.isNullOrEmpty()) {
            repo.updateUserToken(repo.getID(this), token)
        }

        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.

        remoteMessage.notification?.let {
            if(!it.body.isNullOrEmpty()){
                if(remoteMessage.data.isNotEmpty()){
                    val itemID = remoteMessage.data.get("item")
                    if(!itemID.isNullOrEmpty()){
                        sendNotification(it.body!!, itemID)
                    }
                } else {
                    sendNotification(it.body!!)
                }
            }

        }
    }


    private fun sendNotification(messageBody: String, itemID: String? = null) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        if(!itemID.isNullOrEmpty()){
            intent.putExtra("goto", itemID)
            Log.d("MAD2020","Service put: ${itemID}")
        }

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.channeld_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_shopping_cart)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "ReuseIt channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}