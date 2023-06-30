package com.future.composecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.composecalendar.ui.theme.ComposeCalendarTheme
import com.future.composecalendar.utils.XLogger
import com.future.composecalendar.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCalendarTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Calendar()
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
fun Calendar(homeViewModel: HomeViewModel = viewModel()) {
    val homeUiState = homeViewModel.homeUiState.collectAsState().value
    val weekTitleList = homeUiState.weekTitleList
    val pagerState = rememberPagerState()

    val textMeasurerAndTextSize = getTextMeasurerAndTextSize()

    Column(modifier = Modifier.fillMaxSize()) {
        XLogger.d("==================>Calendar")
        YearAndMonth(homeViewModel, pagerState)
        //星期
        WeekRow(weekTitleList)
        //日历信息
        CalendarPager(homeViewModel, pagerState, textMeasurerAndTextSize)
    }
}

@Composable
fun WeekRow(weekTitleList: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        weekTitleList.forEachIndexed { _, s ->
            Text(
                text = s,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YearAndMonth(homeViewModel: HomeViewModel, pagerState: PagerState) {
    val homeUiState = homeViewModel.homeUiState.collectAsState().value
    val calendarList = homeUiState.calendarList
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${calendarList[pagerState.currentPage].year}年",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = "${calendarList[pagerState.currentPage].month}月",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun getTextMeasurerAndTextSize(): Pair<TextMeasurer, IntSize> {
    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult: TextLayoutResult =
        textMeasurer.measure(
            text = "9",
            style = TextStyle(
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        )
    val textSize = textLayoutResult.size

    return Pair(textMeasurer, textSize)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
fun CalendarPager(
    homeViewModel: HomeViewModel,
    pagerState: PagerState,
    textMeasurerAndTextSize: Pair<TextMeasurer, IntSize>
) {
    val homeUiState = homeViewModel.homeUiState.collectAsState().value
    val calendarList = homeUiState.calendarList

    //size 最大的列 找出最长的那个决定高度
    var maxColumn by remember {
        mutableStateOf(1)
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    val paddingPx = 2

    val textMeasurer = textMeasurerAndTextSize.first
    val textSize = textMeasurerAndTextSize.second


    LaunchedEffect(key1 = pagerState.currentPage, block = {
        XLogger.d("=======>${pagerState.currentPage}")
    })
    XLogger.d("==================>CalendarPager")

    HorizontalPager(
        pageCount = calendarList.size,
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),

        ) {
        val calendarData = calendarList[it]

        //绘制整个月的数据
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height((maxColumn * screenWidthDp / 7f).dp)

//            .background(color = Color.Blue)
            , onDraw = {
                val perWidthWithPadding = this.size.width / 7f

                //第一条数据是周几
                val firstWeek = calendarData.list[0].week

                //计算最大的列 跨度 一年的几周就是最大的列
                maxColumn = calendarData.list.groupBy { monthData ->
                    monthData.weekOfYear
                }.size

                val groupedData = calendarData.list.groupBy { monthData ->
                    monthData.week
                }

                groupedData.forEach { (week, monthDataList) ->
                    monthDataList.forEachIndexed { index, monthData ->
                        //按照列写的数据
//                    drawRoundRect(
//                        color = Color.Magenta,
//                        topLeft = Offset(
//                            (week - 1) * perWidthWithPadding + paddingPx,
//                            (index + if (week >= firstWeek) 0 else 1) * perWidthWithPadding - paddingPx
//                        ),
//                        size = Size(
//                            perWidthWithPadding - 2 * paddingPx,
//                            perWidthWithPadding - 2 * paddingPx
//                        )
//                    )


                        drawText(
                            textMeasurer = textMeasurer,
                            text = "${monthData.day}",
                            size = Size(
                                perWidthWithPadding - 2 * paddingPx,
                                perWidthWithPadding - 2 * paddingPx
                            ),
                            topLeft = Offset(
                                (week - 1) * perWidthWithPadding,

                                (index + if (week >= firstWeek) 0 else 1) * perWidthWithPadding
                                        //定位到中间位置
                                        + perWidthWithPadding * 0.5f
                                        //减去文字的高度
                                        - textSize.height / 2f
                            ),
                            style = TextStyle(
                                textAlign = TextAlign.Center,
                                color = if (monthData.isCurrentDay) Color.Red else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        )


                        //当天画圆
                        if (monthData.isCurrentDay) {
                            drawCircle(
                                color = Color.LightGray.copy(0.5f),
                                radius = (perWidthWithPadding - 2 * paddingPx) / 2f,
                                center = Offset(
                                    (week - 1) * perWidthWithPadding + paddingPx + perWidthWithPadding / 2f,
                                    (index + if (week >= firstWeek) 0 else 1) * perWidthWithPadding - paddingPx + perWidthWithPadding / 2f
                                ),
                            )
                        }
                    }
                }
            })
    }
}
