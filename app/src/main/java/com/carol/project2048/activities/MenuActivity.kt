package com.carol.project2048.activities



import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.carol.project2048.R
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val SOUND_ENABLED = "pref_sound_enabled"
const val UNDO_ENABLED = "pref_undo_enabled"
const val SWIPE_ANYWHERE_ENABLED = "pref_swipe_anywhere_enabled"

const val TIME_TRIAL = "opt_time_trial"

@Suppress("UNUSED_PARAMETER")
class MenuActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var click: MediaPlayer
    private lateinit var tap: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        click = MediaPlayer.create(this, R.raw.click)
        tap = MediaPlayer.create(this, R.raw.tap)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        sounds_button.text = if (prefs[SOUND_ENABLED, true]) "Sounds ON" else "Sounds OFF"
        undo_enable_button.text = if (prefs[UNDO_ENABLED, true]) "Undo ON" else "Undo OFF"
        swipe_anywhere_button.text = if (prefs[SWIPE_ANYWHERE_ENABLED, false]) "Swipe Anywhere ON" else "Swipe Anywhere OFF"

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

    fun onClassicButtonClicked(view: View) {
        playTap()
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun onTimeTrialClicked(view: View) {
        playTap()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(TIME_TRIAL, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        finish()
        startActivity(intent)
    }

    fun onStatisticsClicked(view: View) {
        playTap()
        startActivity(Intent(this, StatisticsActivity::class.java))
    }

    fun onSoundsClicked(view: View) {
        val s = !prefs[SOUND_ENABLED, true]
        prefs[SOUND_ENABLED] = s
        sounds_button.text = if (s) {
            playTap()
            "Sounds ON"
        } else {
            playClick()
            "Sounds OFF"
        }
    }

    fun onUndoClicked(view: View) {
        val u = !prefs[UNDO_ENABLED, true]
        prefs[UNDO_ENABLED] = u
        undo_enable_button.text = if (u) {
            playTap()
            "Undo ON"
        } else {
            playClick()
            "Undo OFF"
        }
    }

    fun onSwipeAnywhereClicked(view: View) {
        val s = !prefs[SWIPE_ANYWHERE_ENABLED, false]
        prefs[SWIPE_ANYWHERE_ENABLED] = s
        swipe_anywhere_button.text = if (s) {
            playTap()
            "Swipe Anywhere ON"
        } else {
            playClick()
            "Swipe Anywhere OFF"
        }
    }

    fun onHowToPlayClicked(view: View) {
        playTap()
        startActivity(Intent(this, HowToPlayActivity::class.java))
    }

    fun onAboutClicked(view: View) {
        playTap()
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun playClick() {
        if (prefs[SOUND_ENABLED, true]) {
            GlobalScope.launch {
                click.start()
            }
        }
    }

    private fun playTap() {
        if (prefs[SOUND_ENABLED, true]) {
            GlobalScope.launch {
                tap.start()
            }
        }
    }
}

operator fun SharedPreferences.get(p0: String, p1: Boolean): Boolean {
    return getBoolean(p0, p1)
}

operator fun SharedPreferences.set(p0: String, p1: Boolean) {
    edit().putBoolean(p0, p1).apply()
}