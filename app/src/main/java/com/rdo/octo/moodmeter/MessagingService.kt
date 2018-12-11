package com.rdo.octo.moodmeter

import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("Message firebase reçu", p0.data.toString())
        /*val notification = NotificationCompat.Builder(this, "iubvierv")
            .setSmallIcon(R.drawable.ic_love)
            .setContentTitle(p0.data.get("title") ?: "pas de titre")
            .setContentText(p0.data.get("message") ?: "pas de message")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(this).notify(12, notification)*/
        startActivity(Intent(this, MainActivity::class.java))
    }
}