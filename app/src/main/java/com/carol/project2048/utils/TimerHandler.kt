package com.carol.project2048.utils

import android.os.Handler
import java.util.*

object TimerHandler {

    private var timer: Timer? = null
    private lateinit var timerTask: TimerTask
    private var callCount = 0

    fun startTimer(handler: Handler, callback: (Int) -> Unit) {
        if (timer != null) {
            stopTimer()
        }

        callCount = 0

        timer = Timer()

        initializeTimerTask(handler, callback)

        timer?.schedule(timerTask, 0, 1000)
    }

    private fun stopTimer() {
        timer?.let {
            timer!!.cancel()
            timer = null
        }
    }

    private fun initializeTimerTask(handler: Handler, callback: (Int) -> Unit) {
        timerTask = object : TimerTask() {
            /**
             * The action to be performed by this timer task.
             */
            override fun run() {
                callCount++

                handler.post {
                    callback(callCount)
                }
            }
        }
    }
}