package com.example.scrabble

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class GameActivity : AppCompatActivity(), View.OnClickListener {

    private var mode = 0
    private var isComputer = false
    private var finished = false

    private lateinit var scores: Array<TextView>
    private lateinit var linearLayout: LinearLayout
    private lateinit var field: GameField
    private lateinit var bag: GameBag
    private lateinit var players: Array<GamePlayer>

    private lateinit var dialogs: Array<AlertDialog>
    private lateinit var connectDialog: AlertDialog
    private lateinit var dictDialog: AlertDialog.Builder

    private var turn = 0
    private val handSize = 7

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        supportActionBar?.hide()

        val args = intent.extras
        mode = args?.getInt(getString(R.string.mode)) ?: 0
        isComputer = mode == 2
        finished = false

        scores = arrayOf(findViewById(R.id.score1), findViewById(R.id.score2))
        linearLayout = findViewById(R.id.handLayout)

        val gridLayout: GridLayout = findViewById(R.id.gridField)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        field = GameField(this, gridLayout, this, size.x)
        bag = GameBag()
        players = arrayOf(GamePlayer(linearLayout, this, size.x), GamePlayer(this))

        if (!isComputer) {
            players[1] = GamePlayer(linearLayout, this, size.x)
            dialogs = arrayOf(AlertDialog(this), AlertDialog(this))
            createAlertDialog(0)
            createAlertDialog(1)
        } else {
            val tv: TextView = findViewById(R.id.viewName2)
            tv.setText(R.string.computerName)
        }
        createConnectDialog()

        turn = 0
        if (mode < 3) {
            preferences = getPreferences(MODE_PRIVATE)
            val dict: MutableSet<String> = HashSet()
            val n = preferences.getInt(getString(R.string.myDictSize), 0)
            for (i in 0 until n) {
                dict.add(preferences.getString("myDict$i", "")!!)
            }
            field.setMyDict(dict)
            showHand()
        } else {
            load()
        }
    }

    private fun createAlertDialog(t: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialogTitle)
        val format = resources.getString(R.string.dialogMessage)
        val message = when (t) {
            0 -> String.format(format, resources.getString(R.string.name1))
            1 -> String.format(format, resources.getString(R.string.name2))
            else -> ""
        }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                dialogInterface.dismiss()
            showHand()
        }
        dialogs[t] = builder.create()
    }

    private fun createConnectDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialogTitle)
        builder.setMessage(R.string.connectionMessage)
        builder.setCancelable(true)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
        connectDialog = builder.create()
    }

    private fun showHand() {
        while (players[turn].handSize < handSize) {
            val l = bag.letter ?: break
                    players[turn].addLetter(l)
        }
        players[turn].drawHand()
    }

    private fun createFinishDialog(t: Int) {
        finished = true
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.finish)
        val format = resources.getString(R.string.win)
        val message = when (t) {
            0 -> String.format(format, resources.getString(R.string.name1))
            1 -> String.format(format, resources.getString(R.string.name2))
            2 -> getString(R.string.noWin)
            else -> ""
        }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(R.string.ok) { _, _ -> exit() }
        builder.create().show()
    }

    private fun showDialog() {
        turn = 1 - turn
        linearLayout.removeAllViews()
        dialogs[turn].show()
    }

    private fun showConnectDialog() {
        connectDialog.show()
    }

    private fun showDictDialog(word: String) {
        dictDialog = AlertDialog.Builder(this)
        dictDialog.setTitle(R.string.dialogTitle)
        val format = resources.getString(R.string.dictMessage)
        val message = String.format(format, word)
        dictDialog.setMessage(message)
        dictDialog.setCancelable(true)
        dictDialog.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
        dictDialog.setNeutralButton(R.string.addInDict) { dialogInterface, _ ->
                field.addInDict()
            dialogInterface.dismiss()
            checkWords()
        }
        dictDialog.create().show()
    }

    private fun reset() {
        field.resetLetters()
        players[turn].backLettersFromField()
    }

    private fun checkWords() {
        if (!field.checkConnects()) {
            showConnectDialog()
            return
        }
        val w = field.checkWordsInDict()
        if (w != null) {
            showDictDialog(w)
            return
        }
        field.updateField()
        players[turn].addPoints(field.points)
        scores[turn].text = String.format(Locale.getDefault(), "%d", players[turn].score)

        if (!isComputer) {
            showDialog()
        } else {
            computer()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.reset -> if (!players[turn].isChangeMode) reset()
            R.id.pass -> if (!players[turn].isChangeMode) {
                turn = 1 - turn
                createFinishDialog(turn)
            }
            R.id.ok -> if (!players[turn].isChangeMode) checkWords()
            R.id.change -> {
                if (players[turn].isChangeMode) {
                    val num = players[turn].numChange
                    if (num > 0) {
                        bag.backLetter(players[turn].removeChangedImages())
                        players[turn].setChangeMode()
                        if (!isComputer) showDialog() else computer()
                    } else {
                        players[turn].setChangeMode()
                    }
                } else {
                    reset()
                    players[turn].setChangeMode()
                }
            }
            else -> if (players[turn].clickedLetter != null && field.cellEmpty(view as Button)) {
                val clickedLetter = players[turn].clickedLetter!!
                if (clickedLetter.letter == '*') {
                    changeStar(view)
                } else {
                    addLetter(clickedLetter, view)
                }
            }
        }
    }

    private fun addLetter(letter: GameLetter, view: View) {
        view.setBackgroundResource(letter.imageId)
        field.addLetter(view as Button, letter)
        players[turn].removeClickedImageFromHand()
    }

    private fun changeStar(view: View) {
        val li = LayoutInflater.from(this)
        val dialog = li.inflate(R.layout.change_star, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialog)
        val input = dialog.findViewById<EditText>(R.id.editStar)
                builder.setCancelable(true)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                val s = input.text.toString()
            addLetter(GameLetter(s[0]), view)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun computer() {
        while (players[1].handSize < handSize) {
            players[1].addLetter(bag.letter)
        }
        field.computerMode(players[1].hand)
        players[1].addPoints(field.points)
        scores[1].text = String.format(Locale.getDefault(), "%d", players[1].score)

        showHand()
    }

    private fun exit() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun save() {
        reset()
        preferences = getPreferences(MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("finished", finished)
        val myDict = field.myDict
        editor.putInt(getString(R.string.myDictSize), myDict.size)
        myDict.forEachIndexed { index, item ->
                editor.putString("myDict$index", item)
        }
        if (finished) return
                editor.putInt(getString(R.string.turn), turn)
        editor.putBoolean(getString(R.string.isComputer), isComputer)
        val bagMap = bag.bag
        bagMap.forEach { (key, value) ->
                editor.putInt(key.letter.toString(), value)
        }
        val sz = field.fieldSize
        for (i in 0 until sz) {
            for (j in 0 until sz) {
                val n = i * sz + j
                editor.putString(getString(R.string.fieldLetter) + n, field.getFieldLetter(i, j).toString())
            }
        }
        val dict = field.dict
        editor.putInt(getString(R.string.dictSize), dict.size)
        dict.forEachIndexed { index, item ->
                editor.putString(getString(R.string.dict) + index, item)
        }
        val set1 = players[0].hand
        val set2 = players[1].hand
        editor.putInt("player1Num", players[0].handSize)
        editor.putInt("player2Num", players[1].handSize)
        set1.forEachIndexed { index, item ->
                editor.putString("player1$index", item.letter.toString())
        }
        set2.forEachIndexed { index, item ->
                editor.putString("player2$index", item.letter.toString())
        }
        editor.putInt("score1", players[0].score)
        editor.putInt("score2", players[1].score)
        editor.apply()
    }

    private fun load() {
        preferences = getPreferences(MODE_PRIVATE)
        turn = preferences.getInt(getString(R.string.turn), 0)
        finished = preferences.getBoolean("finished", false)

        if (!finished) {
            field.clearDict()
        }
        val myDictSize = preferences.getInt(getString(R.string.myDictSize), 0)
        val myDict: MutableSet<String> = HashSet()
        for (i in 0 until myDictSize) {
            myDict.add(preferences.getString("myDict$i", "")!!)
        }
        field.setMyDict(myDict)
        if (finished) return

                isComputer = preferences.getBoolean(getString(R.string.isComputer), false)
        if (isComputer) {
            players[1] = GamePlayer(this)
            val tv: TextView = findViewById(R.id.viewName2)
            tv.setText(R.string.computerName)
        }
        val bagMap = bag.bag
        for (i in 0..31) {
            val c = ('a' + i)
            val n = preferences.getInt(c.toString(), 0)
            if (n > 0) {
                bagMap[GameLetter(c)] = n
            } else {
                bagMap.remove(GameLetter(c))
            }
        }
        val n = preferences.getInt("*", 0)
        if (n > 0) {
            bagMap[GameLetter('*')] = n
        } else {
            bagMap.remove(GameLetter('*'))
        }
        val sz = field.fieldSize
        for (i in 0 until sz) {
            for (j in 0 until sz) {
                val m = i * sz + j
                field.setFieldLetter(i, j, preferences.getString(getString(R.string.fieldLetter) + m, "-")!![0])
            }
        }
        val dictSize = preferences.getInt(getString(R.string.dictSize), 0)
        val dict: MutableSet<String> = HashSet()
        for (i in 0 until dictSize) {
            dict.add(preferences.getString(getString(R.string.dict) + i, "")!!)
        }
        field.setDict(dict)
        val set1: MutableSet<Char> = HashSet()
        val set2: MutableSet<Char> = HashSet()
        val handSize1 = preferences.getInt("player1Num", 0)
        val handSize2 = preferences.getInt("player2Num", 0)
        for (i in 0 until handSize1) {
            set1.add(preferences.getString("player1$i", "-")!![0])
        }
        for (i in 0 until handSize2) {
            set2.add(preferences.getString("player2$i", "-")!![0])
        }
        players[0].setHand(set1)
        players[1].setHand(set2)
        players[0].score = preferences.getInt("score1", 0)
        players[1].score = preferences.getInt("score2", 0)
        scores[0].text = String.format(Locale.getDefault(), "%d", players[0].score)
        scores[1].text = String.format(Locale.getDefault(), "%d", players[1].score)
        turn = 1 - turn
        showDialog()
    }

    override fun onPause() {
        save()
        super.onPause()
    }
}
