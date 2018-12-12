package com.rdo.octo.moodmeter

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home_not_connected.*

class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        loginButton.setOnClickListener {
            startActivity(Intent(this, AddUserActivity::class.java))
        }
    }
}
