package com.future.composecalendar.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.future.composecalendar.data.CalendarData
import com.future.composecalendar.data.MonthData
import com.future.composecalendar.utils.XLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar


@Immutable
data class HomeUIState(
    val calendarList: List<CalendarData> = listOf(),
    val clickDay: Triple<Int, Int, Int> = Triple(0, 0, 0),

    val expand: Boolean = true,//展开 或折叠
)

sealed class HomeAction {
    data class ItemClick(val day: Triple<Int, Int, Int>) : HomeAction()

    data class UpdateCurrentPage(val page: Int) : HomeAction()

    data class SetExpand(val expand: Boolean) : HomeAction()
}


class HomeViewModel : ViewModel() {
    private val _homeUIState: MutableStateFlow<HomeUIState> = MutableStateFlow(HomeUIState())

    val homeUiState = _homeUIState.asStateFlow()

    init {
        initData()
    }


    fun dispatch(action: HomeAction) {
        when (action) {
            is HomeAction.ItemClick -> {
                _homeUIState.update {
                    it.copy(clickDay = action.day)
                }
            }

            is HomeAction.UpdateCurrentPage -> {
                if (action.page >= 0) {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    if (_homeUIState.value.calendarList.size > action.page) {
                        val calendarData = _homeUIState.value.calendarList[action.page]
                        //重置点击的数据
                        //不同的月份 点击事件默认 到当月的1号
                        if (calendarData.month != month) {
                            _homeUIState.update {
                                it.copy(
                                    clickDay = Triple(
                                        calendarData.year,
                                        calendarData.month,
                                        1
                                    )
                                )
                            }
                        } else if (calendarData.year == year) {
                            //如果是当月 则定位到 今天的位置
                            _homeUIState.update {
                                it.copy(
                                    clickDay = Triple(
                                        year,
                                        month,
                                        day
                                    )
                                )
                            }
                        }
                    }
                }
            }

            is HomeAction.SetExpand -> {
                _homeUIState.update {
                    it.copy(expand = action.expand)
                }
            }
        }
    }


    private fun initData() {
        val listOfCalendar = mutableListOf<CalendarData>()

        val calendar = Calendar.getInstance()
        val todayCalendar = Calendar.getInstance()
        val todayYear = todayCalendar.get(Calendar.YEAR)
        val todayMonth = todayCalendar.get(Calendar.MONTH)
        val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

        calendar.firstDayOfWeek = Calendar.MONDAY // 设置一周的第一天为周一

        (0..10).forEach { monthIndex ->
            if (monthIndex > 0) {
                calendar.add(Calendar.MONTH, 1)
            } else {
                calendar.add(Calendar.MONTH, 0)
            }

            val year = calendar[Calendar.YEAR]
            val month = calendar.get(Calendar.MONTH)
            val calendarData = CalendarData(
                year = year,
                month = month
            )

            calendar[year, month] = 1 // 设置日期为月份的第一天

            val list = mutableListOf<MonthData>()
            for (dayOfMonth in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                Log.d("TAG,", "dayOfMonth:::$dayOfMonth")
                calendar[year, month] = dayOfMonth

                val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
                XLogger.d("${month + 1} 月 第" + dayOfMonth + "天是星期" + (dayOfWeek - 1))

                if (year == todayYear && month == todayMonth && dayOfMonth == todayDay) {
                    XLogger.d("今天============>${year}-${month + 1}-${dayOfMonth}")
                }

                list.add(
                    MonthData(
                        year = year,
                        month = month,
                        day = dayOfMonth,
                        week = if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1),
                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                        isCurrentDay = (month == todayMonth && dayOfMonth == todayDay),
                        color = if (month == todayMonth && dayOfMonth == todayDay) {
                            Color.Red
                        } else {
                            Color.Black
                        },
                    )
                )
            }


            val firstData = list.first()

            //第一条数据是周几
            //计算最大的列 跨度 一年的几周就是最大的列
            //补充前面的数据
            val beforeList = mutableListOf<MonthData>()
            calendar.add(Calendar.MONTH, -1)
            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)


            XLogger.d("$lastDayOfMonth====lastDayOfMonth=====>${(lastDayOfMonth - firstData.week + 2)}")

            for (dayOfMonth in lastDayOfMonth downTo (lastDayOfMonth - firstData.week + 2)) {
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                //println("${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)+1}-${calendar.get(Calendar.DAY_OF_MONTH)}")
                val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
                beforeList.add(
                    0,
                    MonthData(
                        year = calendar.get(Calendar.YEAR),
                        month = calendar.get(Calendar.MONTH),
                        day = calendar.get(Calendar.DAY_OF_MONTH),
                        week = if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1),
                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                        isCurrentDay = false,
                        isCurrentMonth = false,
                        color = Color.LightGray
                    )
                )
            }

            //回到本月
            calendar.add(Calendar.MONTH, +2)
            val lastData = list.last()
            val afterList = mutableListOf<MonthData>()
            if ((7 - lastData.week) > 1) {
                (1..(7 - lastData.week)).forEach { day ->
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
                    afterList.add(
                        MonthData(
                            year = calendar.get(Calendar.YEAR),
                            month = calendar.get(Calendar.MONTH),
                            day = calendar.get(Calendar.DAY_OF_MONTH),
                            week = if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1),
                            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                            isCurrentDay = false,
                            isCurrentMonth = false,
                            color = Color.LightGray
                        )
                    )
                }
            }

            XLogger.d("============>beforeList size:${beforeList.size}")
            //回到上一个月
            calendar.add(Calendar.MONTH, -1)
            XLogger.d("============>afterList size:${afterList.size}")
            listOfCalendar.add(
                calendarData.copy(
                    monthList = list,
                    monthListWithTrim = beforeList + list + afterList
                )
            )
            XLogger.d("======>beforeList:${beforeList.size} list:${list.size}  afterList:${afterList.size}")
        }

        _homeUIState.update {
            it.copy(
                calendarList = listOfCalendar,
                clickDay = Triple(todayYear, todayMonth, todayDay)
            )
        }
    }
}