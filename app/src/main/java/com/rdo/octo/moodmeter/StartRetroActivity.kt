package com.rdo.octo.moodmeter

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_new_retro.*
import kotlinx.android.synthetic.main.cell_user.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.OnSuccessListener
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter


class StartRetroActivity : AppCompatActivity() {

    private val adapter = UserAdapter()

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    companion object {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_retro)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter
        launch {
            db.collection("users")
                .get()
                .addOnCompleteListener { task ->
                    val result = task.result
                    if (task.isSuccessful && result != null) {
                        val list = result.map {
                            val data = it.data
                            User((data["token"] as String), (data["name"] as String), (data["avatar"] as Long).toInt())
                        }
                        launch(UI) { adapter.setList(list) }
                        progressBar.visibility = View.GONE
                        userRecyclerView.visibility = View.VISIBLE
                    } else {
                        task.exception?.printStackTrace()
                    }
                }
        }
        floatingActionButton.setOnClickListener {
            if (adapter.getSelectedUsers().isNotEmpty()) {
                goToNextStep()
            } else {
                Toast.makeText(this, "On peut pas faire une rétro à 0 narvalo !", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToNextStep() {
        val list = adapter.getSelectedUsers()
        CurrentRetroActivity.setCurrentUsersInRetro(list)
        val name = AddUserActivity.getName(this@StartRetroActivity)
        launch {
            FirebaseFirestore.getInstance()
            val idRetro = name + "|" + LocalDateTime.now().format(StartRetroActivity.dateFormatter)
            val retro = mapOf(
                "idRetro" to idRetro,
                "users" to list.map { it.token }
            )
            db.collection("retro")
                .add(retro)
                .addOnSuccessListener { documentReference ->
                    launch(UI) {
                        startActivity(CurrentRetroActivity.newIntent(this@StartRetroActivity, idRetro))
                        finish()
                    }
                }
                .addOnFailureListener { e -> e.printStackTrace() }
        }
    }
}

data class User(
    val token: String,
    val name: String,
    val avatar: Int
)

class UserAdapter : RecyclerView.Adapter<UserViewHolder>() {

    private var list = mutableListOf<Pair<User, Boolean>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: UserViewHolder, position: Int) {
        val user = list[position]
        with(p0.itemView) {
            userNameTextView.text = user.first.name
            val anim = when (user.first.avatar) {
                1 -> "animations/funky_chicken.json"
                2 -> "animations/dino_dance.json"
                3 -> "animations/acrobatics.json"
                else -> "animations/techno_penguin.json"
            }
            avatarLottieView.setAnimation(anim)
            avatarLottieView.playAnimation()
            avatarLottieView.loop(true)
            userCheckBox.isChecked = user.second
            cellContainer.setOnClickListener {
                userCheckBox.isChecked = !userCheckBox.isChecked
            }
            userCheckBox.setOnCheckedChangeListener { _, isChecked ->
                list[position] = user.first to isChecked
            }
        }
    }

    fun setList(list: List<User>) {
        this.list = list.map { it to false }.toMutableList()
        notifyDataSetChanged()
    }

    fun getSelectedUsers(): List<User> {
        return list.filter { it.second }.map { it.first }
    }
}

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)