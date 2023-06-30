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
        printEveryDay()

    }

    fun printEveryDay(){
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY // 设置一周的第一天为周一

        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]

        calendar[year, month] = 1 // 设置日期为月份的第一天

        for (day in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar[year, month] = day
            val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
            println("$year 年${month+1} 月 第" + day + "天 星期" + if((dayOfWeek - 1)==0) 7 else (dayOfWeek - 1))
        }
    }
}