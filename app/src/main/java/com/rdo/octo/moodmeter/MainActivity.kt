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
            textView4.text = when (it) {
                "Délivrer de la valeur" -> "On délivre des choses top, on en est fiers et notre client est super content !"
                "Facile à délivrer" -> "Livrer est simple, safe, sans douleur et en grande partie automatique"
                "Fun" -> "On adore aller bosser et on kiffe bosser ensemble !"
                "Santé du code" -> "On est fier de la qualité de notre code ! Il est propre, facile à lire et bien couvert"
                "Apprentissage" -> "On apprend toujours plein de trucs intéressants !"
                "Mission" -> "On sait exactement pourquoi on est là, et c'est excitant !"
                "Pions ou joueurs" -> "On a le contrôle de notre destinée ! On décide ce qu'on peut et doit faire."
                "Rapidité" -> "On fait les choses rapidement ! Pas de retard ni de délai chez nous."
                "Process adapté" -> "Notre méthode de travail nous va parfaitement."
                "Support" -> "On a toujours une super aide et un support quand on le demande."
                else -> "On est une équipe super soudée avec une super collaboration !"
            }
            textView6.text = when (it) {
                "Délivrer de la valeur" -> "On délivre un caca dont on a honte et que notre client n'aime pas."
                "Facile à délivrer" -> "Livrer est douloureux, long, risqué et se fait à la mano."
                "Fun" -> "On se fait chier"
                "Santé du code" -> "Notre code est un gros caca bourré de dette technique"
                "Apprentissage" -> "On n'a jamais le temps de rien apprendre"
                "Mission" -> "On n'a aucune idée de ce qu'on fait ici, ce n'est ni clair ni inspirant, et on ne voit pas la big picture."
                "Pions ou joueurs" -> "Nous sommes des pions dans un jeu ou on n'a pas d'influence sur ce qu'on va faire."
                "Rapidité" -> "On semble ne rien arriver à finir, on est toujours bloqué ou interrompu et les stories sont coincées par des dépendances"
                "Process adapté" -> "Notre méthode de travail est nulle"
                "Support" -> "On reste souvent bloqué parce qu'on ne reçoit pas l'aide dont on a besoin."
                else -> "On est un groupe d'individus qui ne s'interesse pas et ne sait pas ce que font les autres de l'équipe"
            }
        }
        button.setOnClickListener { _ ->
            val firestore = FirebaseFirestore.getInstance()
            val notation = mapOf(
                "idRetro" to intent.getStringExtra("yoloId"),
                "question" to intent.getStringExtra("yolo"),
                "note" to (seekBar.progress * 10) / 34

            )
            firestore.collection("notation")
                .add(notation)
                .addOnSuccessListener {
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
                    85 -> emoji4.animate().scaleX(2f).scaleY(2f).setDuration(300L).withEndAction {
                        emoji4.animate().scaleY(1f).scaleX(1f).setDuration(300L).withEndAction(null).start()
                    }.start()
                    0 -> emoji5.animate().scaleX(2f).scaleY(2f).setDuration(300L).withEndAction {
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
