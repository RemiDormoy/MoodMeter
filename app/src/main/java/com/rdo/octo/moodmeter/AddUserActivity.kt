package com.rdo.octo.moodmeter

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_user.*

class AddUserActivity : AppCompatActivity() {

    companion object {
        const val PREF_USER_NAME = "lolilol le nom"
        const val PREF_USER_AVATAR = "lolilol le avatar"

        fun getName(context: Context): String? {
            return context.getSharedPreferences("com.octo.rdo.moodmeter", Context.MODE_PRIVATE).getString(PREF_USER_NAME, null)
        }
        fun getAvatar(context: Context): Int {
            return context.getSharedPreferences("com.octo.rdo.moodmeter", Context.MODE_PRIVATE).getInt(PREF_USER_AVATAR, 1)
        }
    }

    private var selectedAvatar = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)
        profileContainerView1.setOnClickListener {
            selectedAvatar = 1
            alphaView1.visibility = View.VISIBLE
            alphaView1.animate().alpha(0.4f).setDuration(1000L).start()
            checkView1.visibility = View.VISIBLE
            checkView1.animate().alpha(1f).setDuration(1000L).start()
            listOf(alphaView2, checkView2, alphaView3, checkView3, alphaView4, checkView4).forEach {
                it.animateNotChecked()
            }
        }
        profileContainerView2.setOnClickListener {
            selectedAvatar = 2
            alphaView2.visibility = View.VISIBLE
            alphaView2.animate().alpha(0.4f).setDuration(1000L).start()
            checkView2.visibility = View.VISIBLE
            checkView2.animate().alpha(1f).setDuration(1000L).start()
            listOf(alphaView1, checkView1, alphaView3, checkView3, alphaView4, checkView4).forEach {
                it.animateNotChecked()
            }
        }
        profileContainerView3.setOnClickListener {
            selectedAvatar = 3
            alphaView3.visibility = View.VISIBLE
            alphaView3.animate().alpha(0.4f).setDuration(1000L).start()
            checkView3.visibility = View.VISIBLE
            checkView3.animate().alpha(1f).setDuration(1000L).start()
            listOf(alphaView1, checkView1, alphaView2, checkView2, alphaView4, checkView4).forEach {
                it.animateNotChecked()
            }
        }
        profileContainerView4.setOnClickListener {
            selectedAvatar = 4
            alphaView4.visibility = View.VISIBLE
            alphaView4.animate().alpha(0.4f).setDuration(1000L).start()
            checkView4.visibility = View.VISIBLE
            checkView4.animate().alpha(1f).setDuration(1000L).start()
            listOf(alphaView1, checkView1, alphaView3, checkView3, alphaView2, checkView2).forEach {
                it.animateNotChecked()
            }
        }
        addButton.setOnClickListener {
            val name = nameEditText.text?.toString()
            when {
                name == null -> Toast.makeText(
                    this,
                    "Met un nom stp, c'est pas le zoo ici !",
                    Toast.LENGTH_LONG
                ).show()
                name.isBlank() -> Toast.makeText(
                    this,
                    "Met un nom stp, c'est pas le zoo ici !",
                    Toast.LENGTH_LONG
                ).show()
                selectedAvatar == 0 -> Toast.makeText(
                    this,
                    "Choisi un avatar, sinon tu ressembles à rien !",
                    Toast.LENGTH_LONG
                ).show()
                else -> createUserAndStoreIt(name, selectedAvatar)
            }
        }
    }

    private fun createUserAndStoreIt(name: String, selectedAvatar: Int) {
        val db = FirebaseFirestore.getInstance()
        val token = MessagingService.getToken(this)
        val user = mapOf(
            "name" to name,
            "avatar" to selectedAvatar,
            "token" to token
        )
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                val sharedPreferences = getSharedPreferences("com.octo.rdo.moodmeter",
                    Context.MODE_PRIVATE
                )
                sharedPreferences.edit()
                    .putInt(PREF_USER_AVATAR, selectedAvatar)
                    .putString(PREF_USER_NAME, name)
                    .apply()
                finish()
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    private fun View.animateNotChecked() {
        this.animate().alpha(0f).setDuration(1000L).start()
    }
}