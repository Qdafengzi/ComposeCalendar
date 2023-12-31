package com.future.composecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.composecalendar.ui.theme.ComposeCalendarTheme
import com.future.composecalendar.utils.XLogger
import com.future.composecalendar.viewmodel.HomeAction
import com.future.composecalendar.viewmodel.HomeUIState
import com.future.composecalendar.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCalendarTheme {
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

@OptIn(ExperimentalTextApi::class, ExperimentalFoundationApi::class)
@Composable
fun Calendar(homeViewModel: HomeViewModel = viewModel()) {
    val pagerState = rememberPagerState(initialPage = 5000)
    YearMonthSelectDialog(viewModel = homeViewModel,pagerState)

    XLogger.d("==================>Calendar")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
    ) {
        stickyHeader(key = "stickyHeader") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
            ) {
                val textMeasurerAndTextSize = getTextMeasurerAndTextSize()
                YearAndMonth(homeViewModel, pagerState)
                //星期
                WeekRow()
                //日历信息
                CalendarPager(
                    homeViewModel = homeViewModel,
                    textMeasurerAndTextSize = textMeasurerAndTextSize,
                    pagerState
                )
            }
        }
        repeat(20) {
            item(key = it) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(50.dp)
                        .background(
                            color = Color.White
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = if (it % 2 == 0) Color.Gray.copy(alpha = 0.5f) else Color.LightGray.copy(
                                    alpha = 0.5f
                                )
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.padding(start = 12.dp),
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )

                        Text(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .fillMaxWidth(), text = "item $it", color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun YearMonthSelectDialog(viewModel: HomeViewModel, pagerState: PagerState) {
    val homeUIState = viewModel.homeUiState.collectAsState().value

    val clickDay = homeUIState.clickDay
    val year = clickDay.year

    if (homeUIState.showYearMonthDialog) {
        val coroutineScope = rememberCoroutineScope()
        val monthArray = integerArrayResource(id = R.array.month)
        val yearList = mutableListOf<Int>()
        for (day in 0..year + 5000) {
            yearList.add(day)
        }

        val listState = rememberLazyListState()
        LaunchedEffect(key1 = Unit, block = {
            listState.scrollToItem(index = year)
        })

        val pageState = rememberPagerState()

        var selectMonth by remember {
            mutableStateOf(clickDay.month+1)
        }

        var selectYear by remember {
            mutableStateOf(clickDay.year)
        }

        XLogger.d("------>$selectMonth    $selectYear" )

        AlertDialog(modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight(),
            onDismissRequest = {
                viewModel.dispatch(HomeAction.ShowYearMonthSelectDialog(false))
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .background(
                        color = Color.White, shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                    ,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                coroutineScope.launch {
                                    pageState.scrollToPage(0)
                                }
                            },
                        text = "${selectYear}年",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                coroutineScope.launch {
                                    pageState.scrollToPage(1)
                                }
                            },
                        text = "${selectMonth}月",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    pageCount = 2,
                    state = pageState
                ) { page ->
                    if (page == 0) {
                        LazyRow(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                            state = listState,
                            content = {
                                itemsIndexed(items = yearList, key = { index, _ ->
                                    index
                                }, itemContent = { _, content ->
                                    Text(text = "$content",
                                        color = if (selectYear == content) Color.Red  else Color.Black,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clickable {
                                                selectYear = content
                                            }
                                            .padding(horizontal = 6.dp, vertical = 6.dp)
                                    )
                                })
                            })
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxWidth(),
                            columns = GridCells.Fixed(4), content = {
                                itemsIndexed(items = monthArray.asList(), key = { index, _ ->
                                    index
                                }, itemContent = { _, content ->
                                    Text(text = "${content}月",
                                        color = if (selectMonth== content) Color.Red  else Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectMonth = content
                                            }
                                            .padding(vertical = 10.dp)
                                    )
                                })
                            })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                       ,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(modifier = Modifier.padding(horizontal = 20.dp),
                        onClick = {
                            viewModel.dispatch(HomeAction.ShowYearMonthSelectDialog(false))
                        }) {
                        Text(text = "取消")
                    }

                    TextButton(modifier = Modifier.padding(horizontal = 20.dp),
                        onClick = {
                            //月份的跨度
                            val calendar = Calendar.getInstance()

                            val todayYear = calendar.get(Calendar.YEAR)
                            val todayMonth =calendar.get(Calendar.MONTH)

                            //月份的跨度
                            val yearDiff: Int = selectYear -todayYear
                            val monthDiff: Int = selectMonth - todayMonth
                            val totalMonthDiff = yearDiff * 12 + monthDiff

                            XLogger.d("todayYear:$todayYear todayMonth:$todayMonth  selectYear:$selectYear  selectMonth:$selectMonth  totalMonthDiff:$totalMonthDiff")

                            coroutineScope.launch {
                                XLogger.d("=========>${totalMonthDiff + 5000}")
                                pagerState.scrollToPage(totalMonthDiff + 5000-1)
                            }
                            viewModel.dispatch(HomeAction.ShowYearMonthSelectDialog(false))
                        }) {
                        Text(text = "确定")
                    }
                }
            }
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YearAndMonth(homeViewModel: HomeViewModel, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    val homeUiState = homeViewModel.homeUiState.collectAsState(initial = HomeUIState()).value
    val clickDay = homeUiState.clickDay
    XLogger.d("YearAndMonth=======================>${clickDay}")
    //TODO：点击回到  年月日
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(modifier = Modifier.padding(start = 20.dp), onClick = {
            if (pagerState.currentPage - 1 >= 0) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        }) {
            Icon(
                imageVector = Icons.Default.ArrowBack, contentDescription = "上个月",
                tint = Color.Black.copy(alpha = 0.6f)
            )
        }

        TextButton(onClick = {
            homeViewModel.dispatch(HomeAction.ShowYearMonthSelectDialog(true))
        }) {
            Text(
                text = "${clickDay.year}年${clickDay.month + 1}月${clickDay.day}日",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }

        IconButton(modifier = Modifier.padding(end = 20.dp), onClick = {
            if (pagerState.currentPage + 1 < 10000) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "下个月",
                tint = Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
fun CalendarPager(
    homeViewModel: HomeViewModel,
    textMeasurerAndTextSize: Pair<TextMeasurer, IntSize>,
    pagerState: PagerState
) {
    XLogger.d("CalendarPager==================>")


    UpdatePagerState(homeViewModel, pagerState)

    HorizontalPager(
        pageCount = 10000,
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        XLogger.d("page Index========>$it")
        CalendarPagerContent(
            homeViewModel = homeViewModel,
            textMeasurerAndTextSize = textMeasurerAndTextSize,
            page = it
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatePagerState(homeViewModel: HomeViewModel, pagerState: PagerState) {
    XLogger.d("================>UpdatePagerState")
    val homeUIState = homeViewModel.homeUiState.collectAsState().value
    val monthOffset = homeUIState.monthEntity.offset
    val weekOffset = homeUIState.weekEntity.offset
    val offset = if (homeUIState.weekModelFlag) {
        weekOffset
    } else {
        monthOffset
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            XLogger.d("snapshotFlow============>$page")
            homeViewModel.dispatch(HomeAction.UpdateData(page))
        }
    }

    LaunchedEffect(key1 = homeUIState.needScrollPage, block = {
        XLogger.d("滑动到：${homeUIState.needScrollPage}页")
        if (homeUIState.needScrollPage >= 0 && homeUIState.needScrollPage != offset) {
            pagerState.scrollToPage(homeUIState.needScrollPage)
        }
    })
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun CalendarPagerContent(
    homeViewModel: HomeViewModel,
    textMeasurerAndTextSize: Pair<TextMeasurer, IntSize>,
    page: Int,
) {
    XLogger.d("CalendarContent======>")
    val homeUiState = homeViewModel.homeUiState.collectAsState().value
    val weekModel = homeUiState.weekModelFlag
    val monthEntity = homeUiState.monthEntity
    val weekEntity = homeUiState.weekEntity

    val (textMeasurer, textSize) = textMeasurerAndTextSize

    val paddingPx = 2

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val clickDay = homeUiState.clickDay

    val height = if (weekModel) {
        (screenWidthDp / 7f).dp
    } else {
        (monthEntity.dayList.size / 7 * (screenWidthDp / 7f)).dp
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(height)
//        .background(color = Color.Magenta)
//        .animateContentSize()
        .pointerInput(key1 = homeUiState) {
            detectTapGestures(onTap = { offset ->
                val clickIndex = if (!weekModel) {
                    val perWidthWithDp = screenWidthDp / 7f
                    val widthIndex = ceil(offset.x / perWidthWithDp.dp.toPx()).toInt()
                    val heightIndex = ceil(offset.y / perWidthWithDp.dp.toPx()).toInt()
                    (heightIndex - 1) * 7 + widthIndex - 1
                } else {
                    val perWidthWithDp = screenWidthDp / 7f
                    ceil(offset.x / perWidthWithDp.dp.toPx()).toInt() - 1
                }
                homeViewModel.dispatch(HomeAction.ItemClick(clickIndex, page))
            })
        }
        .pointerInput(key1 = homeUiState) {
            detectVerticalDragGestures { change, dragAmount ->
                XLogger.d("detectDragGestures=======>change:${change.position.y}  dragAmount:${dragAmount}")
                if (dragAmount >= 20) {
                    XLogger.d("------------>月历")
                    homeViewModel.dispatch(HomeAction.SetCalendarModel(false, page))
                }
                if (dragAmount <= -20) {
                    XLogger.d("------------>周历")
                    homeViewModel.dispatch(HomeAction.SetCalendarModel(true, page = page))
                }
            }
        }, onDraw = {

        val perWidthWithPadding = this.size.width / 7f

        if (!weekModel) {
            XLogger.d("月历模式")
            monthEntity.dayList.forEachIndexed { index, monthData ->
                val week = index % 7
                val rowIndex = index / 7
                //XLogger.d("每日的数据 ${monthData.year}-${monthData.month+1}-${monthData.day}-${monthData.color}")
                val textColor =
                    if (monthData.year == clickDay.year && monthData.month == clickDay.month && monthData.day == clickDay.day) {
                        Color.White
                    } else if (monthData.isWeekend && monthData.month == monthEntity.month) {
                        Color.Red
                    } else {
                        monthData.color
                    }
                val backgroundColor: Color =
                    if (monthData.year == clickDay.year && monthData.month == clickDay.month && monthData.day == clickDay.day) {
                        //点击的画圆背景
                        Color.Blue.copy(0.5f)
                    } else if (monthData.isCurrentDay) {
                        //当天画圆背景
                        Color.LightGray.copy(0.5f)
                    } else {
                        Color.Transparent
                    }
                drawCircle(
                    color = backgroundColor,
                    radius = (perWidthWithPadding - 2 * paddingPx) / 2f,
                    center = Offset(
                        week * perWidthWithPadding + paddingPx + perWidthWithPadding / 2f,
                        rowIndex * perWidthWithPadding - paddingPx + perWidthWithPadding / 2f
                    ),
                )

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
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                )

                if (monthData.isCurrentDay) {
                    //今天的背景
                    val todayRadius = (perWidthWithPadding - 2 * paddingPx) / 8f
                    drawCircle(
                        color = Color.White,
                        radius = todayRadius,
                        center = Offset(
                            week * perWidthWithPadding + perWidthWithPadding * 0.75f + todayRadius,
                            rowIndex * perWidthWithPadding + todayRadius
                        ),
                    )
                    //今天的文字 大小是0.75倍的宽度
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "今",
                        size = Size(
                            (perWidthWithPadding - 2 * paddingPx) / 4f,
                            (perWidthWithPadding - 2 * paddingPx) / 4f
                        ),
                        topLeft = Offset(
                            week * perWidthWithPadding + perWidthWithPadding * 0.75f,
                            rowIndex * perWidthWithPadding
                        ),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.Magenta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    )
                }
            }
        } else {
            XLogger.d("周历模式")
            weekEntity.dayList.forEachIndexed { index, dayEntity ->
                //一行 当前的日期
                //XLogger.d("每日的数据 ${monthData.year}-${monthData.month+1}-${monthData.day}-${monthData.color}")
                val textColor =
                    if (dayEntity.year == clickDay.year && dayEntity.month == clickDay.month && dayEntity.day == clickDay.day) {
                        Color.White
                    } else if (dayEntity.isWeekend && dayEntity.month == weekEntity.month) {
                        Color.Red
                    } else {
                        dayEntity.color
                    }

                val backgroundColor: Color =
                    if (dayEntity.year == clickDay.year && dayEntity.month == clickDay.month && dayEntity.day == clickDay.day) {
                        //点击的画圆背景
                        Color.Blue.copy(0.5f)
                    } else if (dayEntity.isCurrentDay) {
                        //当天画圆背景
                        Color.LightGray.copy(0.5f)
                    } else {
                        Color.Transparent
                    }

                drawCircle(
                    color = backgroundColor,
                    radius = (perWidthWithPadding - 2 * paddingPx) / 2f,
                    center = Offset(
                        index * perWidthWithPadding + paddingPx + perWidthWithPadding / 2f,
                        perWidthWithPadding / 2f
                    ),
                )

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
                        color = if (textColor == Color.LightGray) Color.Black else textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                )

                if (dayEntity.isCurrentDay) {
                    //今天的背景
                    val todayRadius = (perWidthWithPadding - 2 * paddingPx) / 8f
                    drawCircle(
                        color = Color.White,
                        radius = todayRadius,
                        center = Offset(
                            index * perWidthWithPadding + perWidthWithPadding * 0.75f + todayRadius,
                            todayRadius
                        ),
                    )
                    //今天的文字 大小是0.75倍的宽度
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "今",
                        size = Size(
                            (perWidthWithPadding - 2 * paddingPx) / 4f,
                            (perWidthWithPadding - 2 * paddingPx) / 4f
                        ),
                        topLeft = Offset(
                            index * perWidthWithPadding + perWidthWithPadding * 0.75f,
                            0f
                        ),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.Magenta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    )
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