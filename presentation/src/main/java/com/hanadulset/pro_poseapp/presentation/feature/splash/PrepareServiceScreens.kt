package com.hanadulset.pro_poseapp.presentation.feature.splash

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.component.LocalTypography
import com.hanadulset.pro_poseapp.presentation.component.UIComponents
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceScreens.SplashScreen
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import kotlinx.coroutines.delay

object PrepareServiceScreens {
    private const val APP_NAME = "Pro_Pose"
    private const val CATCH_PRAISE = "포즈, 이제 고민하지마."

    @Composable
    fun SplashScreen() {

        val appIconSize = 200.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            val resource = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.app_icon_rounded)
                    .size(with(LocalDensity.current) {
                        appIconSize.toPx().toInt()
                    }) //현재 버튼의 크기만큼 리사이징한다.
                    .placeholder(R.drawable.app_icon_rounded)
                    .build()
            )
            val style = LocalTypography.current
            val color = LocalColors.current
            val localDensity = LocalDensity.current



            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1F),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(100.dp, Alignment.CenterVertically)
            ) {
                Text(text = CATCH_PRAISE, style = style.heading02)

                Image(
                    modifier = Modifier
                        .size(appIconSize),
                    painter = resource,
                    contentDescription = "앱 아이콘",
                )

                Text(
                    modifier = Modifier,
                    text = APP_NAME,
                    style = style.heading02
                )
            }
//            Canvas(modifier = Modifier.fillMaxSize()) {
//                drawCircle(
//                    color = color.primary100,
//                    radius = localDensity.run { 256.dp.toPx() },
//                    offset =
//
//                    )
//            }

        }
    }

    @Composable
    fun AppLoadingScreen(
        previewState: State<CameraState>,
        cameraInit: () -> Unit,
        prepareServiceViewModel: PrepareServiceViewModel,
        isAfterDownload: Boolean,
        onAfterLoadedEvent: () -> Unit,
        onMoveToDownload: () -> Unit,
        networkState: Boolean = false
    ) {
        val totalLoadedState by prepareServiceViewModel.totalLoadedState.collectAsState()
        val checkNeedToDownloadState by prepareServiceViewModel.checkDownloadState.collectAsState()
        val localActivity = LocalContext.current as Activity
        val isInitiated = remember {
            mutableStateOf(false)
        }

        val afterLoaded by rememberUpdatedState(newValue = onAfterLoadedEvent)
        LaunchedEffect(Unit) {
            delay(1000)
            if (isAfterDownload) {
                prepareServiceViewModel.preLoadModel()
                cameraInit()
            } else prepareServiceViewModel.requestForCheckDownload()
        }

        LaunchedEffect(checkNeedToDownloadState) {
            if (checkNeedToDownloadState != null && isAfterDownload.not()) {
                //아마존 에러인 경우 처리 -> 보통은 와이파이 오류
                if (checkNeedToDownloadState!!.downloadType == CheckResponse.TYPE_ERROR) localActivity.finish()
                else if (checkNeedToDownloadState!!.needToDownload.not()) {
                    prepareServiceViewModel.preLoadModel()
                    cameraInit()
                } else onMoveToDownload()
            }
        }

        if (totalLoadedState && previewState.value.cameraStateId == CameraState.CAMERA_INIT_COMPLETE && isInitiated.value.not()) {
            afterLoaded()//카메라 화면으로 이동하는 거임.
            isInitiated.value = true
        } else InnerAppLoadingScreen()


    }

    //로딩 중일 때 보여주는 화면
    @Composable
    fun InnerAppLoadingScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            val fontStyle = LocalTypography.current
            val colorTheme = LocalColors.current
            Image(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center),
                painter = painterResource(id = R.drawable.app_icon_rounded),
                contentDescription = "앱 아이콘",
            )
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UIComponents.CircularWaitingBar(
                    modifier = Modifier.padding(20.dp)
                )
                Text(
                    text = "Pro_Pose 로딩 중...",
                    style = fontStyle.heading02
                )
            }

        }


    }


}


//@Preview(name = "NEXUS_5", device = Devices.NEXUS_5)
//@Preview(name = "NEXUS_6", device = Devices.NEXUS_6)
//@Preview(name = "NEXUS_5X", device = Devices.NEXUS_5X)
//@Preview(name = "NEXUS_6P", device = Devices.NEXUS_6P)
//@Preview(name = "PIXEL", device = Devices.PIXEL)
//@Preview(name = "PIXEL_2", device = Devices.PIXEL_2)
//@Preview(name = "PIXEL_3", device = Devices.PIXEL_3)
//@Preview(name = "PIXEL_3_XL", device = Devices.PIXEL_3_XL)
//@Preview(name = "PIXEL_3A", device = Devices.PIXEL_3A)
//@Preview(name = "PIXEL_3A_XL", device = Devices.PIXEL_3A_XL)
//@Preview(name = "PIXEL_4", device = Devices.PIXEL_4)
//@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
//@Composable
//fun PreViewSplash() {
//    SplashScreen()
//}
//
//
//@Preview(name = "NEXUS_5", device = Devices.NEXUS_5)
//@Preview(name = "NEXUS_6", device = Devices.NEXUS_6)
//@Preview(name = "NEXUS_5X", device = Devices.NEXUS_5X)
//@Preview(name = "NEXUS_6P", device = Devices.NEXUS_6P)
//@Preview(name = "PIXEL", device = Devices.PIXEL)
//@Preview(name = "PIXEL_2", device = Devices.PIXEL_2)
//@Preview(name = "PIXEL_3", device = Devices.PIXEL_3)
//@Preview(name = "PIXEL_3_XL", device = Devices.PIXEL_3_XL)
//@Preview(name = "PIXEL_3A", device = Devices.PIXEL_3A)
//@Preview(name = "PIXEL_3A_XL", device = Devices.PIXEL_3A_XL)
//@Preview(name = "PIXEL_4", device = Devices.PIXEL_4)
//@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
//@Preview
//@Composable
//fun PreviewInnerAppLoadingScreen() {
//    PrepareServiceScreens.InnerAppLoadingScreen()
//}

