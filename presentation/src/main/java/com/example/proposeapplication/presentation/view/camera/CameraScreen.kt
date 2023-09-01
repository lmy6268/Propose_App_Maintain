package com.example.proposeapplication.presentation.view.camera

import android.graphics.Bitmap
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.presentation.view.camera.CameraModules.CompositionArrow
import com.example.proposeapplication.presentation.view.camera.CameraModules.LowerButtons
import com.example.proposeapplication.presentation.view.camera.CameraModules.UpperButtons
import com.example.proposeapplication.utils.pose.PoseData


@Composable
fun Screen(
    navController: NavHostController, mainViewModel: MainViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = PreviewView(context)
    val isUpdated = remember {
        mutableStateOf(false)
    }
    val isPressedFixedBtn = remember {
        mutableStateOf(false)
    }
    val viewRateIdxState = remember {
        mutableIntStateOf(0)
    }
    val selectedModeIdxState = remember {
        mutableIntStateOf(1)
    }
    val nextRecomPoseState = remember {
        mutableIntStateOf(0)
    }
    val recomPoseSizeState = remember {
        mutableIntStateOf(0)
    }

    val capturedEdgesBitmap: Bitmap? by mainViewModel.edgeDetectBitmapState.collectAsState() //업데이트된 고정화면을 가지고 있는 변수
    val capturedThumbnailBitmap: Bitmap by mainViewModel.capturedBitmapState.collectAsState() //업데이트된 캡쳐화면을 가지고 있는 변수
    val compResultDirection: String by mainViewModel.compResultState.collectAsState()
    val viewRateList = listOf(
        Pair(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY, 3 / 4F),
        Pair(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY, 6 / 19F)
    )
    val poseRecPair: Pair<DoubleArray?, List<PoseData>?>? by mainViewModel.poseResultState.collectAsState()

    LaunchedEffect(key1 = viewRateIdxState.intValue) {

        //뷰를 계속 업데이트 하면서 생겼던 오류
        if (isUpdated.value.not()) {
            mainViewModel.showPreview(
                lifecycleOwner,
                previewView.surfaceProvider,
                viewRateList[viewRateIdxState.intValue].first
            )
            previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            isUpdated.value = true
        }
    }



    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .wrapContentSize()
                .aspectRatio(viewRateList[viewRateIdxState.intValue].second)
                .then(
                    if (viewRateIdxState.intValue == 0) Modifier.offset(0.dp, (-100).dp)
                    else Modifier
                )

        ) {

        }

        Box(
            Modifier
                .matchParentSize()
                .aspectRatio(viewRateList[viewRateIdxState.intValue].second)
                .then(
                    if (viewRateIdxState.intValue == 0) Modifier.offset(0.dp, (-100).dp)
                    else Modifier
                )
//                .onGloballyPositioned {
//                    val metrics = context.resources.displayMetrics
//                    val dpSize = it.size.let { intSize ->
//                        IntSize(
//                            intSize.width / metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT,
//                            intSize.height / metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
//                        )
//                    }
//
//                    previewViewData.value = Pair(dpSize, it.positionInRoot())
//                }
                .align(Alignment.TopCenter)
        ) {
            if (poseRecPair != null && poseRecPair != Pair(null, null)) { //포즈 목록이 도착하면
                
//                Canvas(modifier = , onDraw = )
                
                recomPoseSizeState.intValue = poseRecPair!!.second!!.size
                Image(
                    modifier = Modifier
                        .size(DpSize(350.dp, 350.dp))
                        .align(Alignment.BottomCenter)
//                        .offset(.dp)
                    ,
//                        .offset(
//                            (poseRecPair!!.first[0] * previewViewData.value.first.width).dp,
//                            (previewViewData.value.first.height * poseRecPair!!.first[1]).dp
//                        )
//                        .widthIn((previewViewData.value.first.width * poseRecPair!!.first[2]).dp)
//                        .heightIn((previewViewData.value.first.height * poseRecPair!!.first[3]).dp),

                    painter = painterResource(id = poseRecPair!!.second!![nextRecomPoseState.intValue].poseDrawableId),
                    contentDescription = "포즈 이미지"
                )
            } else if (poseRecPair == Pair(null, null)) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }


        //다음 포즈 선택 버튼
        if ((selectedModeIdxState.intValue == 0 || selectedModeIdxState.intValue == 1) && recomPoseSizeState.intValue > 0)
            IconButton(modifier = Modifier
                .size(50.dp)
                .offset(x = (-20).dp)
                .align(
                    Alignment.CenterEnd
                ), onClick = {
                if (nextRecomPoseState.intValue in 0 until recomPoseSizeState.intValue-1)
                    nextRecomPoseState.intValue += 1
                else nextRecomPoseState.intValue = 0
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.based_circle),
                    contentDescription = "배경",
                    tint = Color.White
                )
                Icon(painter = painterResource(id = R.drawable.refresh), contentDescription = "배경")
            }
        UpperButtons(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .heightIn(100.dp)
                .fillMaxWidth(),
            navController = navController,
            selectedViewRateIdxState = viewRateIdxState,
            mainColor = MaterialTheme.colors.primary,
            selectedModeIdxState = selectedModeIdxState
        )

        CompositionArrow(
            arrowDirection =
            compResultDirection
//            ""
            ,
            modifier = Modifier
                .aspectRatio(viewRateList[viewRateIdxState.intValue].second)
                .heightIn(150.dp)
//                .widthIn(250.dp)
//                .height(200.dp)
                .align(Alignment.Center)

                .offset(y = (-100).dp)
        )


        //엣지 화면
        ShowEdgeImage(
            mainViewModel = mainViewModel,
            capturedEdgesBitmap = capturedEdgesBitmap,
            isPressedFixedBtn = isPressedFixedBtn,
            modifier = Modifier
                .matchParentSize()
                .aspectRatio(viewRateList[viewRateIdxState.intValue].second)
                .then(
                    if (viewRateIdxState.intValue == 0) Modifier.offset(0.dp, (-100).dp)
                    else Modifier
                )
//                .offset(y = (-80).dp)
        )


        LowerButtons(
            selectedModeIdxState = selectedModeIdxState,
            capturedThumbnailBitmap = capturedThumbnailBitmap,
            mainViewModel = mainViewModel,
            isPressedFixedBtn = isPressedFixedBtn,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .heightIn(150.dp)
                .padding(bottom = 100.dp)
        )

    }

}

@Composable
fun ShowEdgeImage(
    mainViewModel: MainViewModel,
    capturedEdgesBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    isPressedFixedBtn: MutableState<Boolean>
) {
    //Fix for AfterImage error when use fixedScreen feature.
    if (isPressedFixedBtn.value) {
        val reqState: Boolean by mainViewModel.reqFixedScreenState.collectAsState(true)
        LaunchedEffect(Unit) {
            mainViewModel.reqFixedScreenState.value = true
        }
        // 요청 처리가 끝났다면 -> 이미지를 받아온다.
        if (!reqState) capturedEdgesBitmap?.let { bitmap ->
            Image(
                modifier = modifier
                    .alpha(0.5F),

                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Edge Image"
            )
        }
    }

}







