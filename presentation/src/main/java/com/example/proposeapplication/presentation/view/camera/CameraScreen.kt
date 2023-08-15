package com.example.proposeapplication.presentation.view.camera

import android.graphics.Bitmap
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.view.camera.CameraModules.CompositionArrow
import com.example.proposeapplication.presentation.view.camera.CameraModules.LowerButtons
import com.example.proposeapplication.presentation.view.camera.CameraModules.UpperButtons


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

    val capturedEdgesBitmap: Bitmap? by mainViewModel.edgeDetectBitmapState.collectAsState() //업데이트된 고정화면을 가지고 있는 변수
    val capturedThumbnailBitmap: Bitmap by mainViewModel.capturedBitmapState.collectAsState() //업데이트된 캡쳐화면을 가지고 있는 변수
    val compResultDirection: String by mainViewModel.compResultState.collectAsState()
    val viewRateList = listOf(
        Pair(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY, 3 / 4F),
        Pair(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY, 6 / 19F)
    )
    val poseRecString: String by mainViewModel.poseResultState.collectAsState()

    LaunchedEffect(key1 = viewRateIdxState) {

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
    Box(Modifier.fillMaxSize()) {
        UpperButtons(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .heightIn(100.dp)
                .fillMaxWidth(),
            navController = navController,
            remember {
                mutableIntStateOf(0)
            }
        )
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .wrapContentSize()
                .aspectRatio(viewRateList[viewRateIdxState.intValue].second)
                .align(Alignment.Center)
                .offset(y = (-80).dp)

        ) {
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
        CompositionArrow(
            arrowDirection = compResultDirection,
            modifier = Modifier
                .matchParentSize()
                .aspectRatio(viewRateList[viewRateIdxState.intValue].second)
                .offset(y = (-80).dp)
        )


        //엣지 화면
        if (isPressedFixedBtn.value) {
            //Fix for AfterImage error when use fixedScreen feature.
            val reqState: Boolean by mainViewModel.reqFixedScreenState.collectAsState(true)
            LaunchedEffect(Unit) {
                mainViewModel.reqFixedScreenState.value = true
            }
            if (!reqState) capturedEdgesBitmap?.let { bitmap ->
                Image(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = (-80).dp)
                        .alpha(0.5F),
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Edge Image"
                )
            }
        }


        LowerButtons(
            capturedThumbnailBitmap = capturedThumbnailBitmap,
            mainViewModel = mainViewModel,
            isPressedFixedBtn = isPressedFixedBtn,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

    }
}

//@Composable
//@Preview
//private fun PreviewScreen() {
//    Screen(
//        navController = NavHostController(LocalContext.current), mainViewModel = viewModel(
//            modelClass = MainViewModel::class.java
//        )
//    )
//}







