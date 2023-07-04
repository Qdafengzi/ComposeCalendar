package com.future.composecalendar.data

import androidx.compose.ui.graphics.Color

data class DayEntity(
    val year: Int,
    val month: Int,
    val day: Int,
    val week: Int,
    /**
     * 是否是周末
     */
    val isWeekend: Boolean = false,
    val weekOfYear: Int = 0,
    val isCurrentDay: Boolean,
    val isCurrentMonth: Boolean,
    val color: Color = Color.Black,
)

data class MonthEntity(
    val year: Int,
    val month: Int,
    val monthList: List<DayEntity>,
)
data class WeekEntity(
    val year: Int,
    val month: Int,
    val weekList : List<DayEntity>,
)


