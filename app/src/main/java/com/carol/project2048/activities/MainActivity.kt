package com.carol.project2048.activities

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import com.carol.project2048.*
import com.carol.project2048.StateHandler.continuingGame
import com.carol.project2048.StateHandler.currentState
import com.carol.project2048.StateHandler.grid
import com.carol.project2048.StateHandler.moveCount
import com.carol.project2048.StateHandler.over
import com.carol.project2048.StateHandler.previousState
import com.carol.project2048.StateHandler.updateState
import com.carol.project2048.StateHandler.updateToMatchState
import com.carol.project2048.StateHandler.won
import com.carol.project2048.TimerHandler.startTimer
import com.carol.project2048.activities.StatisticsActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


val TAG: (Any) -> String = { it.javaClass.simpleName }

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {

    private var tilesToRemove = ArrayList<Tile>()

    private var scale = 1f
    private var margin = 0

    private val handler = Handler()


    private var textBrown = 0
    private var textOffWhite = 0

    private lateinit var prefs: SharedPreferences

    private lateinit var click: MediaPlayer
    private lateinit var tap: MediaPlayer
    private lateinit var whoosh: MediaPlayer
    private lateinit var pop: MediaPlayer

    private var isTimeTrialMode = false

    private val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        GlobalScope.launch {
            Stats.init(this@MainActivity)
        }

        textBrown = ContextCompat.getColor(this, R.color.textBrown)
        textOffWhite = ContextCompat.getColor(this, R.color.offWhiteText)

        scale = resources.displayMetrics.density
        margin = tile_0_0.paddingTop

        click = MediaPlayer.create(this, R.raw.click)
        tap = MediaPlayer.create(this, R.raw.tap)
        whoosh = MediaPlayer.create(this, R.raw.whoosh)
        pop = MediaPlayer.create(this, R.raw.pop)
    }

    override fun onResume() {
        super.onResume()

        clearViews()

        if (StateHandler.loadState(this)) {
            updateToMatchState(true, this::onUpdateState)
        } else {
            newGame()
        }

        startTimer(handler, this::updateTimerTextView)

        logBoard()
        touch_receiver.setOnTouchListener(TileTouchListener(this))
        val tan = ContextCompat.getColor(this, R.color.colorPrimary)
        window.navigationBarColor = tan
        hideSystemUI()

        StateHandler.updateDataValues(this::updateDisplayedData)

        if (prefs[UNDO_ENABLED, true]) {
            undo_button.visibility = View.VISIBLE
        } else {
            undo_button.visibility = View.GONE
        }

        if (prefs[SWIPE_ANYWHERE_ENABLED, false]) {
            val constraints = ConstraintSet()
            constrainToTarget(constraints, touch_receiver.id, ConstraintSet.PARENT_ID)
            constraints.applyTo(full_page)
        } else {
            val constraints = ConstraintSet()
            constrainToTarget(constraints, touch_receiver.id, game_container.id)
            constraints.applyTo(full_page)
        }

        if (over) {
            promptGameOver()
        } else if (won && !continuingGame) {
            promptContinue()
        }
    }

    override fun onPause() {
        super.onPause()

        updateState()
        StateHandler.saveState(this)
        Stats.writeToFile()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.extras?.let {
            isTimeTrialMode = it.getBoolean(TIME_TRIAL, false)
        }
        Log.d(TAG(this), "time trial mode?$isTimeTrialMode")
    }


    private fun updateDisplayedData(score: Int, highScore: Int) {
        score_view.text = formatScore(score)
        best_score.text = formatScore(highScore)
        val movesText = if (moveCount == 1) {
            "1 move"
        } else {
            "$moveCount moves"
        }
        move_count_text_view.text = movesText
    }

    private fun formatScore(s: Int): String {
        return when {
            s >= 1_000_000_000 -> "${(s / 100_000_000).toFloat() / 10}b"
            s >= 1_000_000 -> "${(s / 100_000).toFloat() / 10}m"
            s >= 1_000 -> "${(s / 100).toFloat() / 10}k"
            else -> s.toString()
        }
    }


    private fun promptContinue() {
        showMessage("You win!")
        message_container.setBackgroundColor(ContextCompat.getColor(this, R.color.transparentYellow))
        keep_going_button.visibility = View.VISIBLE
    }

    private fun promptGameOver() {
        showMessage("Game over!")
        message_container.setBackgroundColor(ContextCompat.getColor(this, R.color.transparentBrown))
        keep_going_button.visibility = View.GONE
    }

    private fun showMessage(str: String) {
        touch_receiver.visibility = View.GONE
        message_container.visibility = View.VISIBLE
        message.text = str
    }

    private fun dismissMessage() {
        message_container.visibility = View.GONE
        touch_receiver.visibility = View.VISIBLE
    }

    fun tryAgain(view: View) {
        playClick()
        newGame()
    }

    fun keepGoing(view: View) {
        playClick()
        dismissMessage()
        continuingGame = true
    }

    fun share(view: View) {
        playClick()
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0)

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                takeScreenshot()
            } else {
                Toast.makeText(this,
                    "Sharing requires temporarily storing a screenshot.",
                    Toast.LENGTH_LONG).show()
            }
        } else {
            takeScreenshot()
        }
    }

    @Suppress("DEPRECATION")
    private fun takeScreenshot() {
        val v1 = window.decorView.rootView
        v1.isDrawingCacheEnabled = true
        val mBitmap = Bitmap.createBitmap(v1.drawingCache)
        v1.isDrawingCacheEnabled = false

        val path = MediaStore.Images.Media.insertImage(contentResolver, mBitmap, "Image Description", null)
        val uri = Uri.parse(path)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, "Share Image"))
    }


    private fun addRandom() {
        val available = grid.availablePositions()
        if (available.isEmpty() && isTimeTrialMode) {
            over = true
            won = false
            continuingGame = false
            promptGameOver()
        } else {
            val newPos = available.removeAt((0 until available.size).random())
            addAt(newPos)
        }
    }

    private fun addAt(p: Pos, value: Int = if ((0..9).random() < 9) 2 else 4) {
        grid[p] = Tile(p, value)

        val tile = createTileTextView(value)

        val id = tile.id
        grid[p]?.textView = tile

        val constraintSet = ConstraintSet()
        with(constraintSet) {
            applyDefaultConstraints(this, id)
            constrainToTarget(this, id, p)
            applyTo(game_container as ConstraintLayout?)
        }
    }

    private fun applyDefaultConstraints(constraintSet: ConstraintSet, id: Int) {
        constraintSet.constrainHeight(id, 0)
        constraintSet.constrainWidth(id, 0)
        constraintSet.setDimensionRatio(id, "1:1")
    }

    @SuppressLint("InflateParams")
    private fun createTileTextView(value: Int): TextView {
        val tile = layoutInflater.inflate(R.layout.activity_tile, null) as TextView
        tile.id = View.generateViewId()

        with(tile) {

            text = value.toString()
            if (value < 8) {
                setTextColor(textBrown)
            } else {
                setTextColor(textOffWhite)
            }

            if (value <= 2048) {
                val colorId = ContextCompat.getColor(this@MainActivity, resources.getIdentifier("tile$value", "color", packageName))
                background.mutate().setTint(colorId)
            } else {
                background.mutate().setTint(ContextCompat.getColor(this@MainActivity, R.color.tileSuper))
            }

            textSize = when (value.length()) {
                1, 2 -> {
                    40f
                }
                3 -> {
                    30f
                }
                4 -> {
                    24f
                }
                5 -> {
                    18f
                }
                //if you get bigger than this, congrats, you broke it and you definitely cheated
                else -> {
                    12f
                }
            }

            game_container.addView(this)
        }
        return tile
    }

    private fun getTargetId(p: Pos): Int {
        val gridLocIdName = "tile_${p.y}_${p.x}"
        return resources.getIdentifier(gridLocIdName, "id", packageName)
    }

    private fun constrainToTarget(constraintSet: ConstraintSet, sourceId: Int, pos: Pos) {
        constrainToTarget(constraintSet, sourceId, getTargetId(pos))
    }

    private fun constrainToTarget(constraintSet: ConstraintSet, sourceId: Int, targetId: Int) {
        constraintSet.connect(sourceId, ConstraintSet.LEFT, targetId, ConstraintSet.LEFT, margin)
        constraintSet.connect(sourceId, ConstraintSet.RIGHT, targetId, ConstraintSet.RIGHT, margin)
        constraintSet.connect(sourceId, ConstraintSet.TOP, targetId, ConstraintSet.TOP, margin)
        constraintSet.connect(sourceId, ConstraintSet.BOTTOM, targetId, ConstraintSet.BOTTOM, margin)
    }

    private fun getVector(direction: Int): Pair<Int, Int> {
        return when (direction) {
            0 -> Pair(0, -1)// Up
            1 -> Pair(1, 0) // Right
            2 -> Pair(0, 1) // Down
            3 -> Pair(-1, 0)// Left
            else -> throw IllegalArgumentException()
        }
    }

    private fun prepareTiles() {
        grid.forEach { tile ->
            tile?.let {
                tile.mergedFrom = null
                tile.savePosition()
            }
        }
    }

    private fun moveTile(tile: Tile, pos: Pos) {
        grid[tile.pos] = null
        grid[pos] = tile
        tile.updatePosition(pos)
    }


    internal fun move(direction: Int) {
        if (isGameTerminated())
            return

//        Log.d(TAG(this), "currentState = \n${currentState.grid}")

        previousState = currentState.copy()

        grid = currentState.grid

        val vector = getVector(direction)
        val traversals = buildTraversals(vector)
        var moved = false

        prepareTiles()

        for (i in traversals.first) {
            for (j in traversals.second) {
                val pos = Pos(i, j)
                val tile = grid[pos]

                tile?.let {
                    val positions = getMaxShift(vector, pos)
//                    Log.v(TAG(this), "max pos if merge: ${positions.first} else: ${positions.second}")
                    val next = grid[positions.second]

                    //Merge tiles; only 1 merger per row traversal
                    if (next != null && next.value == tile.value && next.mergedFrom == null) {
                        val merged = Tile(positions.second, tile.value * 2)
                        merged.mergedFrom = Pair(tile, next)


                        grid[merged.pos] = merged
                        grid[tile.pos] = null

                        //Converge the two tiles' positions
                        tile.updatePosition(positions.second)

                        tilesToRemove.add(tile)
                        tilesToRemove.add(next)

                        StateHandler.score += merged.value

                        //Win condition
                        if (merged.value == 2048) {
                            won = true
                        }
                    } else {
                        moveTile(tile, positions.first)
                    }

                    if (pos != tile.pos) {
                        moved = true
                    }
                }
            }
        }

        if (moved) {
            onMove()
        }
    }

    private fun buildTraversals(vector: Pair<Int, Int>): Pair<ArrayList<Int>, ArrayList<Int>> {
        val x = ArrayList<Int>()
        val y = ArrayList<Int>()

        for (i in 0 until grid.size) {
            x.add(i)
            y.add(i)
        }

        if (vector.first == 1) x.reverse()
        if (vector.second == 1) y.reverse()

        return Pair(x, y)
    }

    private fun getMaxShift(vector: Pair<Int, Int>, pos: Pos): Pair<Pos, Pos> {
        var previous: Pos
        var p = pos
        do {
            previous = p
            p = previous + vector
        } while (grid.withinBounds(p) && grid.isPosAvailable(p))

        return Pair(previous, p)
    }

    private fun onMove() {
        val transition = AutoTransition()
        transition.duration = 100
        val constraintSet = ConstraintSet()

        val combinedAny = !tilesToRemove.isEmpty()

        playWhoosh(combinedAny)

        grid.forEach { tile ->
            tile?.let {
                //Update moved tiles
                if (tile.previousPos != tile.pos) {
                    var textView = tile.textView

                    //Add combined tile
                    if (tile.mergedFrom != null) {
                        textView = createTileTextView(tile.value)
                        applyDefaultConstraints(constraintSet, textView.id)
                        val pop = AnimationUtils.loadAnimation(this, R.anim.pop)
                        textView.startAnimation(pop)
                    }
                    textView?.let { constrainToTarget(constraintSet, textView.id, tile.pos) }
                        ?: Log.d(TAG(this), "Found a null TextView @ ${tile.pos}")
                    tile.textView = textView
                }
            }
        }

        tilesToRemove.forEach { tile ->
            constrainToTarget(constraintSet, tile.textView!!.id, tile.pos)
        }

        TransitionManager.beginDelayedTransition(game_container as ViewGroup?, transition)
        constraintSet.applyTo(game_container as ConstraintLayout?)

        tilesToRemove.forEach {
            (game_container as ViewGroup?)?.removeView(it.textView)
        }

        tilesToRemove.clear()

        moveCount++
        StateHandler.updateDataValues(this::updateDisplayedData)

        addRandom()

        if (!movesAvailable()) {
            over = true
        }

        updateState()
        StateHandler.saveState(this)

        if (won && !continuingGame) {
            promptContinue()
        } else if (over) {
            promptGameOver()
        }
    }


    private fun logBoard() {
        Log.v(TAG(this), "grid = \n $grid")
    }


    private fun movesAvailable(): Boolean {
        return grid.arePositionsAvailable() || tileMatchesAvailable()
    }

    private fun tileMatchesAvailable(): Boolean {
        for (i in 0 until grid.size) {
            for (j in 0 until grid.size) {
                val pos = Pos(i, j)
                val tile = grid[pos]
                tile?.let {
                    for (direction in 0 until 4) {
                        val vector = getVector(direction)
                        val otherPos = pos + vector
                        val other = grid[otherPos]

                        if (other != null && other.value == tile.value) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }


    private fun isGameTerminated(): Boolean {
        return over || (won && !continuingGame)
    }

    @Suppress("UNUSED_PARAMETER")
    fun promptNewGame(view: View?) {
        playTap()
        AlertDialog.Builder(this).apply {
            title = "New Game"
            setMessage("Are you sure you want to start a new game?")
            setPositiveButton("Yes") { _, _ ->
                newGame()
            }
            setNegativeButton("No", null)
        }.also {
            it.create()
            it.show()
        }
    }

    private fun newGame() {
        dismissMessage()

        grid.forEach { tile ->
            tile?.let {
                tilesToRemove.add(tile)
            }
        }
        tilesToRemove.forEach {
            game_container.removeView(it.textView)
        }

        tilesToRemove.clear()

        clearViews()

        StateHandler.newGame {
            2.addStartingTiles()

            StateHandler.updateDataValues(this::updateDisplayedData)

            updateState()
            StateHandler.saveState(this)

            startTimer(handler, this::updateTimerTextView)
        }
    }

    private fun Int.addStartingTiles() {
        repeat(this) {
            addRandom()
        }
    }

    private fun clearViews() {
        Log.d(TAG(this), "Clearing views...")
        grid.forEach { tile ->
            //            Log.d(TAG(this), "tile = $tile")
            tile?.let {
                //                Log.d(TAG(this), "textView = ${tile.textView}")
//                Log.d(TAG(this), "index of textView = ${game_container.indexOfChild(tile.textView)}")
                tile.textView?.let { game_container.removeView(it) }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun undo(view: View) {
        playTap()
        previousState?.let {
            //Revert gamesReached count if user just got there
            val maxTile = currentState.grid.maxVal()
            if (maxTile != previousState?.grid?.maxVal()) {
                val currentMaxStat = Stats.getStatForValue(currentState.grid.maxVal())
                currentMaxStat?.let {
                    it.gamesReached--
                }
            }
            grid.forEach { tile ->
                tile?.let {
                    tilesToRemove.add(tile)
                }
            }
            currentState = previousState!!
            previousState = null

            tilesToRemove.forEach {
                game_container.removeView(it.textView)
            }
            tilesToRemove.clear()

            StateHandler.updateToMatchState(listener = this::onUpdateState)
        }
            ?: if (moveCount != 0) Toast.makeText(this, "You can only undo once.", Toast.LENGTH_SHORT).show()

    }

    private fun onUpdateState() {
        clearViews()
        StateHandler.updateDataValues(this::updateDisplayedData)

        grid.forEach { tile ->
            tile?.let {
                addAt(tile.pos, tile.value)
            }
        }
    }

    private fun updateTimerTextView(callCount: Int = 0){
        val strDate = dateFormat.format(System.currentTimeMillis() - StateHandler.startTime + StateHandler.previouslyElapsedTime)
        timer_text_view.text = strDate

        if (isTimeTrialMode && callCount % 2 == 0) {
            addRandom()
        }
    }

    fun openMenu(view: View) {
        playClick()
        startActivity(Intent(this, MenuActivity::class.java))
    }


    fun viewStats(view: View) {
        playClick()
        startActivity(Intent(this, StatisticsActivity::class.java))
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

    private fun playWhoosh(playPopAfter: Boolean = false) {
        if (prefs[SOUND_ENABLED, true]) {
            GlobalScope.launch {
                whoosh.start()
            }.invokeOnCompletion { if (playPopAfter) playPop() }
        }
    }

    private fun playPop() {
        if (prefs[SOUND_ENABLED, true]) {
            GlobalScope.launch {
                pop.start()
            }
        }
    }
}


//returns the number of digits
fun Int.length(): Int {
    var copy = this
    var count = 0
    while (copy != 0) {
        copy /= 10
        count++
    }
    return count
}


fun AppCompatActivity.hideSystemUI() {
    // Enables regular immersive mode.
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
}
