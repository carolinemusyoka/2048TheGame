package com.carol.project2048.activities

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import com.carol.project2048.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BoringActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        version_text.text = "Version ${info.versionName}"
    }
}
