package com.rdo.octo.moodmeter

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import android.util.TypedValue
import com.google.firebase.firestore.FirebaseFirestore
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch


class MainActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, title: String?, idRetro: String?): Intent {
            return Intent(context, MainActivity::class.java)
                .putExtra("yolo", title)
                .putExtra("yoloId", idRetro)
        }
    }

    private val evaluator: ArgbEvaluator by lazy {
        ArgbEvaluator()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intent.getStringExtra("yolo")?.let {
            textView.text = it
        }
        button.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            val notation = mapOf(
                "idRetro" to intent.getStringExtra("yoloId"),
                "question" to intent.getStringExtra("yolo"),
                "note" to (seekBar.progress * 10) / 34

            )
            firestore.collection("notation")
                .add(notation)
                .addOnSuccessListener { documentReference ->
                    launch(UI) { finish() }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (progress) {
                    340 -> emoji1.animate().scaleX(2f).scaleY(2f).setDuration(300L).withEndAction {
                        emoji1.animate().scaleY(1f).scaleX(1f).setDuration(300L).withEndAction(null).start()
                    }.start()
                    255 -> emoji2.animate().scaleX(2f).scaleY(2f).setDuration(300L).withEndAction {
                        emoji2.animate().scaleY(1f).scaleX(1f).setDuration(300L).withEndAction(null).start()
                    }.start()
                    170 -> emoji3.animate().scaleX(2f).scaleY(2f).setDuration(300L).withEndAction {
                        emoji3.animate().scaleY(1f).scaleX(1f).setDuration(300L).withEndAction(null).start()
                    }.start()
                    85 ->  emoji4.animate().scaleX(2f).scaleY(2f).setDuration(300L).withEndAction {
                        emoji4.animate().scaleY(1f).scaleX(1f).setDuration(300L).withEndAction(null).start()
                    }.start()
                    0 ->  emoji5.animate().scaleX(2f).scaleY(2f).setDuration(300L).withEndAction {
                        emoji5.animate().scaleY(1f).scaleX(1f).setDuration(300L).withEndAction(null).start()
                    }.start()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

        })
        emoji1.setOnClickListener { button ->
            seekBar.progress = 340
        }
        emoji2.setOnClickListener { button ->
            seekBar.progress = 255
        }
        emoji3.setOnClickListener { button ->
            seekBar.progress = 170
        }
        emoji4.setOnClickListener { button ->
            seekBar.progress = 85
        }
        emoji5.setOnClickListener { button ->
            seekBar.progress = 0
        }
    }

    private fun Int.toDp(): Float {
        val r = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            r.displayMetrics
        )
    }
}
