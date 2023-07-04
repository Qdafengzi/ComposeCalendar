package com.future.composecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.composecalendar.data.MonthEntity
import com.future.composecalendar.data.WeekEntity
import com.future.composecalendar.ui.theme.ComposeCalendarTheme
import com.future.composecalendar.utils.XLogger
import com.future.composecalendar.viewmodel.HomeAction
import com.future.composecalendar.viewmodel.HomeUIState
import com.future.composecalendar.viewmodel.HomeViewModel
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
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


@OptIn(ExperimentalTextApi::class)
@Composable
fun Calendar(homeViewModel: HomeViewModel = viewModel()) {
    XLogger.d("==================>Calendar")
    val textMeasurerAndTextSize = getTextMeasurerAndTextSize()
    Column(modifier = Modifier.fillMaxSize()) {
        YearAndMonth(homeViewModel)
        //星期
        WeekRow()
        //日历信息
        CalendarPager(
            homeViewModel = homeViewModel,
            textMeasurerAndTextSize = textMeasurerAndTextSize
        )

        Box(
            modifier = Modifier
                .height(50.dp)
                .background(color = Color.Red)
        ) {
            Text(text = "啦啦啦")
        }
    }
}

/**
 * 星期信息
 */
@Composable
fun WeekRow() {
    val weekArray = stringArrayResource(id = R.array.week)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        weekArray.forEachIndexed { _, s ->
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

/**
 * 年和月
 */
@Composable
fun YearAndMonth(homeViewModel: HomeViewModel) {
    val homeUiState = homeViewModel.homeUiState.collectAsState(initial = HomeUIState()).value
    val clickDay = homeUiState.clickDay
    XLogger.d("YearAndMonth=======================>${clickDay}")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "${clickDay.first}年",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = "${clickDay.second + 1}月",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Text(
            text = "${clickDay.third}日",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
fun CalendarPager(
    homeViewModel: HomeViewModel,
    textMeasurerAndTextSize: Pair<TextMeasurer, IntSize>
) {
    XLogger.d("CalendarPager==================>")
    val homeUiState = homeViewModel.homeUiState.collectAsState()
    val monthEntityList = homeUiState.value.monthEntityList
    val weekEntityList = homeUiState.value.weekEntityList
    val weekModel = homeUiState.value.weekModel

//    LaunchedEffect(key1 = pagerState.currentPage, block = {
//        XLogger.d("=======>${pagerState.currentPage}")
//        //TODO：监听 滑动到<=2 或 size-2 的时候追加 日期数据
//    })
    val pagerState = rememberPagerState()

    UpdatePagerState(homeViewModel, pagerState)

    HorizontalPager(
        pageCount = if (weekModel) weekEntityList.size else monthEntityList.size,
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        XLogger.d(" page Index========>$it")


        CalendarPagerContent(
            homeViewModel = homeViewModel,
            textMeasurerAndTextSize = textMeasurerAndTextSize,
            monthEntity = if (weekModel) null else monthEntityList[it],
            weekEntity = if (weekModel) weekEntityList[it] else null,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatePagerState(homeViewModel: HomeViewModel, pagerState: PagerState) {
    val homeUIState = homeViewModel.homeUiState.collectAsState().value
    LaunchedEffect(key1 = homeUIState.needScrollPage, block = {
        XLogger.d("滑动到：${homeUIState.needScrollPage}页")
        pagerState.scrollToPage(homeUIState.needScrollPage)
    })


    //TODO: 触发 问题
    LaunchedEffect(key1 = pagerState.targetPage, key2 = homeUIState.needScrollPage){
        homeViewModel.dispatch(HomeAction.UpdateCurrentPage(page = pagerState.targetPage))
    }
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun CalendarPagerContent(
    homeViewModel: HomeViewModel,
    textMeasurerAndTextSize: Pair<TextMeasurer, IntSize>,
    monthEntity: MonthEntity?,
    weekEntity: WeekEntity?,

    ) {
    XLogger.d("CalendarContent======>")
    val homeUiState = homeViewModel.homeUiState.collectAsState().value
    val weekModel = homeUiState.weekModel

    val textMeasurer = textMeasurerAndTextSize.first
    val textSize = textMeasurerAndTextSize.second
    val paddingPx = 2

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val clickDay = homeUiState.clickDay

    val height: Dp? = if (weekModel) {
        (screenWidthDp / 7f).dp
    } else {
        monthEntity?.let {
            (monthEntity.monthList.size / 7 * (screenWidthDp / 7f)).dp
        }
    }

    XLogger.d("height=====monthList:${height?.value}")

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(height ?: 1.dp)
//        .background(color = Color.Magenta)
//        .animateContentSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = { offset ->
                if (weekModel) {
                    weekEntity?.let {
                        XLogger.d("onTap x y =========>${offset.x} ${offset.y}")
                        val perWidthWithDp = screenWidthDp / 7f
                        val widthIndex = ceil(offset.x / perWidthWithDp.dp.toPx()).toInt()
                        val weekData = weekEntity.weekList[widthIndex - 1]
                        XLogger.d("click========>${weekData.year}-${weekData.month + 1}-${weekData.day}")
                        homeViewModel.dispatch(
                            HomeAction.ItemClick(Triple(weekData.year, weekData.month, weekData.day))
                        )
                    }
                } else {
                    monthEntity?.let {
                        XLogger.d("onTap x y =========>${offset.x} ${offset.y}")
                        val perWidthWithDp = screenWidthDp / 7f
                        val widthIndex = ceil(offset.x / perWidthWithDp.dp.toPx()).toInt()
                        val heightIndex = ceil(offset.y / perWidthWithDp.dp.toPx()).toInt()
                        val monthData =
                            monthEntity.monthList[(heightIndex - 1) * 7 + widthIndex - 1]
                        XLogger.d("click========>${monthData.year}-${monthData.month + 1}-${monthData.day}")
                        homeViewModel.dispatch(HomeAction.ItemClick(Triple(monthData.year,monthData.month,monthData.day))
                        )
                    }
                }
            })
        }
        .pointerInput(Unit) {
            detectVerticalDragGestures { change, dragAmount ->
                XLogger.d("detectDragGestures=======>change:${change.position.y}  dragAmount:${dragAmount}")
                if (dragAmount >= 20) {
                    homeViewModel.dispatch(HomeAction.SetCalendarModel(false))
                }
                if (dragAmount <= -20) {
                    homeViewModel.dispatch(HomeAction.SetCalendarModel(true))
                }
            }
        }, onDraw = {

        val perWidthWithPadding = this.size.width / 7f

        if (!weekModel) {
            monthEntity?.let {
                monthEntity.monthList.forEachIndexed { index, monthData ->
                    XLogger.d("月历模式")
                    val week = index % 7
                    val rowIndex = index / 7
                    //XLogger.d("每日的数据 ${monthData.year}-${monthData.month+1}-${monthData.day}-${monthData.color}")
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${monthData.day}",
                        size = Size(
                            perWidthWithPadding - 2 * paddingPx,
                            perWidthWithPadding - 2 * paddingPx
                        ),
                        topLeft = Offset(
                            week * perWidthWithPadding,
                            rowIndex * perWidthWithPadding
                                    //定位到中间位置
                                    + perWidthWithPadding * 0.5f
                                    //减去文字的高度
                                    - textSize.height / 2f
                        ),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = monthData.color,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    )


                    val currentDayTriple = Triple(monthData.year, monthData.month, monthData.day)

                    if (clickDay == currentDayTriple) {
                        drawCircle(
                            color = Color.Blue.copy(0.5f),
                            radius = (perWidthWithPadding - 2 * paddingPx) / 2f,
                            center = Offset(
                                week * perWidthWithPadding + paddingPx + perWidthWithPadding / 2f,
                                rowIndex * perWidthWithPadding - paddingPx + perWidthWithPadding / 2f
                            ),
                        )
                    } else if (monthData.isCurrentDay) {
                        //当天画圆背景
                        drawCircle(
                            color = Color.LightGray.copy(0.5f),
                            radius = (perWidthWithPadding - 2 * paddingPx) / 2f,
                            center = Offset(
                                week * perWidthWithPadding + paddingPx + perWidthWithPadding / 2f,
                                rowIndex * perWidthWithPadding - paddingPx + perWidthWithPadding / 2f
                            ),
                        )
                    }
                }
            }

        } else {
            XLogger.d("周历模式")
            weekEntity?.let {
                weekEntity.weekList.forEachIndexed { index, dayEntity ->
                    XLogger.d("周历模式weekList")
                    //一行 当前的日期
                    //XLogger.d("每日的数据 ${monthData.year}-${monthData.month+1}-${monthData.day}-${monthData.color}")
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${dayEntity.day}",
                        size = Size(
                            perWidthWithPadding - 2 * paddingPx,
                            perWidthWithPadding - 2 * paddingPx
                        ),
                        topLeft = Offset(
                            index * perWidthWithPadding,
                            perWidthWithPadding * 0.5f
                                    //减去文字的高度
                                    - textSize.height / 2f
                        ),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    )

                    val currentDayTriple = Triple(dayEntity.year, dayEntity.month, dayEntity.day)

                    if (clickDay == currentDayTriple) {
                        drawCircle(
                            color = Color.Blue.copy(0.5f),
                            radius = (perWidthWithPadding - 2 * paddingPx) / 2f,
                            center = Offset(
                                index * perWidthWithPadding + paddingPx + perWidthWithPadding / 2f,
                                perWidthWithPadding / 2f
                            ),
                        )
                    } else if (dayEntity.isCurrentDay) {
                        //当天画圆背景
                        drawCircle(
                            color = Color.LightGray.copy(0.5f),
                            radius = (perWidthWithPadding - 2 * paddingPx) / 2f,
                            center = Offset(
                                index * perWidthWithPadding + paddingPx + perWidthWithPadding / 2f,
                                 perWidthWithPadding / 2f
                            ),
                        )
                    }
                }
            }
        }
    })
}


/**
 * 获取 TextMeasurer 测量文字的高度
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun getTextMeasurerAndTextSize(): Pair<TextMeasurer, IntSize> {
    val textMeasurer = rememberTextMeasurer(cacheSize = 0)
    val textLayoutResult: TextLayoutResult =
        textMeasurer.measure(
            text = "9",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        )
    val textSize = textLayoutResult.size

    return Pair(textMeasurer, textSize)
}
