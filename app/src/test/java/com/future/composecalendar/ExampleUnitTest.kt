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
            println("${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)+1}-${calendar.get(Calendar.DAY_OF_MONTH)}")
        }

    }



    @Test
    fun test(){
        val list = listOf(1,3,1,1,1,1,1,2,2,2,2,2,2,2,3,3,3,3,3,)
        list.chunked(7).forEach {
            println(it)
        }
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