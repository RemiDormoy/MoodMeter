package com.rdo.octo.moodmeter

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.readystatesoftware.chuck.ChuckInterceptor
import kotlinx.android.synthetic.main.activity_current_retro.*
import kotlinx.android.synthetic.main.cell_question.view.*
import kotlinx.coroutines.experimental.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

class CurrentRetroActivity : AppCompatActivity() {

    companion object {
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
    }

    private val adapter = QuestionAdapter(::onEveryQuestionAsked, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_retro)
        retroRecyclerView.layoutManager = GridLayoutManager(this, 2)
        retroRecyclerView.adapter = adapter
    }

    private fun onEveryQuestionAsked() {

    }
}

class QuestionAdapter(private val endCallback: () -> Unit, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        with(p0.itemView) {
            questionTextView.text = question.title
            questionImageView.setImageResource(question.icon)
            if (isChecked) {
                alphaView.alpha = 0.4f
                checkView.alpha = 1f
                profileContainerView.setOnClickListener(null)
            } else {
                alphaView.alpha = 0f
                checkView.alpha = 0f
                profileContainerView.setOnClickListener {
                    alphaView.visibility = View.VISIBLE
                    alphaView.animate().alpha(0.4f).setDuration(1000L).start()
                    checkView.visibility = View.VISIBLE
                    checkView.animate().alpha(1f).setDuration(1000L).start()
                    list[position] = question to true
                    launchQuestion(question.title)
                }
            }
        }
    }

    private fun launchQuestion(title: String) {
        CurrentRetroActivity.usersInRetro?.forEach {
            launch {
                val body = Body(it.token, BodyData("message", "title", title))
                try {
                    val client = CurrentRetroActivity.getOkhttpClient(this@QuestionAdapter.context)
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
    val question: String
)