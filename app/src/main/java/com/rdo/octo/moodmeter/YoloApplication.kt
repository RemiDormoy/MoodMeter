package com.rdo.octo.moodmeter

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class YoloApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}