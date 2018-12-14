package com.rdo.octo.moodmeter

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.protobuf.Empty
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home_connected.*
import kotlinx.android.synthetic.main.activity_home_not_connected.*
import kotlinx.android.synthetic.main.cell_retro.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class HomeActivity: AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val retroAdapter: RetroAdapter by lazy { RetroAdapter(::onRetroClick) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        loginButton.setOnClickListener {
            startActivity(Intent(this, AddUserActivity::class.java))
        }
        floatingActionButton.setOnClickListener {
            startActivity(Intent(this, StartRetroActivity::class.java))
        }
        homeRecyclerView.layoutManager = LinearLayoutManager(this)
        homeRecyclerView.adapter = retroAdapter
    }

    private fun onRetroClick(id: String) {
        startActivity(PastRetroActivity.newIntent(this, id))
    }

    override fun onStart() {
        super.onStart()
        val child = if (AddUserActivity.getName(this) != null) {
            startRetroList()
            1
        } else {
            0
        }
        homeViewFlipper.displayedChild = child
    }

    private fun startRetroList() {
        val user = MessagingService.getToken(this)
        db.collection("retro")
            .get()
            .addOnCompleteListener { task ->
                val result = task.result
                if (task.isSuccessful && result != null) {
                    val list = result.filter {
                        try {
                            (it.data["users"] as List<*>).contains(user)
                        } catch (e: Throwable) {
                            false
                        }
                    }.map {
                        val idRetro = it.data["idRetro"] as String
                        val s = idRetro.substringAfter("|")
                        val parse = LocalDate.parse(s, StartRetroActivity.dateFormatter)
                        val date = parse.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                        RetroViewModel(idRetro, date, (it.data["users"] as List<*>).size)
                    }
                    retroAdapter.setList(list)
                    homeRecyclerView.visibility = View.VISIBLE
                    progressBarHome.visibility = View.GONE
                    if (list.isEmpty()) {

                    }
                } else {
                    task.exception?.printStackTrace()
                }
            }
    }
}

data class RetroViewModel(
    val id: String,
    val date: String,
    val nbUsers: Int
)

class RetroAdapter(private val onClick: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list = listOf<RetroViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_retro, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, position: Int) {
        val retro = list[position]
        with(p0.itemView) {
            cellRetroContainer.setOnClickListener {
                this@RetroAdapter.onClick(retro.id)
            }
            dateTextView.text = "Le ${retro.date}"
            nbUserTextView.text = "Avec ${retro.nbUsers} personnes"
        }
    }

    fun setList(list: List<RetroViewModel>) {
        this.list = list
        notifyDataSetChanged()
    }
}



