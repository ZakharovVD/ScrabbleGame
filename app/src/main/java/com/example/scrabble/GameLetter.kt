package com.example.scrabble

import java.util.Objects

class GameLetter {
    var imageId: Int
    val letter: Char
    var points: Int

    constructor() {
        letter = '-'
        imageId = R.drawable.cell
        points = 0
    }

    constructor(letter: Char) {
        this.letter = letter
        when (letter) {
            'а' -> {
                imageId = R.drawable.a
                points = 1
            }
            'б' -> {
                imageId = R.drawable.b
                points = 3
            }
            'в' -> {
                imageId = R.drawable.v
                points = 2
            }
            'г' -> {
                imageId = R.drawable.g
                points = 3
            }
            'д' -> {
                imageId = R.drawable.d
                points = 2
            }
            'е', 'ё' -> {
                imageId = R.drawable.e
                points = 1
            }
            'ж' -> {
                imageId = R.drawable.zh
                points = 5
            }
            'з' -> {
                imageId = R.drawable.z
                points = 5
            }
            'и' -> {
                imageId = R.drawable.i
                points = 1
            }
            'й' -> {
                imageId = R.drawable.y
                points = 4
            }
            'к' -> {
                imageId = R.drawable.k
                points = 2
            }
            'л' -> {
                imageId = R.drawable.l
                points = 2
            }
            'м' -> {
                imageId = R.drawable.m
                points = 2
            }
            'н' -> {
                imageId = R.drawable.n
                points = 1
            }
            'о' -> {
                imageId = R.drawable.o
                points = 1
            }
            'п' -> {
                imageId = R.drawable.p
                points = 2
            }
            'р' -> {
                imageId = R.drawable.r
                points = 2
            }
            'с' -> {
                imageId = R.drawable.s
                points = 2
            }
            'т' -> {
                imageId = R.drawable.t
                points = 2
            }
            'у' -> {
                imageId = R.drawable.u
                points = 3
            }
            'ф' -> {
                imageId = R.drawable.f
                points = 10
            }
            'х' -> {
                imageId = R.drawable.kh
                points = 5
            }
            'ц' -> {
                imageId = R.drawable.ts
                points = 5
            }
            'ч' -> {
                imageId = R.drawable.ch
                points = 5
            }
            'ш' -> {
                imageId = R.drawable.sh
                points = 10
            }
            'щ' -> {
                imageId = R.drawable.shch
                points = 10
            }
            'ъ' -> {
                imageId = R.drawable.tz
                points = 10
            }
            'ы' -> {
                imageId = R.drawable.y2
                points = 5
            }
            'ь' -> {
                imageId = R.drawable.mz
                points = 5
            }
            'э' -> {
                imageId = R.drawable.e2
                points = 8
            }
            'ю' -> {
                imageId = R.drawable.yu
                points = 10
            }
            'я' -> {
                imageId = R.drawable.ya
                points = 4
            }
            '*' -> {
                imageId = R.drawable.st
                points = -1
            }
            else -> {
                imageId = R.drawable.cell
                points = 0
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val letter1 = other as GameLetter
        return imageId == letter1.imageId && letter == letter1.letter && points == letter1.points
    }

    override fun hashCode(): Int {
        return Objects.hash(imageId, letter, points)
    }
}