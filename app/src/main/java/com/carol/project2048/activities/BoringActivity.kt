package com.carol.project2048.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.carol.project2048.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class BoringActivity: AppCompatActivity(){

    private lateinit var click: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tan = ContextCompat.getColor(this, R.color.colorPrimary)
        window.navigationBarColor = tan

        hideSystemUI()

        click = MediaPlayer.create(this, R.raw.click)
    }

    override fun onResume() {
        super.onResume()

        val tan = ContextCompat.getColor(this, R.color.colorPrimary)
        window.navigationBarColor = tan

        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    @Suppress("UNUSED_PARAMETER")
    fun back(view: View) {
        if (PreferenceManager.getDefaultSharedPreferences(this)[SOUND_ENABLED, true]){
            GlobalScope.launch {
                click.start()
            }
        }
        finish()
    }
}