package com.carol.project2048

import java.io.Serializable

//stores a x,y pair
data class Pos(var x: Int, var y: Int): Serializable {
    operator fun plus(pos: Pos): Pos{
        return Pos(x + pos.x, y + pos.y)
    }

    operator fun plus(vector: Pair<Int, Int>): Pos {
        return Pos(x + vector.first, y + vector.second)
    }

    operator fun compareTo(other: Pos): Int {
        return if (x < other.x || (x == other.x && y < other.y)){
            -1
        } else if (x == other.x && y == other.y){
            0
        } else {
            1
        }
    }

    fun copy(): Pos{
        return copy(x = x, y =y)
    }
}

operator fun <T> Array<Array<T?>>.set(pos: Pos, v: T?) {
    this[pos.x][pos.y] = v
}

operator fun <T> Array<Array<T?>>.get(pos: Pos): T? = this[pos.x][pos.y]
