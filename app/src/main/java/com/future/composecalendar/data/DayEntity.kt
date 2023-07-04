package com.future.composecalendar.data

import androidx.compose.ui.graphics.Color

data class DayEntity(
    val year: Int = 2023,
    val month: Int =0,
    val day: Int = 1,
    val week: Int= 1,
    /**
     * 是否是周末
     */
    val isWeekend: Boolean = false,
    val weekOfYear: Int = 0,
    val isCurrentDay: Boolean = false,
    val isCurrentMonth: Boolean= false,
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


