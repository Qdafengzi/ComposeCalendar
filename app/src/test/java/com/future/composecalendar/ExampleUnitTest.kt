package com.future.composecalendar

import org.junit.Test
import java.util.Calendar

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (dayOfMonth in lastDayOfMonth downTo 1) {
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            println(
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${
                    calendar.get(
                        Calendar.DAY_OF_MONTH
                    )
                }"
            )
        }

    }


    @Test
    fun test() {
        repeat(5){
            println("$it")
        }
    }


    data class CalendarEntity(
        val year: Int,
        val month: Int,
        val day: Int,
        val week:Int,
        val isCurrentDay:Boolean,
        val isCurrentMonth:Boolean,
    )


    @Test
    fun initData2() {
        val list = mutableListOf<CalendarEntity>()

        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val todayCalendar = Calendar.getInstance()
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

            list.add(CalendarEntity(
                year = year,
                month = month,
                day = dayOfMonth,
                week = weekDay,
                isCurrentDay = (year == todayYear && month == todayMonth && dayOfMonth == todayDay),
                isCurrentMonth = true,
            ))
        }



        if(list.first().week!=1){
            //回到当月的第一天
            calendar.set(year,month,1)

            repeat(list.first().week-1){
                println("=====>补充前面的数据")
                calendar.add(Calendar.DAY_OF_MONTH,-1)

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

                list.add(0,CalendarEntity(
                    year = year1,
                    month = month1,
                    day = day1,
                    week = weekDay,
                    isCurrentDay = false,
                    isCurrentMonth = false,
                ))
            }
        }

        //
        if(list.last().week!=7){
            //回到本月第一天
            calendar[year, month] = 1
            //回到当月最后一天
            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            calendar[year, month] = lastDayOfMonth

            repeat(7-list.last().week){
                calendar.add(Calendar.DAY_OF_MONTH,1)
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

                list.add(CalendarEntity(
                    year = year1,
                    month = month1,
                    day = day1,
                    week = weekDay,
                    isCurrentDay = false,
                    isCurrentMonth = false,
                ))
                println("添加的数据=====>${year1}-${month1+1}-${day1}周:${weekDay}")
            }
        }

        list.forEach {
            println("=====>${it.year}-${it.month+1}-${it.day}周:${it.week}")
        }

        println(list.size)
    }

    fun printEveryDay() {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY // 设置一周的第一天为周一

        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]

        calendar[year, month] = 1 // 设置日期为月份的第一天

        for (day in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar[year, month] = day
            val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
            println("$year 年${month + 1} 月 第" + day + "天 星期" + if ((dayOfWeek - 1) == 0) 7 else (dayOfWeek - 1))
        }
    }
}