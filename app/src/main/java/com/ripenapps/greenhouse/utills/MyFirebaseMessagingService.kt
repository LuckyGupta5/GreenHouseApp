package com.ripenapps.greenhouse.utills

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.screen.SplashActivity
import com.ripenapps.greenhouse.screen.bidder.BidderNotification
import com.ripenapps.greenhouse.screen.bidder.Home
import com.ripenapps.greenhouse.screen.seller.HomeSeller
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle received message
        if (remoteMessage != null) {
            Log.d(TAG, "From: ${remoteMessage.notification}")

            Log.e("data1", Gson().toJson(remoteMessage))
            var message = ""
            var title = ""
            var action = ""
            var bidId = ""
            var type = ""
            if (remoteMessage.data.isNotEmpty()) {
                message = remoteMessage.data.getOrDefault("message", "").trim()
                title = remoteMessage.data.getOrDefault("title", "").trim()
                bidId = remoteMessage.data.getOrDefault("bidId", "").trim()
                type = remoteMessage.data.getOrDefault("type", "").trim()
            } else {
                Log.d(TAG, "onMessageReceived: no data found")
            }


//            if (remoteMessage.getData()["message"] != null) {
//                message = remoteMessage.data.getOrDefault("message", "").trim()
//                title = remoteMessage.data.getOrDefault("title", "").trim()
//                bidId = remoteMessage.data.getOrDefault("bidId", "").trim()
//                type = remoteMessage.data.getOrDefault("type", "").trim()
//                // action=remoteMessage.getData().get("action").trim()
//            } else if (remoteMessage.getData()["body"] != null) {
//                title = remoteMessage.data.getOrDefault("title", "").trim()
//                message = remoteMessage.data.getOrDefault("body", "").trim()
//                bidId = remoteMessage.data.getOrDefault("bidId", "").trim()
//                type = remoteMessage.data.getOrDefault("type", "").trim()
//                /*  title = remoteMessage.getNotification().getTitle();
//              message = remoteMessage.getNotification().getBody();*/
//                // action=remoteMessage.getNotification().getClickAction();
//            }

            Log.d(TAG, "onMessageReceived: $bidId $type")//66757732044ba6a354e65db9

            remoteMessage.notification?.let {
                showNotification(
                    this,
                    remoteMessage.notification?.title,
                    remoteMessage.notification?.body,
                    bidId,
                    type
                )
            }
        }
    }


    private fun showNotification(
        context: Context,
        title: String?,
        body: String?,
        bidId: String? = null,
        type: String? = null
    ) {
        try {
            val notificationId = Random.nextInt(1000, 9999)
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel if Android version is Oreo or higher
            val channelId = "GreenHouse"
            val channelName = "GreenHouse Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            var channel = notificationManager.getNotificationChannel(channelId)

            if (channel == null) {
                channel = NotificationChannel(channelId, channelName, importance)
                channel.enableLights(true)
                channel.lightColor = Color.RED
                channel.enableVibration(true)
                notificationManager.createNotificationChannel(channel)
            }

            val intent: Intent =
                if (Preferences.getBooleanPreference(applicationContext, "isBidder")) {
                    if (body?.contains("rishabh") == true) {//go to bidderNotification
                        Intent(baseContext, BidderNotification::class.java)
                    } else {
                        Intent(baseContext, Home::class.java)
                    }
                } else if (Preferences.getBooleanPreference(applicationContext, "isSeller")) {
                    if (body?.contains("rishabh") == true) {//go to bidderNotification
                        Intent(baseContext, BidderNotification::class.java)
                    } else {
                        Intent(baseContext, HomeSeller::class.java)
                    }
                } else {
                    Intent(baseContext, SplashActivity::class.java)
                }

            if (!bidId.isNullOrEmpty() && !type.isNullOrEmpty()) {
                intent.putExtra("type", type)
                intent.putExtra("bidId", bidId)
            }
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
                )
            }
            // var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "GreenHouse")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(soundUri)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setSmallIcon(R.drawable.ic_stat_name)
//                    .setColor(R.drawable.bg_notification)
                    .setColor(resources.getColor(R.color.greenhousetheme))
                    .setContentIntent(pendingIntent)
                    .setGroupSummary(true)

            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Get updated InstanceID token.
        Log.d(TAG, "Refreshed token: $token")
        Preferences.setStringPreference(applicationContext, "deviceToken", token)
        // You can send the token to your server for sending push notifications.
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}