package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.utils.pose.PoseData

object PoseScreen {

    //추천된 포즈를 스크롤 형식으로 넘겨볼 수 있게 보여주는 화면
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ScrollableRecommendPoseScreen(
        modifier: Modifier = Modifier,
        poseDataList: List<PoseData>,
        clickedItemIndexState: Int, //버튼을 클릭했을 때 그 버튼의 인덱스
        pageCount: Int = poseDataList.size,
        lowerBarDisplayPxSize: IntSize,
        cameraDisplayPxSize: IntSize,
        onPoseChangeEvent: (PoseData) -> Unit,
        onPageChangeEvent: (Int) -> Unit
    ) {
        val pagerState = rememberPagerState(pageCount = { pageCount })
        val scrollBySwitch = remember {
            mutableStateOf(false)
        }

        LaunchedEffect(clickedItemIndexState) {
            scrollBySwitch.value = true
            pagerState.animateScrollToPage(clickedItemIndexState)
            scrollBySwitch.value = false
        }
        LaunchedEffect(pagerState) {
            // do your stuff with selected page
            snapshotFlow { pagerState.currentPage }.collect { page ->
                if (scrollBySwitch.value.not()) onPageChangeEvent(page)
            }
        }
        HorizontalPager(state = pagerState, modifier = modifier) {
            //이곳에 각 포즈 데이터를 저장하기만 하면 됨.
            PoseItem(
                poseData = poseDataList[it],
                lowerBarDisplayPxSize = lowerBarDisplayPxSize,
                cameraDisplayPxSize = cameraDisplayPxSize,
                onPoseChangeEvent = onPoseChangeEvent
            )

        }
    }

    //사용자에게 보여주는 포즈 아이템에 대한 설정 ->
    @Composable
    fun PoseItem(
        modifier: Modifier = Modifier,
        poseData: PoseData,
        lowerBarDisplayPxSize: IntSize,
        cameraDisplayPxSize: IntSize,
        onPoseChangeEvent: (PoseData) -> Unit
    ) {
        val context = LocalContext.current

        val offset = remember {
            mutableStateOf(
                Offset(
//                    -(cameraDisplayPxSize.width / 2f), -(cameraDisplayPxSize.height / 2f)
                    0f, 0f
                )
            )
        }
        val zoom = remember {
            mutableFloatStateOf(1F)
        }

        val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
            if (zoom.floatValue * zoomChange in 0.5f..2f) zoom.floatValue *= zoomChange
            val tmp = offset.value + offsetChange
            if (tmp.x in -(cameraDisplayPxSize.width / 2f)..(cameraDisplayPxSize.width / 2f) && tmp.y in -(cameraDisplayPxSize.height / 2f)..(cameraDisplayPxSize.height / 2f)) offset.value += offsetChange
        }

