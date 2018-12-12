package com.rdo.octo.moodmeter

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home_connected.*
import kotlinx.android.synthetic.main.activity_home_not_connected.*

class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        loginButton.setOnClickListener {
            startActivity(Intent(this, AddUserActivity::class.java))
        }
        floatingActionButton.setOnClickListener {
            startActivity(Intent(this, StartRetroActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val child = if (AddUserActivity.getName(this) != null) {
            1
        } else {
            0
        }
        homeViewFlipper.displayedChild = child
    }
}
