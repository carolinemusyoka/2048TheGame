package com.carol.project2048

import android.util.Log
import android.view.MotionEvent
import android.view.View

class TileTouchListener(private val mainActivity: MainActivity) : View.OnTouchListener {
    private var startX = 0f
    private var startY = 0f
    private var swipedAlready = false

    private val minimumRegisteredDistance = 100f

    private val UP = 0
    private val RIGHT = 1
    private val DOWN = 2
    private val LEFT = 3

    /**
     * Called when a touch motionEvent is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v The view the touch motionEvent has been dispatched to.
     * @param motionEvent The MotionEvent object containing full information about
     * the motionEvent.
     * @return True if the listener has consumed the motionEvent, false otherwise.
     */
    override fun onTouch(v: View, motionEvent: MotionEvent): Boolean {
        val action = motionEvent.action

        return when (action) {
            MotionEvent.ACTION_UP -> {
                swipedAlready = false
                true
            }
            MotionEvent.ACTION_DOWN -> {
                startX = motionEvent.x
                startY = motionEvent.y
                true
            }
            MotionEvent.ACTION_MOVE -> {

                val dx = motionEvent.x - startX
                val dy = motionEvent.y - startY

                if (!swipedAlready && (Math.abs(dx) > minimumRegisteredDistance || Math.abs(dy) > minimumRegisteredDistance)) {
                    val movedSomething = if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) {
                            Log.v("Input", "swiped right")
                            mainActivity.move(RIGHT)
                        } else {
                            Log.v("Input", "swiped left")
                            mainActivity.move(LEFT)
                        }
                    } else {
                        if (dy > 0) {
                            Log.v("Input", "swiped down")
                            mainActivity.move(DOWN)
                        } else {
                            Log.v("Input", "swiped up")
                            mainActivity.move(UP)
                        }
                    }
//                    if (movedSomething) {
//                        mainActivity.onMove()
//                    }

                    swipedAlready = true
                }
                true
            }
            else -> v.performClick()
        }
    }

}