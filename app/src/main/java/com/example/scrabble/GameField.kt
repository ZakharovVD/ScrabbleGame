package com.example.scrabble

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import androidx.core.util.Pair
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class GameField(
        private val context: Context,
        private val gridField: GridLayout,
        private val onClickListener: View.OnClickListener,
        width: Int
) {

    private val fieldSize = 15
    private val cellSize: Int
    private val field = Array(fieldSize) { IntArray(fieldSize) }
    private val fieldLetter = Array(fieldSize) { Array(fieldSize) { GameLetter() } }
    private val fieldImage = Array(fieldSize) { IntArray(fieldSize) }
    private val buttonCoordinates = HashMap<Button, Pair<Int, Int>>()
    private val dict = HashSet<String>()
    private val curWords = ArrayList<String>()
    private var points = 0
    private var addWord: String? = null
    private val removeWords = HashSet<String>()
    private val myDict = HashSet<String>()

    init {
        try {
            val is: InputStream = context.assets.open(context.getString(R.string.fileName))
            val reader = BufferedReader(InputStreamReader(is))
            var word: String?
            while (reader.readLine().also { word = it } != null) {
                if (word!!.length <= 15) {
                    dict.add(word!!)
                }
            }
            is.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        cellSize = (width * 1.0 / fieldSize + 0.5).toInt()
        for (row in 0 until fieldSize) {
            for (col in 0 until fieldSize) {
                fieldImage[row][col] = R.drawable.cell
                fieldLetter[row][col] = GameLetter()
                field[row][col] = 0
            }
        }
        fieldImage[fieldSize / 2][fieldSize / 2] = R.drawable.start
        setImageId(0, 0, R.drawable.w3)
        setImageId(0, fieldSize / 2, R.drawable.w3)
        setImageId(fieldSize / 2, 0, R.drawable.w3)
        for (i in 1 until 5) {
            setImageId(i, i, R.drawable.w2)
        }
        setImageId(0, 5, R.drawable.l3)
        setImageId(5, 0, R.drawable.l3)
        setImageId(5, 5, R.drawable.l3)
        setImageId(0, 3, R.drawable.l2)
        setImageId(3, 0, R.drawable.l2)
        setImageId(2, fieldSize / 2 - 1, R.drawable.l2)
        setImageId(3, fieldSize / 2, R.drawable.l2)
        setImageId(fieldSize / 2 - 1, 2, R.drawable.l2)
        setImageId(fieldSize / 2, 3, R.drawable.l2)
        setImageId(fieldSize / 2 - 1, fieldSize / 2 - 1, R.drawable.l2)
        drawField()
    }

    private fun setImageId(row: Int, col: Int, id: Int) {
        fieldImage[row][col] = id
        fieldImage[row][fieldSize - col - 1] = id
        fieldImage[fieldSize - row - 1][col] = id
        fieldImage[fieldSize - row - 1][fieldSize - col - 1] = id
    }

    private fun drawField() {
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                val row = GridLayout.spec(i)
                val col = GridLayout.spec(j)
                val layoutParams = GridLayout.LayoutParams(row, col)
                layoutParams.width = cellSize
                layoutParams.height = cellSize
                val buttonCell = Button(context)
                buttonCell.id = i * fieldSize + j
                buttonCell.setBackgroundResource(fieldImage[i][j])
                buttonCell.layoutParams = layoutParams
                buttonCell.setOnClickListener(onClickListener)
                gridField.addView(buttonCell)
                buttonCoordinates[buttonCell] = Pair(i, j)
            }
        }
        val startWord = getStartWord()
        var n = startWord.length / 2
        var col = fieldSize / 2 - n
        val row = fieldSize / 2
        for (i in startWord.indices) {
            val btn = gridField.findViewById<Button>(row * fieldSize + col)
            val letter = GameLetter(startWord[i])
            btn.setBackgroundResource(letter.imageId)
            field[row][col] = 3
            fieldLetter[row][col] = letter
            col++
        }
    }

    fun cellEmpty(btn: Button): Boolean {
        val crd = buttonCoordinates[btn]
        return crd != null && field[crd.first][crd.second] == 0
    }

    fun addLetter(btn: Button, letter: GameLetter) {
        val crd = buttonCoordinates[btn]
        if (crd != null) {
            field[crd.first][crd.second] = 1
            fieldLetter[crd.first][crd.second] = letter
        }
    }

    fun resetLetters() {
        for (item in buttonCoordinates.entries) {
            val (i, j) = item.value
            if (field[i][j] < 3) {
                item.key.setBackgroundResource(fieldImage[i][j])
                field[i][j] = 0
                fieldLetter[i][j] = GameLetter()
            }
        }
    }

    private fun getStartWord(): String {
        val idx = Random().nextInt(dict.size)
        var i = 0
        var word = ""
        for (item in dict) {
            if (i == idx) {
                word = item
                dict.remove(item)
                break
            }
            ++i
        }
        return word
    }

    private fun ltrPoints(i: Int, j: Int): Int {
        var p = fieldLetter[i][j].points
        if (fieldImage[i][j] == R.drawable.l2) {
            p *= 2
        }
        if (fieldImage[i][j] == R.drawable.l3) {
            p *= 3
        }
        return p
    }

    private fun kPoints(i: Int, j: Int): Int {
        var k = 1
        if (fieldImage[i][j] == R.drawable.w2) {
            k *= 2
        }
        if (fieldImage[i][j] == R.drawable.w3) {
            k *= 3
        }
        return k
    }

    fun checkWordsInDict(): String? {
        points = 0
        curWords.clear()
        var wordPoints: Int
        var k: Int
        var w: String
        var newWord: Boolean

        for (i in 0 until fieldSize) {
            w = ""
            newWord = false
            wordPoints = 0
            k = 1
            for (j in 0 until fieldSize) {
                if (field[i][j] == 0) {
                    if (w.length <= 1 || !newWord) {
                        w = ""
                        wordPoints = 0
                        newWord = false
                        k = 1
                        continue
                    }
                    if (dict.contains(w)) {
                        curWords.add(w)
                        points += wordPoints * k
                    } else {
                        addWord = w
                        return w
                    }
                    w = ""
                    wordPoints = 0
                    newWord = false
                    k = 1
                    continue
                }
                w += fieldLetter[i][j].letter
                val p = ltrPoints(i, j)
                wordPoints += p
                k *= kPoints(i, j)
                if (field[i][j] == 2) {
                    newWord = true
                }
            }
            if (w.length > 1 && newWord) {
                if (dict.contains(w)) {
                    curWords.add(w)
                    points += wordPoints * k
                } else {
                    addWord = w
                    return w
                }
            }
        }

        for (j in 0 until fieldSize) {
            w = ""
            newWord = false
            wordPoints = 0
            k = 1
            for (i in 0 until fieldSize) {
                if (field[i][j] == 0) {
                    if (w.length <= 1 || !newWord) {
                        w = ""
                        wordPoints = 0
                        newWord = false
                        k = 1
                        continue
                    }
                    if (dict.contains(w)) {
                        curWords.add(w)
                        points += wordPoints * k
                    } else {
                        addWord = w
                        return w
                    }
                    w = ""
                    wordPoints = 0
                    newWord = false
                    k = 1
                    continue
                }
                w += fieldLetter[i][j].letter
                val p = ltrPoints(i, j)
                wordPoints += p
                k *= kPoints(i, j)
                if (field[i][j] == 2) {
                    newWord = true
                }
            }
            if (w.length > 1 && newWord) {
                if (dict.contains(w)) {
                    curWords.add(w)
                    points += wordPoints * k
                } else {
                    addWord = w
                    return w
                }
            }
        }
        return null
    }

    private fun updateCell(i: Int, j: Int) {
        if (i - 1 >= 0 && field[i - 1][j] == 1) {
            field[i - 1][j] = 2
            updateCell(i - 1, j)
        }
        if (i + 1 < fieldSize && field[i + 1][j] == 1) {
            field[i + 1][j] = 2
            updateCell(i + 1, j)
        }
        if (j - 1 >= 0 && field[i][j - 1] == 1) {
            field[i][j - 1] = 2
            updateCell(i, j - 1)
        }
        if (j + 1 < fieldSize && field[i][j + 1] == 1) {
            field[i][j + 1] = 2
            updateCell(i, j + 1)
        }
    }

    fun checkConnects(): Boolean {
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                if (field[i][j] == 3) {
                    updateCell(i, j)
                }
            }
        }
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                if (field[i][j] == 1) {
                    return false
                }
            }
        }
        return true
    }

    fun getPoints(): Int {
        return points
    }

    fun updateField() {
        for (w in curWords) {
            dict.remove(w)
        }
        curWords.clear()
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                if (field[i][j] == 2) {
                    field[i][j] = 3
                }
            }
        }
    }

    fun addInDict() {
        addWord?.let {
            dict.add(it)
            myDict.add(it)
        }
    }

    fun computerMode(letters: Set<GameLetter>) {
        for (l in letters) {
            Log.d("comp", "" + l.letter)
        }
        points = 0

        findInRow(letters)
        findInCol(letters)

        Log.d("comp", "fin")
    }

    private fun findInRow(letters: Set<GameLetter>): Boolean {
        var find = false
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                if (field[i][j] == 0 && j - 1 >= 0 && field[i][j - 1] > 0) {
                    var j1 = j - 1
                    var begin = "" + fieldLetter[i][j1].letter
                    while (j1 > 0 && field[i][j1 - 1] > 0) {
                        --j1
                        begin = fieldLetter[i][j1].letter + begin
                    }
                    for (w in dict) {
                        if (j1 + w.length > fieldSize || (j1 + w.length != fieldSize && field[i][j1 + w.length] > 0)) {
                            continue
                        }
                        val copySet = letters.map { it.letter }.toMutableSet()
                        if (w.length > j - j1 && w.substring(0, j - j1) == begin) {
                            if (!checkMaskRow(w, copySet, i, j, j1)) {
                                continue
                            }
                        } else {
                            continue
                        }
                        val fit = checkConnectWordsRow(w, i, j1)
                        if (fit) {
                            Log.d("comp", w)
                            setComputerWordRow(letters, copySet, w, i, j1)
                            find = true
                            break
                        }
                    }
                }
                if (!find && field[i][j] == 0 && (j - 1 < 0 || field[i][j - 1] == 0) && j + 1 < fieldSize && field[i][j + 1] > 0) {
                    for (pref in 0..j) {
                        if (j - pref < 0 || field[i][j - pref] != 0) break
                        for (w in dict) {
                            val copySet = letters.map { it.letter }.toMutableSet()
                            val j1 = j - pref
                            if (j1 + w.length > fieldSize || (j1 + w.length != fieldSize && field[i][j1 + w.length] > 0)) {
                                continue
                            }
                            if (w.length > pref + 1) {
                                if (!checkMaskRow(w, copySet, i, j1, j1)) {
                                    continue
                                }
                            } else {
                                continue
                            }
                            val fit = checkConnectWordsRow(w, i, j1)
                            if (fit) {
                                Log.d("comp", w)
                                setComputerWordRow(letters, copySet, w, i, j1)
                                find = true
                                break
                            }
                        }
                        if (find) {
                            break
                        }
                    }
                }
            }
        }
        return find
    }

    private fun checkMaskRow(w: String, copySet: MutableSet<Char>, i: Int, j: Int, j1: Int): Boolean {
        for (s in j - j1 until w.length) {
            val idx = s + j1
            if (field[i][idx] == 0 && !copySet.contains(w[s])) {
                return false
            }
            if (field[i][idx] > 0 && fieldLetter[i][idx].letter != w[s]) {
                return false
            }
            if (field[i][idx] == 0) {
                copySet.remove(w[s])
            }
        }
        return true
    }

    private fun checkConnectWordsRow(w: String, i: Int, j1: Int): Boolean {
        removeWords.clear()
        for (s in w.indices) {
            if (field[i][j1 + s] == 0) {
                var w2 = "" + w[s]
                var i1 = i - 1
                while (i1 >= 0 && field[i1][j1 + s] > 0) {
                    w2 = fieldLetter[i1][j1 + s].letter + w2
                            --i1
                }
                i1 = i + 1
                while (i1 < fieldSize && field[i1][j1 + s] > 0) {
                    w2 += fieldLetter[i1][j1 + s].letter
                            ++i1
                }
                if (w2.length > 1) {
                    if (dict.contains(w2) && !removeWords.contains(w2)) {
                        removeWords.add(w2)
                    } else {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun setComputerWordRow(
            letters: Set<GameLetter>,
            copySet: MutableSet<Char>,
            w: String,
            i: Int,
            j1: Int
    ) {
        var wordPoints = 0
        var k = 1
        for (s in w.indices) {
            if (field[i][j1 + s] == 0) {
                field[i][j1 + s] = 3
                val ltr = GameLetter(w[s])
                copySet.remove(w[s])
                fieldLetter[i][j1 + s] = ltr
                val btn = gridField.findViewById<Button>(i * fieldSize + j1 + s)
                btn.setBackgroundResource(ltr.imageId)
            }
            wordPoints += ltrPoints(i, j1 + s)
            k *= kPoints(i, j1 + s)
        }
        points += wordPoints * k
        letters.clear()
        for (c in copySet) {
            letters.add(GameLetter(c))
        }
        dict.remove(w)
        for (s in removeWords) {
            dict.remove(s)
        }
        removeWords.clear()
    }

    private fun findInCol(letters: Set<GameLetter>): Boolean {
        var find = false
        for (j in 0 until fieldSize) {
            for (i in 0 until fieldSize) {
                if (field[i][j] == 0 && i - 1 >= 0 && field[i - 1][j] > 0) {
                    var i1 = i - 1
                    var begin = "" + fieldLetter[i1][j].letter
                    while (i1 > 0 && field[i1 - 1][j] > 0) {
                        --i1
                        begin = fieldLetter[i1][j].letter + begin
                    }
                    for (w in dict) {
                        if (i1 + w.length > fieldSize || (i1 + w.length != fieldSize && field[i1 + w.length][j] > 0)) {
                            continue
                        }
                        val copySet = letters.map { it.letter }.toMutableSet()
                        if (w.length > i - i1 && w.substring(0, i - i1) == begin) {
                            if (checkMaskCol(w, copySet, i, j, i1)) {
                                continue
                            }
                        } else {
                            continue
                        }
                        val fit = checkConnectWordsCol(w, i1, j)
                        if (fit) {
                            Log.d("comp", w)
                            setComputerWordCol(letters, copySet, w, i1, j)
                            find = true
                            break
                        }
                    }
                }
                if (!find && field[i][j] == 0 && (i - 1 < 0 || field[i - 1][j] == 0) && i + 1 < fieldSize && field[i + 1][j] > 0) {
                    for (pref in 0..i) {
                        if (i - pref < 0 || field[i - pref][j] != 0) break
                        for (w in dict) {
                            val copySet = letters.map { it.letter }.toMutableSet()
                            val i1 = i - pref
                            if (i1 + w.length > fieldSize || (i1 + w.length != fieldSize && field[i1 + w.length][j] > 0)) {
                                continue
                            }
                            if (w.length > pref + 1) {
                                if (checkMaskCol(w, copySet, i1, j, i1)) {
                                    continue
                                }
                            } else {
                                continue
                            }
                            val fit = checkConnectWordsCol(w, i1, j)
                            if (fit) {
                                Log.d("comp", w)
                                setComputerWordCol(letters, copySet, w, i1, j)
                                find = true
                                break
                            }
                        }
                        if (find) {
                            break
                        }
                    }
                }
            }
        }
        return find
    }

    private fun checkMaskCol(w: String, copySet: MutableSet<Char>, i: Int, j: Int, i1: Int): Boolean {
        for (s in i - i1 until w.length) {
            val idx = s + i1
            if (field[idx][j] == 0 && !copySet.contains(w[s])) {
                return true
            }
            if (field[idx][j] > 0 && fieldLetter[idx][j].letter != w[s]) {
                return true
            }
            if (field[idx][j] == 0) {
                copySet.remove(w[s])
            }
        }
        return false
    }

    private fun checkConnectWordsCol(w: String, i1: Int, j: Int): Boolean {
        removeWords.clear()
        for (s in w.indices) {
            if (field[i1 + s][j] == 0) {
                var w2 = "" + w[s]
                var j1 = j - 1
                while (j1 >= 0 && field[i1 + s][j1] > 0) {
                    w2 = fieldLetter[i1 + s][j1].letter + w2
                            --i1
                }
                j1 = j + 1
                while (j1 < fieldSize && field[i1 + s][j1] > 0) {
                    w2 += fieldLetter[i1 + s][j1].letter
                            ++i1
                }
                if (w2.length > 1) {
                    if (dict.contains(w2) && !removeWords.contains(w2)) {
                        removeWords.add(w2)
                    } else {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun setComputerWordCol(
            letters: Set<GameLetter>,
            copySet: MutableSet<Char>,
            w: String,
            i1: Int,
            j: Int
    ) {
        var wordPoints = 0
        var k = 1
        for (s in w.indices) {
            if (field[i1 + s][j] == 0) {
                field[i1 + s][j] = 3
                val ltr = GameLetter(w[s])
                copySet.remove(w[s])
                fieldLetter[i1 + s][j] = ltr
                val btn = gridField.findViewById<Button>((i1 + s) * fieldSize + j)
                btn.setBackgroundResource(ltr.imageId)
            }
            wordPoints += ltrPoints(i1 + s, j)
            k *= kPoints(i1 + s, j)
        }
        points += wordPoints * k
        letters.clear()
        for (c in copySet) {
            letters.add(GameLetter(c))
        }
        dict.remove(w)
        for (s in removeWords) {
            dict.remove(s)
        }
        removeWords.clear()
    }

    fun getFieldSize(): Int {
        return fieldSize
    }

    fun getFieldLetter(i: Int, j: Int): Char {
        return fieldLetter[i][j].letter
    }

    fun setFieldLetter(i: Int, j: Int, c: Char) {
        val btn = gridField.findViewById<Button>(i * fieldSize + j)
        if (c == '-') {
            field[i][j] = 0
            fieldLetter[i][j] = GameLetter()
            btn.setBackgroundResource(fieldImage[i][j])
        } else {
            field[i][j] = 3
            fieldLetter[i][j] = GameLetter(c)
            btn.setBackgroundResource(fieldLetter[i][j].imageId)
        }
    }

    fun getDict(): Set<String> {
        return dict
    }

    fun setDict(s: Set<String>) {
        dict.addAll(s)
    }

    fun clearDict() {
        dict.clear()
    }

    fun getMyDict(): Set<String> {
        return myDict
    }

    fun setMyDict(s: Set<String>) {
        myDict.clear()
        for (item in s) {
            myDict.add(item)
            dict.add(item)
        }
    }
}
