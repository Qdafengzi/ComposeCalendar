package com.future.composecalendar.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.future.composecalendar.data.DayEntity
import com.future.composecalendar.data.MonthEntity
import com.future.composecalendar.data.WeekEntity
import com.future.composecalendar.utils.XLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar


data class HomeUIState(
    val clickDay: Triple<Int, Int, Int> = Triple(0, 0, 0),

    val weekModel: Boolean = false,//周历模式

    val monthEntityList: List<MonthEntity> = listOf(),
    val weekEntityList: List<WeekEntity> = listOf(),

    val needScrollPage: Int = -1,
)

sealed class HomeAction {
    data class ItemClick(val day: Triple<Int, Int, Int>) : HomeAction()

    data class UpdateCurrentPage(val page: Int) : HomeAction()

    data class SetCalendarModel(val isWeekModel: Boolean) : HomeAction()
}


class HomeViewModel : ViewModel() {
    private val _homeUIState: MutableStateFlow<HomeUIState> = MutableStateFlow(HomeUIState())

    val homeUiState = _homeUIState.asStateFlow()

    init {
//        initData()
        generate48MonthData()
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
                    if(!_homeUIState.value.weekModel){
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        if (_homeUIState.value.monthEntityList.size > action.page) {
                            val calendarData = _homeUIState.value.monthEntityList[action.page]
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
                    }else{
                        //周历的模式
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        if (_homeUIState.value.weekEntityList.size > action.page) {
                            val calendarData = _homeUIState.value.weekEntityList[action.page]
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
            }

            is HomeAction.SetCalendarModel -> {
                _homeUIState.update {
                    it.copy(weekModel = action.isWeekModel)
                }

                if (action.isWeekModel) {
                    //如果是周历 要找当前的周
                    val year = _homeUIState.value.clickDay.first
                    val month = _homeUIState.value.clickDay.second
                    val day = _homeUIState.value.clickDay.third

                    if (year == 0 && month == 0 && day == 0) {
                        return
                    }

                    XLogger.d("查找周历：$year-${month+1}-$day")

                    var findWeekIndex = -1
                    run breaking@{
                        _homeUIState.value.weekEntityList.forEachIndexed { index, weekEntity ->
                            if (year != weekEntity.year || month != weekEntity.month) {
                                return@forEachIndexed
                            }
                            weekEntity.weekList.forEach {
                                if (year == it.year && month == it.month && it.day == day) {
                                    XLogger.d("${index} 查找周历=========>${it.year}-${it.month+1}-${it.day}")
                                    findWeekIndex = index
                                    return@breaking
                                }
                            }
                        }
                    }


                    if(findWeekIndex>=0){
                        //找到
                        XLogger.d("周历的那一天 在Index:$findWeekIndex")
                        _homeUIState.update {
                            it.copy(needScrollPage = findWeekIndex)
                        }
                    }
                }else{
                    //如果是月历 要找当前的月
                    val year = _homeUIState.value.clickDay.first
                    val month = _homeUIState.value.clickDay.second
                    val day = _homeUIState.value.clickDay.third

                    if (year == 0 && month == 0 && day == 0) {
                        return
                    }

                    XLogger.d("查找月历：$year-${month+1}-$day")

                    var findMonthIndex = -1
                    run breaking@{
                        _homeUIState.value.monthEntityList.forEachIndexed { index, monthEntity ->
                            if (year != monthEntity.year || month != monthEntity.month) {
                                return@forEachIndexed
                            }
                            monthEntity.monthList.forEach {
                                if (year == it.year && month == it.month && it.day == day) {
                                    XLogger.d("${index} 查找月历=========>${it.year}-${it.month+1}-${it.day}")
                                    findMonthIndex = index
                                    return@breaking
                                }
                            }
                        }
                    }

                    if(findMonthIndex>=0){
                        //找到
                        XLogger.d("周历的那一天 在Index:$findMonthIndex")
                        _homeUIState.update {
                            it.copy(needScrollPage = findMonthIndex)
                        }
                    }
                }
            }
        }
    }


//    private fun initData() {
//        val listOfCalendar = mutableListOf<CalendarData>()
//
//        val calendar = Calendar.getInstance()
//        val todayCalendar = Calendar.getInstance()
//        val todayYear = todayCalendar.get(Calendar.YEAR)
//        val todayMonth = todayCalendar.get(Calendar.MONTH)
//        val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
//
//        calendar.firstDayOfWeek = Calendar.MONDAY // 设置一周的第一天为周一
//
//        (0..10).forEach { monthIndex ->
//            if (monthIndex > 0) {
//                calendar.add(Calendar.MONTH, 1)
//            } else {
//                calendar.add(Calendar.MONTH, 0)
//            }
//
//            val year = calendar[Calendar.YEAR]
//            val month = calendar.get(Calendar.MONTH)
//            val calendarData = CalendarData(
//                year = year,
//                month = month
//            )
//
//            calendar[year, month] = 1 // 设置日期为月份的第一天
//
//            val list = mutableListOf<MonthData>()
//            for (dayOfMonth in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
//                Log.d("TAG,", "dayOfMonth:::$dayOfMonth")
//                calendar[year, month] = dayOfMonth
//
//                val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
//                XLogger.d("${month + 1} 月 第" + dayOfMonth + "天是星期" + (dayOfWeek - 1))
//
//                if (year == todayYear && month == todayMonth && dayOfMonth == todayDay) {
//                    XLogger.d("今天============>${year}-${month + 1}-${dayOfMonth}")
//                }
//
//                list.add(
//                    MonthData(
//                        year = year,
//                        month = month,
//                        day = dayOfMonth,
//                        week = if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1),
//                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
//                        isCurrentDay = (month == todayMonth && dayOfMonth == todayDay),
//                        color = if (month == todayMonth && dayOfMonth == todayDay) {
//                            Color.Red
//                        } else {
//                            Color.Black
//                        },
//                    )
//                )
//            }
//
//
//            val firstData = list.first()
//
//            //第一条数据是周几
//            //计算最大的列 跨度 一年的几周就是最大的列
//            //补充前面的数据
//            val beforeList = mutableListOf<MonthData>()
//            calendar.add(Calendar.MONTH, -1)
//            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//
//
//            XLogger.d("$lastDayOfMonth====lastDayOfMonth=====>${(lastDayOfMonth - firstData.week + 2)}")
//
//            for (dayOfMonth in lastDayOfMonth downTo (lastDayOfMonth - firstData.week + 2)) {
//                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//                //println("${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)+1}-${calendar.get(Calendar.DAY_OF_MONTH)}")
//                val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
//                beforeList.add(
//                    0,
//                    MonthData(
//                        year = calendar.get(Calendar.YEAR),
//                        month = calendar.get(Calendar.MONTH),
//                        day = calendar.get(Calendar.DAY_OF_MONTH),
//                        week = if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1),
//                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
//                        isCurrentDay = false,
//                        isCurrentMonth = false,
//                        color = Color.LightGray
//                    )
//                )
//            }
//
//            //回到本月
//            calendar.add(Calendar.MONTH, +2)
//            val lastData = list.last()
//            val afterList = mutableListOf<MonthData>()
//            if ((7 - lastData.week) > 1) {
//                (1..(7 - lastData.week)).forEach { day ->
//                    calendar.set(Calendar.DAY_OF_MONTH, day)
//                    val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
//                    afterList.add(
//                        MonthData(
//                            year = calendar.get(Calendar.YEAR),
//                            month = calendar.get(Calendar.MONTH),
//                            day = calendar.get(Calendar.DAY_OF_MONTH),
//                            week = if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1),
//                            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
//                            isCurrentDay = false,
//                            isCurrentMonth = false,
//                            color = Color.LightGray
//                        )
//                    )
//                }
//            }
//
//            XLogger.d("============>beforeList size:${beforeList.size}")
//            //回到上一个月
//            calendar.add(Calendar.MONTH, -1)
//            XLogger.d("============>afterList size:${afterList.size}")
//            listOfCalendar.add(
//                calendarData.copy(
//                    monthList = list,
//                    monthListWithTrim = beforeList + list + afterList
//                )
//            )
//            XLogger.d("======>beforeList:${beforeList.size} list:${list.size}  afterList:${afterList.size}")
//        }
//
//        _homeUIState.update {
//            it.copy(
//                calendarList = listOfCalendar,
//                clickDay = Triple(todayYear, todayMonth, todayDay)
//            )
//        }
//    }


    /**
     * 生成48个月的数据
     */
    private fun generate48MonthData(generateDataSize: Int = 20) {
        val start = System.currentTimeMillis()
        val monthList = mutableListOf<MonthEntity>()
        val weekList = mutableListOf<WeekEntity>()

        val calendar = Calendar.getInstance()

        val todayCalendar = Calendar.getInstance()
        val todayYear = todayCalendar.get(Calendar.YEAR)
        val todayMonth = todayCalendar.get(Calendar.MONTH)

        repeat(generateDataSize / 2) {
            calendar.add(Calendar.MONTH, if (it == 0) 0 else 1)
            val generateMonthDataPair = generateMonthData(calendar, todayCalendar)

            //月数据
            monthList.add(generateMonthDataPair.first)
            //周数据
            weekList.addAll(generateMonthDataPair.second)

        }

        //回到本月
        calendar[todayYear, todayMonth] = 1

        repeat(generateDataSize / 2) {
            calendar.add(Calendar.MONTH, -1)

            val generateMonthDataPair = generateMonthData(calendar, todayCalendar)



            //月数据
            monthList.add(0, generateMonthDataPair.first)
            //周数据
            weekList.addAll(0, generateMonthDataPair.second)
        }


        _homeUIState.update {
            it.copy(monthEntityList = monthList, weekEntityList = weekList, needScrollPage = generateDataSize/2)
        }



        val end = System.currentTimeMillis()
        monthList.forEach {
            it.monthList.forEach {
                XLogger.d("monthList::: ${it.year}-${it.month+1}-${it.day}")
            }
        }

        weekList.forEach {
            it.weekList.forEachIndexed { index, dayEntity ->
                XLogger.d("weekList::: ${dayEntity.year}-${dayEntity.month+1}-${dayEntity.day}")
            }
        }

        val end2 = System.currentTimeMillis()
        println("====耗时========>${end2 - start}  ${end - start}")

    }

    /**
     * 根据年月日 生成数据
     */
    private fun generateMonthData(
        calendar: Calendar,
        todayCalendar: Calendar
    ): Pair<MonthEntity, MutableList<WeekEntity>> {
        val list = mutableListOf<DayEntity>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val todayYear = todayCalendar.get(Calendar.YEAR)
        val todayMonth = todayCalendar.get(Calendar.MONTH)
        val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

        //一周第一天是否为星期天
        val isFirstSunday = calendar.firstDayOfWeek == Calendar.SUNDAY

        for (dayOfMonth in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar[year, month] = dayOfMonth
            //获取周几
            var weekDay: Int = calendar.get(Calendar.DAY_OF_WEEK)
            //若一周第一天为星期天，则-1
            if (isFirstSunday) {
                println("周天是第一天")
                weekDay -= 1
                if (weekDay == 0) {
                    weekDay = 7
                }
            }

            list.add(
                DayEntity(
                    year = year,
                    month = month,
                    day = dayOfMonth,
                    week = weekDay,
                    isCurrentDay = (year == todayYear && month == todayMonth && dayOfMonth == todayDay),
                    isCurrentMonth = true,
                    isWeekend = weekDay == 6 || weekDay == 7,
                    weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                    color = if (year == todayYear && month == todayMonth && dayOfMonth == todayDay) Color.Red else Color.Black
                )
            )
        }



        if (list.first().week != 1) {
            //回到当月的第一天
            calendar.set(year, month, 1)
            repeat(list.first().week - 1) {
                println("=====>补充前面的数据")
                calendar.add(Calendar.DAY_OF_MONTH, -1)

                var weekDay: Int = calendar.get(Calendar.DAY_OF_WEEK)
                val year1: Int = calendar.get(Calendar.YEAR)
                val month1: Int = calendar.get(Calendar.MONTH)
                val day1: Int = calendar.get(Calendar.DAY_OF_MONTH)
                //若一周第一天为星期天，则-1
                if (isFirstSunday) {
//                    println("周天是第一天")
                    weekDay -= 1
                    if (weekDay == 0) {
                        weekDay = 7
                    }
                }

                list.add(
                    0, DayEntity(
                        year = year1,
                        month = month1,
                        day = day1,
                        week = weekDay,
                        isCurrentDay = false,
                        isCurrentMonth = false,
                        isWeekend = weekDay == 6 || weekDay == 7,
                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                        color = Color.LightGray
                    )
                )
            }
        }

        //
        if (list.last().week != 7) {
            //回到本月第一天
            calendar[year, month] = 1
            //回到当月最后一天
            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            calendar[year, month] = lastDayOfMonth

            repeat(7 - list.last().week) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                var weekDay: Int = calendar.get(Calendar.DAY_OF_WEEK)
                val year1: Int = calendar.get(Calendar.YEAR)
                val month1: Int = calendar.get(Calendar.MONTH)
                val day1: Int = calendar.get(Calendar.DAY_OF_MONTH)
                //若一周第一天为星期天，则-1
                if (isFirstSunday) {
                    weekDay -= 1
                    if (weekDay == 0) {
                        weekDay = 7
                    }
                }

                list.add(
                    DayEntity(
                        year = year1,
                        month = month1,
                        day = day1,
                        week = weekDay,
                        isCurrentDay = false,
                        isCurrentMonth = false,
                        isWeekend = weekDay == 6 || weekDay == 7,
                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                        color = Color.LightGray
                    )
                )
                println("添加的数据=====>${year1}-${month1 + 1}-${day1}周:${weekDay}")
            }
        }

//        list.forEach {
//            println("=====>${it.year}-${it.month + 1}-${it.day}周:${it.week}")
//        }

        calendar[year, month] = 1

        val week: MutableList<WeekEntity> = mutableListOf()
        list.chunked(7).forEach {
            week.add(
                WeekEntity(
                    year = year,
                    month = month,
                    weekList = it
                )
            )
        }

        return Pair(MonthEntity(year = year, month = month, monthList = list), week)
    }
}