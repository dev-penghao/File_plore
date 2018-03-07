package com.penghao.activitys

import android.os.Bundle
import android.preference.PreferenceFragment
import com.penghao.file_plroe.R

/**
 * Created by Penghao on 2018-2-18
 */
class MyPreferenceFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference)
    }
}