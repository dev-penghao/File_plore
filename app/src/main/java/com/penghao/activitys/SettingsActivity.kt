package com.penghao.activitys

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class SettingsActivity : AppCompatActivity() {

    val f=MyPreferenceFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.fragmentManager.beginTransaction().replace(android.R.id.content,f).commit()
    }
}
