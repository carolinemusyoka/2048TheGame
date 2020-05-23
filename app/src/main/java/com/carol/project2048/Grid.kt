package com.carol.project2048

import java.io.Serializable
import java.util.*

data class Grid(val size: Int): Iterable<Tile?>, Serializable {


    private var tileGrid = Array(size) { Array<Tile?>(size) { null } }

    constructor(other: Grid): this(other.size){
        tileGrid = Array(size) { i -> Array(size){ j -> other.tileGrid[i][j]?.copy()}}
    }

    fun copy(): Grid{
        return Grid(this)
    }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator(): Iterator<Tile?> {
        return GridIterator()
    }

    inner class GridIterator: Iterator<Tile?>{

        private var pos = Pos(0, 0)

        /**
         * Returns `true` if the iteration has more elements.
         */
        override fun hasNext(): Boolean {
            return withinBounds(pos)
        }

        /**
         * Returns the next element in the iteration.
         */
        override fun next(): Tile? {
            val oldPos = pos
            pos = if (pos.x + 1 < size) Pos(pos.x + 1, pos.y) else Pos(0, pos.y + 1)
            return tileGrid[oldPos]
        }

    }

    operator fun set(p: Pos, t: Tile?) {
        if (withinBounds(p)){
            tileGrid[p] = t
        } else {
            throw IndexOutOfBoundsException()
        }
    }

    operator fun get(p: Pos): Tile? {
        return if (withinBounds(p)) {
            tileGrid[p]
        } else {
            null
        }
    }

    fun availablePositions(): ArrayList<Pos> {
        val availablePositions = ArrayList<Pos>()
        for (i in tileGrid.indices) {
            for (j in tileGrid[i].indices) {
                if (tileGrid[i][j] == null) {
                    availablePositions.add(Pos(i, j))
                }
            }
        }
        return availablePositions
    }

    fun arePositionsAvailable(): Boolean {
        return availablePositions().size != 0
    }

    fun isPosAvailable(pos: Pos): Boolean {
        return !isPosOccupied(pos)
    }

    private fun isPosOccupied(pos: Pos): Boolean {
        return this[pos] != null
    }

    fun withinBounds(pos: Pos): Boolean {
        return pos.x in 0 until size && pos.y in 0 until size
    }

    fun maxVal(): Int{
        return maxBy { it?.value ?: 0 }!!.value
    }

}