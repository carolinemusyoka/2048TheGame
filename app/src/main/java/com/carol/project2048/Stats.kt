package com.carol.project2048

import android.content.Context
import android.util.Log
import java.io.*

object Stats : Serializable {

    var bestScore = 0
    var totalScore = 0
    var topTile = 0

    var tileStats = ArrayList<TileStats>()

    private var file: File? = null

    fun init(context: Context){
        file = File(context.filesDir, "stats.dat")
        readFromFile()
    }

    private fun readFromFile() {
        if (file?.exists() == true) {// gross, but correct, apparently (null check)
            val ois = ObjectInputStream(FileInputStream(file))

            bestScore = ois.readInt()
            totalScore = ois.readInt()
            topTile = ois.readInt()

            @Suppress("UNCHECKED_CAST")
            tileStats = ois.readObject() as ArrayList<TileStats>
            ois.close()

        }
    }

    fun writeToFile() {

        val oos = ObjectOutputStream(FileOutputStream(file))

        oos.writeInt(bestScore)
        oos.writeInt(totalScore)
        oos.writeInt(topTile)
        oos.writeObject(tileStats)
        oos.flush()
        oos.close()
    }

    data class TileStats(val value: Int, var gamesReached: Int = 0, var shortestTime: Long = 0, var fewestMoves: Int = 0) : Serializable

    fun getStatForValue(x: Int): TileStats? {
        return tileStats.firstOrNull { stats -> stats.value == x }
    }
}
