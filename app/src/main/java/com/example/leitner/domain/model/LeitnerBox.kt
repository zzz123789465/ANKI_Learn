package com.example.leitner.domain.model

enum class LeitnerBox(val number: Int, val label: String, val intervalLabel: String) {
    BOX_1(1, "Box 1", "每天"),
    BOX_2(2, "Box 2", "每 3 天"),
    BOX_3(3, "Box 3", "每週"),
    BOX_4(4, "Box 4", "每月");

    fun promote(): LeitnerBox = when (this) {
        BOX_1 -> BOX_2
        BOX_2 -> BOX_3
        BOX_3 -> BOX_4
        BOX_4 -> BOX_4
    }

    companion object {
        fun fromNumber(number: Int): LeitnerBox = entries.firstOrNull { it.number == number }
            ?: error("Invalid Leitner box: $number")
    }
}