        Canvas( //그림 1
            modifier = Modifier
//                .background(Color.White)
                .size(200.dp)
                .graphicsLayer(
                    scaleX = zoom.floatValue,
                    scaleY = zoom.floatValue,
                    translationX = offset.value.x,
                    translationY = offset.value.y
                )
//                .offset(offset.value.x.dp, offset.value.y.dp)
                .transformable(state = transformState)

        ) {
//                    if (poseResultData.first!!.isNotEmpty()) {
//                        offset = offset.copy(
//                            (poseRecPair!!.first!![0] * size.width).toFloat(),
//                            (poseRecPair!!.first!![0] * size.height).toFloat()
//                        )
//                    }

            drawImage(
                image = BitmapFactory.decodeResource(context.resources,
                    poseData.poseDrawableId.apply {
                        onPoseChangeEvent(poseData)
                    }).asImageBitmap(),
            )
        }
    }


    //스크롤 가능한 메뉴 바 -> 추천된 포즈 선택 시 이용
    @Composable
    fun RecommendedPoseSelectMenu(
        modifier: Modifier = Modifier,
        customName: String = "추천", //추천된 포즈를 나열할 때 쓰는 말
        poseCnt: Int, //전체 추천 포즈의 갯수
        clickedItemIndexState: Int, //현재 클릭된 추천 포즈에 대한 인덱스
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(20.dp),
        onItemClickEvent: (Int) -> Unit // 아이템 클릭을 입력받을 때, 발생되는 이벤트
    ) {
        val localDensity = LocalDensity.current
        val itemList = List(poseCnt) {
            "$customName #${it + 1}"
        } //여기에 해제 버튼도 하나 추가해야댐.
        val rowSizeState = remember { mutableStateOf(DpSize(width = 0.dp, height = 0.dp)) }
        val lazyRowState = rememberLazyListState()
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    rowSizeState.value =
                        rowSizeState.value.copy(width = with(localDensity) { it.size.width.toDp() },
                            height = with(localDensity) { it.size.height.toDp() })
                },
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            contentPadding = PaddingValues(horizontal = rowSizeState.value.width.div(2.5F)),
            state = lazyRowState
        ) {
            //인덱스를 돌면서, 해당 리스트에 있는 데이터를 가져온다.
            itemsIndexed(itemList) { idx, item ->
                LaunchedEffect(key1 = clickedItemIndexState) {
                    if (idx == clickedItemIndexState) lazyRowState.animateScrollToItem(idx)
                }
                MenuModule(text = item, isSelected = clickedItemIndexState == idx, onClickEvent = {
                    onItemClickEvent(idx)
                })
            }
        }

    }

    @Composable
    fun PoseResultScreen(
        modifier: Modifier = Modifier,
        cameraDisplaySize: State<IntSize>,
        cameraDisplayPxSize: State<IntSize>,
        lowerBarDisplayPxSize: State<IntSize>,
        upperButtonsRowSize: State<IntSize>,
        poseResultData: Pair<DoubleArray?, List<PoseData>?>?,
        clickedItemIndexState: Int,
        onPoseChangeEvent: (PoseData) -> Unit,
        onVisibilityEvent: () -> Unit
    ) {
//        val selectedPoseState = remember {
//            mutableIntStateOf(0)
//        }
//        val context = LocalContext.current
//
//        val offset = remember {
//            mutableStateOf(
//                Offset(
//                    cameraDisplayPxSize.value.width / 2f, cameraDisplayPxSize.value.height / 2f
//                )
//            )
//        }
//        val zoom = remember {
//            mutableFloatStateOf(1F)
//        }
//
//        val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
//            if (zoom.floatValue * zoomChange in 0.5f..2f) zoom.floatValue *= zoomChange
//            val tmp = offset.value + offsetChange
//            if (tmp.x in -1f..cameraDisplayPxSize.value.width.toFloat() - 1 && tmp.y in -1f..cameraDisplayPxSize.value.height.toFloat() - lowerBarDisplayPxSize.value.height.toFloat() + 20F) offset.value += offsetChange
//        }

        Box(modifier = modifier) {
            //만약에 포즈를 추천 중이라면
            if (poseResultData == null) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .sizeIn(30.dp)
                        .align(Alignment.Center)
                )
            }
            //만약에 포즈 추천이 완료되었다면
            else {
                val rememberClickIndexState = remember {
                    mutableIntStateOf(0)
                }

                ScrollableRecommendPoseScreen(
                    modifier = Modifier.size(
                        cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp
                    ),
                    poseDataList = poseResultData.second!!,
                    clickedItemIndexState = rememberClickIndexState.intValue,
                    cameraDisplayPxSize = cameraDisplayPxSize.value,
                    lowerBarDisplayPxSize = lowerBarDisplayPxSize.value,
                    onPoseChangeEvent = onPoseChangeEvent,
                    onPageChangeEvent = {
                        rememberClickIndexState.intValue = it
                    }
                )
                RecommendedPoseSelectMenu(poseCnt = poseResultData.second!!.size,
                    clickedItemIndexState = rememberClickIndexState.intValue,
                    onItemClickEvent = {
                        rememberClickIndexState.intValue = it
                    })
            }

        }
    }
}

//메뉴별 아이콘
@Composable
fun MenuModule(text: String, isSelected: Boolean, onClickEvent: () -> Unit) {
    Box(modifier = Modifier
        .widthIn(50.dp)
        .heightIn(36.dp)
        .background(
            color = Color(
                if (isSelected) 0xFF212121
                else 0x99212121 //투명하게
            ), shape = RoundedCornerShape(size = 18.dp)
        )
        .clickable {
            onClickEvent()
        }
        .padding(horizontal = 20.dp, vertical = 8.dp)


    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center),
            color = Color(
                if (isSelected) 0xFFFFFFFF
                else 0xFF000000 //검은색
            )
        )
    }

}


@Preview
@Composable
private fun TestMenuIcon() {
    Row {
        MenuModule(text = "추천#1", isSelected = true) {}
        MenuModule(text = "추천#2", isSelected = false) {}
    }
}

@Preview(widthDp = 360, heightDp = 360, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TestPager() {
    val clickedItemIndexState = remember { mutableIntStateOf(0) }
    val poseIdList = listOf(
        com.hanadulset.pro_poseapp.utils.R.drawable.key_image_0,
        com.hanadulset.pro_poseapp.utils.R.drawable.key_image_1,
        com.hanadulset.pro_poseapp.utils.R.drawable.key_image_2
    )

//
//    Column(Modifier.fillMaxSize()) {
//        PoseScreen.ScrollableRecommendPoseScreen(
//            modifier = Modifier
//                .wrapContentSize(),
//            pageCount = poseIdList.size,
//            clickedItemIndexState = clickedItemIndexState.intValue,
//            poseDataList = poseIdList,
//        )
//
//        PoseScreen.RecommendedPoseSelectMenu(
//            modifier = Modifier.wrapContentSize(),
//            poseCnt = poseIdList.size,
//            clickedItemIndexState = clickedItemIndexState.intValue,
//            horizontalArrangement = Arrangement.spacedBy(10.dp),
//            verticalAlignment = Alignment.Bottom
//        ) { index ->
//            clickedItemIndexState.intValue = index
//        }
//    }

}