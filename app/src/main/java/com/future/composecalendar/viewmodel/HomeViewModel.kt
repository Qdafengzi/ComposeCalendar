package com.future.composecalendar.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.future.composecalendar.data.DayEntity
import com.future.composecalendar.data.MonthEntity
import com.future.composecalendar.data.WeekEntity
import com.future.composecalendar.utils.XLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar


data class HomeUIState(
    //点击的位置 当前日期
    val clickDay: DayEntity = DayEntity(year = 2023),

    val weekModel: Boolean = false,//周历模式

    val monthEntityList: List<MonthEntity> = listOf(),
    val weekEntityList: List<WeekEntity> = listOf(),

    val needScrollPage: Int = -1,


    //展示的月份的月数据
    val monthEntity: MonthEntity = MonthEntity(),
    //点击事件 周的数据
    val weekEntity: WeekEntity = WeekEntity(),
)

sealed class HomeAction {
    data class ItemClick(val day: DayEntity) : HomeAction()

    data class UpdateCurrentPage(val page: Int) : HomeAction()

    data class SetCalendarModel(val isWeekModel: Boolean) : HomeAction()

    data class GetMonthData(val isNextMonth: Boolean) : HomeAction()
}


class HomeViewModel : ViewModel() {
    private val _homeUIState: MutableStateFlow<HomeUIState> = MutableStateFlow(HomeUIState())

    val homeUiState = _homeUIState.asStateFlow()

    val mMonthEntity = mutableStateOf(MonthEntity())

