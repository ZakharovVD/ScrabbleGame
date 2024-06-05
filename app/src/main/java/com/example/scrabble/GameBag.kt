package com.example.scrabble

import java.util.Random

class GameBag {

    private val bag: MutableMap<GameLetter, Int> = mutableMapOf()
    private var num = 131

    init {
        bag[GameLetter('а')] = 10
        bag[GameLetter('б')] = 3
        bag[GameLetter('в')] = 5
        bag[GameLetter('г')] = 3
        bag[GameLetter('д')] = 5
        bag[GameLetter('е')] = 9
        bag[GameLetter('ж')] = 2
        bag[GameLetter('з')] = 2
        bag[GameLetter('и')] = 8
        bag[GameLetter('й')] = 4
        bag[GameLetter('к')] = 6
        bag[GameLetter('л')] = 4
        bag[GameLetter('м')] = 5
        bag[GameLetter('н')] = 8
        bag[GameLetter('о')] = 10
        bag[GameLetter('п')] = 6
        bag[GameLetter('р')] = 6
        bag[GameLetter('с')] = 6
        bag[GameLetter('т')] = 5
        bag[GameLetter('у')] = 3
        bag[GameLetter('ф')] = 1
        bag[GameLetter('х')] = 2
        bag[GameLetter('ц')] = 1
        bag[GameLetter('ч')] = 2
        bag[GameLetter('ш')] = 1
        bag[GameLetter('щ')] = 1
        bag[GameLetter('ъ')] = 1
        bag[GameLetter('ы')] = 2
        bag[GameLetter('ь')] = 2
        bag[GameLetter('э')] = 1
        bag[GameLetter('ю')] = 1
        bag[GameLetter('я')] = 3
        bag[GameLetter('*')] = 3
    }

    fun getLetter(): GameLetter? {
        if (bag.isEmpty()) {
            return null
        }
        val idx = Random().nextInt(num) + 1
        var i = 0
        for ((key, value) in bag) {
            i += value
            if (i >= idx) {
                if (value > 1) {
                    bag[key] = value - 1
                } else {
                    bag.remove(key)
                }
                num--
                return key
            }
        }
        return GameLetter()
    }

    fun backLetter(backSet: Set<GameLetter>) {
        for (item in backSet) {
            bag[item] = bag.getOrDefault(item, 0) + 1
        }
    }

    fun getBag(): Map<GameLetter, Int> {
        return bag
    }
}
