package com.future.composecalendar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.future.composecalendar.data.CalendarData
import com.future.composecalendar.data.MonthData
import com.future.composecalendar.utils.XLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

data class HomeUIState(
    val calendarList: List<CalendarData> = listOf(),
    val weekTitleList: List<String> = listOf(),
)


class HomeViewModel : ViewModel() {
    private val _homeUIState: MutableStateFlow<HomeUIState> = MutableStateFlow(HomeUIState())

    val homeUiState = _homeUIState.asStateFlow()

    init {
        initData()
        initWeekListData()
    }

    private fun initWeekListData() {
        _homeUIState.update {
            it.copy(weekTitleList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
        }
    }


    private fun initData() {
        val listOfCalendar = mutableListOf<CalendarData>()

        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayMonth  = calendar.get(Calendar.MONTH)
        val todayDay  = calendar.get(Calendar.DAY_OF_MONTH)


        calendar.firstDayOfWeek = Calendar.MONDAY // 设置一周的第一天为周一

        (0..100).forEach { monthIndex ->
            if (monthIndex > 0) {
                calendar.add(Calendar.MONTH, 1)
            } else {
                calendar.add(Calendar.MONTH, 0)
            }

            val year = calendar[Calendar.YEAR]
            val month = calendar.get(Calendar.MONTH)
            val calendarData = CalendarData(
                year = year,
                month = month + 1
            )

            calendar[year, month] = 1 // 设置日期为月份的第一天

            val list = mutableListOf<MonthData>()
            for (dayOfMonth in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                Log.d("TAG,", "dayOfMonth:::$dayOfMonth")
                calendar[year, month] = dayOfMonth

                val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
                XLogger.d("${month + 1} 月 第" + dayOfMonth + "天是星期" + (dayOfWeek - 1))

                list.add(
                    MonthData(
                        year = year,
                        month = month + 1,
                        day = dayOfMonth,
                        week = if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1),
                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                        isCurrentDay = year == todayYear && month == todayMonth && dayOfMonth == todayDay

                    )
                )
            }
            listOfCalendar.add(calendarData.copy(list = list))
            _homeUIState.update {
                it.copy(calendarList = listOfCalendar)
            }
        }
    }
}