    val mCurrentPage = mutableStateOf(1)
    private val viewModelJob = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        // 异常处理代码
        XLogger.d("Caught exception: $exception")
    }

    private val ioScope = CoroutineScope(exceptionHandler + Dispatchers.IO + viewModelJob)



    init {
//        initData()
        getMonthData(5000)
//        generate48MonthData()
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
                    if (!_homeUIState.value.weekModel) {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        if (_homeUIState.value.monthEntityList.size > action.page) {
                            val calendarData = _homeUIState.value.monthEntityList[action.page]
                            //重置点击的数据
                            //不同的月份 点击事件默认 到当月的1号
                            if (calendarData.month != month) {
                                calendar[calendarData.year, calendarData.month] = 1
                                val week = getWeek(calendar)

                                _homeUIState.update {
                                    it.copy(
                                        clickDay = DayEntity(
                                            year = calendar.get(Calendar.YEAR),
                                            month = calendar.get(Calendar.MONTH),
                                            day = 1,
                                            week = week,
                                            isCurrentDay = false,
                                            isCurrentMonth = false,
                                            isWeekend = week == 6 || week == 7,
                                            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                                            color = Color.LightGray
                                        )
                                    )
                                }
                            } else if (calendarData.year == year) {
                                //如果是当月 则定位到 今天的位置
                                val week = getWeek(calendar)
                                _homeUIState.update {
                                    it.copy(
                                        clickDay = DayEntity(
                                            year = year,
                                            month = month,
                                            day = day,
                                            week = week,
                                            isCurrentDay = true,
                                            isCurrentMonth = true,
                                            isWeekend = week == 6 || week == 7,
                                            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                                            color = Color.LightGray
                                        )
                                    )
                                }
                            }
                        }
                    } else {
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
                                calendar[calendarData.year, calendarData.month] = 1
                                val week = getWeek(calendar)

                                _homeUIState.update {
                                    it.copy(
                                        clickDay = DayEntity(
                                            year = calendar.get(Calendar.YEAR),
                                            month = calendar.get(Calendar.MONTH),
                                            day = 1,
                                            week = week,
                                            isCurrentDay = false,
                                            isCurrentMonth = false,
                                            isWeekend = week == 6 || week == 7,
                                            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                                            color = Color.LightGray
                                        )
                                    )
                                }

                            } else if (calendarData.year == year) {
                                //如果是当月 则定位到 今天的位置
                                val week = getWeek(calendar)
                                _homeUIState.update {
                                    it.copy(
                                        clickDay = DayEntity(
                                            year = year,
                                            month = month,
                                            day = day,
                                            week = week,
                                            isCurrentDay = true,
                                            isCurrentMonth = true,
                                            isWeekend = week == 6 || week == 7,
                                            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                                            color = Color.LightGray
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
                    val year = _homeUIState.value.clickDay.year
                    val month = _homeUIState.value.clickDay.month
                    val day = _homeUIState.value.clickDay.day

                    if (year == 0 && month == 0 && day == 0) {
                        return
                    }

                    XLogger.d("查找周历：$year-${month + 1}-$day")

                    var findWeekIndex = -1
                    run breaking@{
                        _homeUIState.value.weekEntityList.forEachIndexed { index, weekEntity ->
                            if (year != weekEntity.year || month != weekEntity.month) {
                                return@forEachIndexed
                            }
                            weekEntity.weekList.forEach {
                                if (year == it.year && month == it.month && it.day == day) {
                                    XLogger.d("${index} 查找周历=========>${it.year}-${it.month + 1}-${it.day}")
                                    findWeekIndex = index
                                    return@breaking
                                }
                            }
                        }
                    }


                    if (findWeekIndex >= 0) {
                        //找到
                        XLogger.d("周历的那一天 在Index:$findWeekIndex")
                        _homeUIState.update {
                            it.copy(needScrollPage = findWeekIndex)
                        }
                    }
                } else {
                    //如果是月历 要找当前的月
                    val year = _homeUIState.value.clickDay.year
                    val month = _homeUIState.value.clickDay.month
                    val day = _homeUIState.value.clickDay.day

                    if (year == 0 && month == 0 && day == 0) {
                        return
                    }

                    XLogger.d("查找月历：$year-${month + 1}-$day")

                    var findMonthIndex = -1
                    run breaking@{
                        _homeUIState.value.monthEntityList.forEachIndexed { index, monthEntity ->
                            if (year != monthEntity.year || month != monthEntity.month) {
                                return@forEachIndexed
                            }
                            monthEntity.monthList.forEach {
                                if (year == it.year && month == it.month && it.day == day) {
                                    XLogger.d("${index} 查找月历=========>${it.year}-${it.month + 1}-${it.day}")
                                    findMonthIndex = index
                                    return@breaking
                                }
                            }
                        }
                    }

                    if (findMonthIndex >= 0) {
                        //找到
                        XLogger.d("周历的那一天 在Index:$findMonthIndex")
                        _homeUIState.update {
                            it.copy(needScrollPage = findMonthIndex)
                        }
                    }
                }
            }

            is HomeAction.GetMonthData -> {
                if(action.isNextMonth) getNextMonthData() else getPreMonthData()
            }
        }
    }


    /**
     * 生成48个月的数据
     */
    private fun generate48MonthData(generateDataSize: Int = 48) {
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
            it.copy(
                monthEntityList = monthList,
                weekEntityList = weekList,
                needScrollPage = generateDataSize / 2
            )
        }


        val end = System.currentTimeMillis()
        monthList.forEach {
            it.monthList.forEach {
                XLogger.d("monthList::: ${it.year}-${it.month + 1}-${it.day}")
            }
        }

        weekList.forEach {
            it.weekList.forEachIndexed { index, dayEntity ->
                XLogger.d("weekList::: ${dayEntity.year}-${dayEntity.month + 1}-${dayEntity.day}")
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

    private fun getWeek(calendar: Calendar): Int {
        val isFirstSunday = calendar.firstDayOfWeek == Calendar.SUNDAY
        var weekDay: Int = calendar.get(Calendar.DAY_OF_WEEK)
        //若一周第一天为星期天，则-1
        if (isFirstSunday) {
            weekDay -= 1
            if (weekDay == 0) {
                weekDay = 7
            }
        }

        return weekDay
    }

    /**
     * 获取某月的天数
     *
     * @param year  年
     * @param month 月
     * @return 某月的天数
     */
    fun getMonthDaysCount(year: Int, month: Int): Int {
        var count = 0
        //判断大月份
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            count = 31
        }

        //判断小月
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            count = 30
        }

        //判断平年与闰年
        if (month == 2) {
            count = if (isLeapYear(year)) {
                29
            } else {
                28
            }
        }
        return count
    }

    /**
     * 是否是闰年
     *
     * @param year year
     * @return 是否是闰年
     */
    fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0
    }


    /**
     * 获取下一个月份的数据
     */
     fun getNextMonthData() {

    }

    /**
     * 获取上一个月份的数据
     */
     fun getPreMonthData(){
    }

    /**
     * 获取月份数据
     * @param offset 月份偏移量
     */
     fun getMonthData(
        currentPage: Int = 0,
    ) {
         val offset = currentPage-5000

        ioScope.launch {
            println("=====>生成的数据")
            val todayCalendar = Calendar.getInstance()
            val todayYear = todayCalendar.get(Calendar.YEAR)
            val todayMonth = todayCalendar.get(Calendar.MONTH)
            val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

            val monthList: MutableList<DayEntity> = mutableListOf()

            val calendar = Calendar.getInstance()
            calendar.set(todayYear,todayMonth, 1)

            calendar.add(Calendar.MONTH, offset)

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            calendar[year, month] = 1

            val nextMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (day in 1..nextMaxDay) {
                calendar[year, month] = day
                val week = getWeek(calendar)
                monthList.add(
                    DayEntity(
                        year = year,
                        month = month,
                        day = day,
                        week = week,
                        isCurrentDay = todayYear == year && todayMonth == month && todayDay == day,
                        isCurrentMonth = todayYear == year && todayMonth == month,
                        isWeekend = week == 6 || week == 7,
                        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                        color = Color.LightGray
                    )
                )
            }


            if (monthList.first().week != 1) {
                //回到当月的第一天
                calendar.set(year, month, 1)
                repeat(monthList.first().week - 1) {

                    calendar.add(Calendar.DAY_OF_MONTH, -1)

                    val weekDay: Int = getWeek(calendar)
                    val year1: Int = calendar.get(Calendar.YEAR)
                    val month1: Int = calendar.get(Calendar.MONTH)
                    val day1: Int = calendar.get(Calendar.DAY_OF_MONTH)
                    monthList.add(
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
            if (monthList.last().week != 7) {
                //回到本月第一天
                calendar[year, month] = 1
                //回到当月最后一天
                val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                calendar[year, month] = lastDayOfMonth

                repeat(7 - monthList.last().week) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val weekDay: Int = getWeek(calendar)
                    val year1: Int = calendar.get(Calendar.YEAR)
                    val month1: Int = calendar.get(Calendar.MONTH)
                    val day1: Int = calendar.get(Calendar.DAY_OF_MONTH)
                    monthList.add(
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
//                    println("添加的数据=====>${year1}-${month1 + 1}-${day1}周:${weekDay}")
                }
            }

            viewModelScope.launch (Dispatchers.Main){
                val monthEntity = MonthEntity(year = year, month = month, monthList = monthList)
                _homeUIState.update {
                    it.copy(monthEntity = monthEntity)
                }
                mMonthEntity.value = monthEntity
                mCurrentPage.value = currentPage
            }
        }
    }
}