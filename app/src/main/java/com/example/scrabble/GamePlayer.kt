package com.example.scrabble

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout

class GamePlayer(private val handLayout: LinearLayout, private val context: Context, width: Int) : View.OnClickListener {
    private var isComputer = false
    private var hand: MutableSet<GameLetter> = HashSet()
    private var score = 0
    private val cellSize: Int = width / 9
    private val margin: Int = cellSize / 7
    private var clickedImage: ImageView? = null
    private val imageMap: MutableMap<ImageView, GameLetter> = HashMap()
    private val removedImages: MutableSet<ImageView> = HashSet()
    private var changeMode = false
    private val changedImage: MutableSet<ImageView> = HashSet()

    init {
        println("constructor")
        isComputer = false
        changeMode = false
    }

    constructor(context: Context) : this(LinearLayout(context), context, 0) {
        println("constructorEmpty")
        isComputer = true
        changeMode = false
    }

    private fun setClickedImage(view: ImageView) {
        println("setClicked")
        clickedImage = view
    }

    private fun resetClickedImage() {
        println("reset")
        clickedImage = null
    }

    override fun onClick(view: View) {
        println("onClick $changeMode")
        if (!changeMode) {
            clickedImage?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(500)
            if (clickedImage == null || clickedImage != view) {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500)
                setClickedImage(view as ImageView)
            } else {
                resetClickedImage()
            }
        } else {
            if (changedImage.contains(view)) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(500)
                changedImage.remove(view)
            } else {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500)
                changedImage.add(view as ImageView)
            }
        }
    }

    fun getClickedLetter(): GameLetter? {
        println("getClicked")
        return clickedImage?.let { imageMap[it] }
    }

    fun removeClickedImageFromHand() {
        println("removeClickedFromHand")
        clickedImage?.let {
            it.animate().scaleX(1f).scaleY(1f).setDuration(0)
            handLayout.removeView(it)
            hand.remove(imageMap[it])
            removedImages.add(it)
            resetClickedImage()
        }
    }

    fun backLettersFromField() {
        println("backFromField")
        removedImages.forEach { handLayout.addView(it) }
        removedImages.clear()
    }

    fun setChangeMode() {
        changeMode = !changeMode
        println("setMode $changeMode")
    }

    fun isChangeMode(): Boolean {
        println("checkMode")
        return changeMode
    }

    fun removeChangedImages(): Set<GameLetter> {
        println("removeChangedImages")
        val backLetter = HashSet<GameLetter>()
        changedImage.forEach {
            handLayout.removeView(it)
            hand.remove(imageMap[it])
            backLetter.add(imageMap[it]!!)
        }
        changedImage.clear()
        return backLetter
    }

    fun getNumChange(): Int {
        println("getNum")
        return changedImage.size
    }

    fun drawHand() {
        println("drawHand")
        handLayout.removeAllViews()
        hand.forEach { item ->
            val imageLetter = ImageView(context)
            imageMap[imageLetter] = item
            imageLetter.setImageResource(item.imageId)
            val layoutParams = LinearLayout.LayoutParams(cellSize, cellSize)
            layoutParams.setMargins(margin, 2 * margin, margin, 2 * margin)
            imageLetter.layoutParams = layoutParams
            imageLetter.setOnClickListener(this)
            handLayout.addView(imageLetter)
        }
    }

    fun getScore(): Int {
        println("getScore")
        return score
    }

    fun setScore(score: Int) {
        println("setScore")
        this.score = score
    }

    fun getHandSize(): Int {
        println("getHandSize")
        return hand.size
    }

    fun addLetter(lt: GameLetter) {
        println("addLetter")
        hand.add(lt)
    }

    fun addPoints(p: Int) {
        println("addPoints")
        score += p
    }

    fun getHand(): MutableSet<GameLetter> {
        println("getHand")
        return hand
    }

    fun setHand(set: Set<Char>) {
        println("setHand")
        hand.clear()
        set.forEach { hand.add(GameLetter(it)) }
    }
}
