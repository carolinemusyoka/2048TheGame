package com.carol.project2048.utils


import java.io.Serializable

data class State(val grid: Grid, val moveCount: Int = 0, val score: Int = 0, val bestScore: Int = 0,
                 val gameOver: Boolean = false, val won: Boolean = false,
                 val continuingGame: Boolean = false, val time: Long = 0) : Serializable {

    constructor(state: State, time: Long) : this(state.grid.copy(), state.moveCount, state.score, state.bestScore,
        state.gameOver, state.won, state.continuingGame, time)

    fun copy(): State {
        return State(grid.copy(), moveCount, score, bestScore, gameOver, won, continuingGame, time)
    }
}