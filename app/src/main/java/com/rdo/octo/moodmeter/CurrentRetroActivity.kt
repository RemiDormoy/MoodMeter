package com.rdo.octo.moodmeter

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.rdo.octo.moodmeter.CurrentRetroActivity.Companion.EXTRA_ID
import com.rdo.octo.moodmeter.utils.CategoryDonut
import com.readystatesoftware.chuck.ChuckInterceptor
import kotlinx.android.synthetic.main.activity_current_retro.*
import kotlinx.android.synthetic.main.cell_question.view.*
import kotlinx.coroutines.experimental.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener



class CurrentRetroActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra id yolo"

        var usersInRetro: List<User>? = null
        fun setCurrentUsersInRetro(list: List<User>) {
            usersInRetro = list
        }

        private var client: OkHttpClient? = null


        fun getOkhttpClient(context: Context) = if (client == null) {
            client = OkHttpClient.Builder()
                .addInterceptor(ChuckInterceptor(context))
                .build()
            client
        } else {
            client
        }

        fun newIntent(context: Context, id: String): Intent {
            return Intent(context, CurrentRetroActivity::class.java).putExtra(EXTRA_ID, id)
        }
    }

    private val adapter: QuestionAdapter by lazy {  QuestionAdapter(::onEveryQuestionAsked, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_retro)
        retroRecyclerView.layoutManager = GridLayoutManager(this, 2)
        retroRecyclerView.adapter = adapter
    }

    private fun onEveryQuestionAsked() {

    }
}

class QuestionAdapter(private val endCallback: () -> Unit,
                      private val activity: CurrentRetroActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val list = mutableListOf(
        Question(R.drawable.ic_value, "Délivrer de la valeur") to false,
        Question(R.drawable.ic_trolley, "Facile à délivrer") to false,
        Question(R.drawable.ic_laugh, "Fun") to false,
        Question(R.drawable.ic_doctor, "Santé du code") to false,
        Question(R.drawable.ic_teacher, "Apprentissage") to false,
        Question(R.drawable.ic_mission, "Mission") to false,
        Question(R.drawable.ic_chess, "Pions ou joueurs") to false,
        Question(R.drawable.ic_running, "Rapidité") to false
    )

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.cell_question, p0, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, position: Int) {
        val (question, isChecked) = list[position]
        val idRetro = activity.intent.getStringExtra(EXTRA_ID)
        with(p0.itemView) {
            questionTextView.text = question.title
            questionImageView.setImageResource(question.icon)
            questionImageViewDonut.setImageResource(question.icon)
            if (isChecked) {
                questionViewFlipper.displayedChild = 1
                profileContainerView.setOnClickListener(null)
            } else {
                questionViewFlipper.displayedChild = 0
                profileContainerView.setOnClickListener { view ->
                    launch {
                        db.collection("notation")
                            .whereEqualTo("idRetro", idRetro)
                            .whereEqualTo("question", question.title)
                            .addSnapshotListener { querySnapshot, e ->
                                if (e != null || querySnapshot == null) {
                                    e?.printStackTrace()
                                    return@addSnapshotListener
                                } else {
                                    val list = querySnapshot.documents.filter {
                                        it.data?.get("note") != null //&& it.data?.get("question") == question.title && it.data?.get("idRetro") == idRetro
                                    }.map {
                                        it.data!!.get("note") as Long
                                    }
                                    val love = list.filter { it > 80 }.size.toFloat() / list.size.toFloat()
                                    val happy = list.filter { it in 61..80 }.size.toFloat() / list.size.toFloat()
                                    val mute = list.filter { it in 41..60 }.size.toFloat() / list.size.toFloat()
                                    val sad = list.filter { it in 21..40 }.size.toFloat() / list.size.toFloat()
                                    val vain = list.filter { it <= 20 }.size.toFloat() / list.size.toFloat()
                                    questionDonut.addItemList(listOf(
                                        CategoryDonut.Item(ContextCompat.getColor(this@with.context, android.R.color.holo_green_dark),  love, ContextCompat.getDrawable(this@with.context, R.drawable.ic_love)),
                                        CategoryDonut.Item(ContextCompat.getColor(this@with.context, android.R.color.holo_red_dark), vain, ContextCompat.getDrawable(this@with.context, R.drawable.ic_vain)),
                                        CategoryDonut.Item(ContextCompat.getColor(this@with.context, android.R.color.holo_orange_dark), sad, ContextCompat.getDrawable(this@with.context, R.drawable.ic_sad)),
                                        CategoryDonut.Item(ContextCompat.getColor(this@with.context, android.R.color.holo_purple), mute, ContextCompat.getDrawable(this@with.context, R.drawable.ic_muted)),
                                        CategoryDonut.Item(ContextCompat.getColor(this@with.context, android.R.color.holo_blue_dark), happy, ContextCompat.getDrawable(this@with.context, R.drawable.ic_happy))
                                    ))
                                }
                            }
                    }
                    val animator = ValueAnimator.ofInt(0, 180)
                    animator.duration = 1000
                    animator.addUpdateListener { animation ->
                        val value = animation.animatedValue as Int
                        if (value < 90) {
                            p0.itemView.rotationY = value.toFloat()
                        } else {
                            questionViewFlipper.displayedChild = 1
                            p0.itemView.rotationY = value + 180f
                        }
                    }
                    animator.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                            // Do nothing
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            cellCard.elevation = 8f
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            // Do nothing
                        }

                        override fun onAnimationStart(animation: Animator?) {
                            cellCard.elevation = 0.1f
                        }

                    })
                    animator.start()
                    list[position] = question to true
                    launchQuestion(question.title)
                }
            }
        }
    }

    private fun launchQuestion(title: String) {
        val idRetro = activity.intent.getStringExtra(EXTRA_ID)
        CurrentRetroActivity.usersInRetro?.forEach {
            launch {
                val body = Body(it.token, BodyData("message", "title", title, idRetro))
                try {
                    val client = CurrentRetroActivity.getOkhttpClient(this@QuestionAdapter.activity)
                    val request = Request.Builder()
                        .addHeader("Authorization", "key=AAAAbj2oAkg:APA91bG5kfMrZK0wRUqSFe5qSDuxdkXEv_AECea-GaqAOrxPStBDMaDrCJnrm4aWliPzHJml81vAcwkmH40ooHNswiQ0qhkyF5afAC2MERb-FpIvjjSnPYLFXlOvnJ9wBE0Z5stdD616")
                        .addHeader("Content-Type", "application/json")
                        .url("https://fcm.googleapis.com/fcm/send")
                        .post(
                            RequestBody.create(
                                MediaType.parse("application/json; charset=utf-8"),
                                Gson().toJson(body)
                            )
                        )
                        .build()
                    client?.newCall(request)?.execute()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}

data class Question(
    val icon: Int,
    val title: String
)

data class Body(
    val to: String,
    val data: BodyData
)

data class BodyData(
    val message: String,
    val title: String,
    val question: String,
    val idRetro: String?
)