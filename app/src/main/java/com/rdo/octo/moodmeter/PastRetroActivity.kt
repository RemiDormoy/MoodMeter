package com.rdo.octo.moodmeter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.rdo.octo.moodmeter.utils.CategoryDonut
import kotlinx.android.synthetic.main.activity_past_retro.*
import kotlinx.android.synthetic.main.cell_summary.view.*

class PastRetroActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val adapter = SummaryAdapter()

    companion object {
        private const val EXTRA_ID = "yoloYoloYolo"

        fun newIntent(context: Context, id: String) =
            Intent(context, PastRetroActivity::class.java).putExtra(EXTRA_ID, id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_retro)
        summaryRecyclerView.layoutManager = LinearLayoutManager(this)
        summaryRecyclerView.adapter = adapter
        db.collection("notation")
            .whereEqualTo("idRetro", intent.getStringExtra(EXTRA_ID))
            .get()
            .addOnCompleteListener { task ->
                val result = task.result
                if (task.isSuccessful && result != null) {
                    val map = result.map {
                        Note(it.data["question"] as String, it.data["note"] as Long)
                    }.groupBy { note ->
                        note.title
                    }.mapValues {
                        val average = (it.value.foldRight(0) { a , b -> a.note.toInt() + b}).toFloat() / it.value.size.toFloat()
                        val emoji = when {
                            average > 80 -> R.drawable.ic_love
                            average > 60 -> R.drawable.ic_happy
                            average > 40 -> R.drawable.ic_muted
                            average > 20 -> R.drawable.ic_sad
                            else -> R.drawable.ic_vain
                        }
                        val list = it.value.map { it.note }
                        val love = list.filter { it > 80 }.size.toFloat() / list.size.toFloat()
                        val happy = list.filter { it in 61..80 }.size.toFloat() / list.size.toFloat()
                        val mute = list.filter { it in 41..60 }.size.toFloat() / list.size.toFloat()
                        val sad = list.filter { it in 21..40 }.size.toFloat() / list.size.toFloat()
                        val vain = list.filter { it <= 20 }.size.toFloat() / list.size.toFloat()
                        val items = listOf(
                            CategoryDonut.Item(ContextCompat.getColor(this@PastRetroActivity, android.R.color.holo_green_dark),  love, ContextCompat.getDrawable(this@PastRetroActivity, R.drawable.ic_love)),
                            CategoryDonut.Item(ContextCompat.getColor(this@PastRetroActivity, android.R.color.holo_red_dark), vain, ContextCompat.getDrawable(this@PastRetroActivity, R.drawable.ic_vain)),
                            CategoryDonut.Item(ContextCompat.getColor(this@PastRetroActivity, android.R.color.holo_orange_dark), sad, ContextCompat.getDrawable(this@PastRetroActivity, R.drawable.ic_sad)),
                            CategoryDonut.Item(ContextCompat.getColor(this@PastRetroActivity, android.R.color.holo_purple), mute, ContextCompat.getDrawable(this@PastRetroActivity, R.drawable.ic_muted)),
                            CategoryDonut.Item(ContextCompat.getColor(this@PastRetroActivity, android.R.color.holo_blue_dark), happy, ContextCompat.getDrawable(this@PastRetroActivity, R.drawable.ic_happy))
                        )
                        it.key to QuestionSummary(items, " moyenne : $average", emoji)
                    }
                    val list: List<Pair<String, QuestionSummary>> = map.values.toList()
                    summaryRecyclerView.visibility = VISIBLE
                    yoloProgressBar.visibility = GONE
                    adapter.setList(list)
                } else {
                    task.exception?.printStackTrace()
                }
            }
    }
}

class SummaryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list: List<Pair<String, QuestionSummary>> = listOf()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.cell_summary, p0, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val summary = list[p1]
        with(p0.itemView) {
            title.text = summary.first
            average.text = summary.second.average
            summaryDonut.addItemList(summary.second.items)
            averageImage.setImageResource(summary.second.emoji)
        }
    }

    fun setList(list: List<Pair<String, QuestionSummary>>) {
        this.list = list
        notifyDataSetChanged()
    }

}


data class Note(val title: String, val note: Long)
data class QuestionSummary(val items: List<CategoryDonut.Item>, val average: String, val emoji: Int)
