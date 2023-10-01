package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.BitmapFactory
import android.view.MotionEvent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
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
    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    fun ScrollableRecommendPoseScreen(
        modifier: Modifier = Modifier,
        onDownActionEvent: (MotionEvent) -> Unit,
        poseDataList: List<PoseData>,
        clickedItemIndexState: Int, //버튼을 클릭했을 때 그 버튼의 인덱스
        pageCount: Int = poseDataList.size,
        lowerBarDisplayPxSize: IntSize,
        cameraDisplayPxSize: IntSize,
        onPoseChangeEvent: (PoseData) -> Unit,
        onPageChangeEvent: (Int) -> Unit
    ) {
        val pagerState = rememberPagerState(pageCount = { pageCount })


        LaunchedEffect(clickedItemIndexState) {
            //외부스크롤에 영향을 받음 -> 그런데 이를 실행하면, onPageChangeEvent(page)도 살행됨.
            //이러다 보니, 가끔 이전 것을 호출하는 버그가 발생함.
            pagerState.animateScrollToPage(clickedItemIndexState)
        }
//        LaunchedEffect(pagerState) {
//            // do your stuff with selected page
//            snapshotFlow { pagerState.currentPage }.collect { page ->
//                onPageChangeEvent(page)
//            }
//        }
        HorizontalPager(
            state = pagerState,
            modifier = modifier.pointerInteropFilter {
                if (it.action == MotionEvent.ACTION_DOWN) {

                }
                return@pointerInteropFilter false
            },
            userScrollEnabled = false
        ) {
            //이곳에 각 포즈 데이터를 저장하기만 하면 됨.
            PoseItem(
                poseData = poseDataList[it],
                lowerBarDisplayPxSize = lowerBarDisplayPxSize,
                cameraDisplayPxSize = cameraDisplayPxSize,
//                onPoseChangeEvent = onPoseChangeEvent
            )

        }
    }

    //사용자에게 보여주는 포즈 아이템에 대한 설정 ->
    @Composable
    fun PoseItem(
        poseData: PoseData,
        lowerBarDisplayPxSize: IntSize,
        cameraDisplayPxSize: IntSize,
//        onPoseChangeEvent: (PoseData) -> Unit
    ) {
        val context = LocalContext.current
        val offset = remember {
            mutableStateOf(
                Offset(
//                    -(cameraDisplayPxSize.width / 2f), -(cameraDisplayPxSize.height / 2f)
                    (cameraDisplayPxSize.width / 4f), (cameraDisplayPxSize.height / 4f)
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
        //해제 일 경우엔 보여주지 않음.
        if (poseData.poseId != -1) {
            Canvas( //그림 1
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = zoom.floatValue,
                        scaleY = zoom.floatValue,
                        translationX = offset.value.x,
                        translationY = offset.value.y
                    )
//                    .background(Color.White)
                    .transformable(state = transformState)
                    .size(200.dp)

            ) {
                drawImage(
                    image = BitmapFactory.decodeResource(context.resources,
                        poseData.poseDrawableId)
//                            .apply {
//                            onPoseChangeEvent(poseData)
//                        })
                        .asImageBitmap(),
                )
            }
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
            if (it != 0) "$customName #${it}"
            else "없음"
        }

        //여기에 해제 버튼도 하나 추가해야댐.
        val rowSizeState = remember { mutableStateOf(DpSize(width = 0.dp, height = 0.dp)) }
        val lazyRowState = rememberLazyListState()

        LaunchedEffect(key1 = clickedItemIndexState) {
            lazyRowState.animateScrollToItem(clickedItemIndexState)
        }
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
        onDownActionEvent: (MotionEvent) -> Unit,
        poseResultData: Pair<DoubleArray?, List<PoseData>?>?,
        rememberClickIndexState: Int,
        onPoseChangeEvent: (PoseData) -> Unit,
        onPageChangeEvent: (Int) -> Unit,
        onVisibilityEvent: () -> Unit
    ) {
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
                ScrollableRecommendPoseScreen(
                    modifier = Modifier.fillMaxSize(),
                    onDownActionEvent = onDownActionEvent,
                    poseDataList = poseResultData.second!!,
                    clickedItemIndexState = rememberClickIndexState,
                    cameraDisplayPxSize = cameraDisplayPxSize.value,
                    lowerBarDisplayPxSize = lowerBarDisplayPxSize.value,
                    onPoseChangeEvent = onPoseChangeEvent,
                    onPageChangeEvent = onPageChangeEvent
                )
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