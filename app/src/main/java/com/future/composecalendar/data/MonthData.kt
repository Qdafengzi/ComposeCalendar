package com.future.composecalendar.data

import androidx.compose.ui.graphics.Color

data class MonthData(
    /**
     * 年
     */
    val year: Int = 0,

    /**
     * 月1-12
     */
    val month: Int = 0,

    /**
     * 如果是闰月，则返回闰月
     */
    val leapMonth: Int = 0,

    /**
     * 日1-31
     */
    val day: Int = 0,

    /**
     * 是否是闰年
     */
    val isLeapYear: Boolean = false,

    /**
     * 是否是本月,这里对应的是月视图的本月，而非当前月份，请注意
     */
    val isCurrentMonth: Boolean = false,

    /**
     * 是否是今天
     */
    val isCurrentDay: Boolean = false,
    /**
     * 是否是周末
     */
    val isWeekend: Boolean = false,

    /**
     * 星期,0-6 对应周日到周一
     */
    val week: Int = 0,
    /**
     * 一年的第几周
     */
    val weekOfYear: Int = 0,
    val color: Color = Color.Black,

    )

data class CalendarData(
    /**
     * 年
     */
    val year: Int = 0,

    /**
     * 月1-12
     */
    val month: Int = 0,

    /**
     * 如果是闰月，则返回闰月
     */
    val leapMonth: Int = 0,


    /**
     * 是否是闰年
     */
    val isLeapYear: Boolean = false,

    /**
     * 整月数据
     */
    val monthList: List<MonthData> = listOf(),
    /**
     * 整个月的数据 带有前后 月的数据
     */
    val monthListWithTrim: List<MonthData> = mutableListOf(),

    )
