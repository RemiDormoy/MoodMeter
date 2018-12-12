package com.rdo.octo.moodmeter

import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    companion object {
        const val PREF_FCM_TOKEN = "bzrebuvibzreiv"

        fun getToken(context: Context): String? {
            return context.getSharedPreferences("com.octo.rdo.moodmeter", Context.MODE_PRIVATE).getString(PREF_FCM_TOKEN, "empty")
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val sharedPreferences = getSharedPreferences("com.octo.rdo.moodmeter", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(PREF_FCM_TOKEN, p0).apply()
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
        startActivity(MainActivity.newIntent(this, p0.data["question"]))
    }

}