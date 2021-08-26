package com.carol.project2048.activities

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carol.project2048.R
import com.carol.project2048.Stats
import kotlinx.android.synthetic.main.activity_statistics.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : BoringActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var tap: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        tap = MediaPlayer.create(this, R.raw.tap)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        best_score_val.text = Stats.bestScore.toString()
        total_score_val.text = Stats.totalScore.toString()
        top_tile_val.text = Stats.topTile.toString()

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = ListAdapter()

    }

    @Suppress("UNUSED_PARAMETER")
    fun info(view: View) {
        playTap()
        startActivity(Intent(this, InfoActivity::class.java))
    }

    private fun playTap() {
        if (prefs[SOUND_ENABLED, true]) {
            GlobalScope.launch {
                tap.start()
            }
        }
    }


    inner class ListAdapter : RecyclerView.Adapter<StatsHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): StatsHolder {
            val v = this@StatisticsActivity.layoutInflater
                .inflate(R.layout.activity_stats, p0, false) as ConstraintLayout
            return StatsHolder(v)
        }

        override fun getItemCount(): Int {
            return Stats.tileStats.size
        }

        override fun onBindViewHolder(p0: StatsHolder, p1: Int) {
            val v = Stats.tileStats[p1]

            p0.header.text = v.value.toString()
            p0.gamesReachedValue.text = v.gamesReached.toString()
            p0.shortestTimeValue.text = formatter.format(v.shortestTime)
            p0.fewestMovesValue.text = v.fewestMoves.toString()
        }

    }

    val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())

    inner class StatsHolder(layout: ConstraintLayout) : RecyclerView.ViewHolder(layout) {
        val header: TextView = layout.findViewById<TextView>(R.id.group_header)
        val gamesReachedValue = layout.findViewById<TextView>(R.id.grv)!!
        val shortestTimeValue: TextView = layout.findViewById(R.id.stv)
        val fewestMovesValue: TextView = layout.findViewById(R.id.fmv)
    }
}

