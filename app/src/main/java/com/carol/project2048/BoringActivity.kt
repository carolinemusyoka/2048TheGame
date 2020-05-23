package com.carol.project2048

import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.carol.project2048.hideSystemUI
import com.carol.project2048.SOUND_ENABLED
import com.carol.project2048.get
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