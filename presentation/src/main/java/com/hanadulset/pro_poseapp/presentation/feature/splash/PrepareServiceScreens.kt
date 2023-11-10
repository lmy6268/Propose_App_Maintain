package com.hanadulset.pro_poseapp.presentation.feature.splash

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
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
import com.hanadulset.pro_poseapp.presentation.core.CustomDialog.InternetConnectionDialog
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceScreens.SplashScreen
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import kotlinx.coroutines.delay

object PrepareServiceScreens {
    private const val APP_NAME = "프로_포즈"
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
                    .data(R.drawable.app_icon_rounded_without_background)
                    .size(with(LocalDensity.current) {
                        appIconSize.toPx().toInt()
                    }) //현재 버튼의 크기만큼 리사이징한다.
                    .placeholder(R.drawable.app_icon_rounded_without_background)
                    .build()
            )
            val style = LocalTypography.current
            val color = LocalColors.current
            val localDensity = LocalDensity.current
            val positionOfTitle = remember {
                mutableStateOf(Offset.Zero)
            }



            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1F),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(150.dp, Alignment.CenterVertically)
            ) {
                Text(text = CATCH_PRAISE, style = style.heading01)

                Image(
                    modifier = Modifier
                        .size(appIconSize),
                    painter = resource,
                    contentDescription = "앱 아이콘",
                )

                Text(
                    modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                        positionOfTitle.value = layoutCoordinates.positionInRoot()
                    },
                    text = APP_NAME,
                    style = style.heading01
                )
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = color.primaryGreen100,
                    radius = localDensity.run { 320.dp.toPx() },
                    center = localDensity.run {
                        positionOfTitle.value.let {
                            Offset(it.x + 50.dp.toPx(), it.y + 50.dp.toPx())
                        }
                    }
                )
            }

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
                if (checkNeedToDownloadState!!.needToDownload.not()) {
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
        val appIconSize = 200.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            val resource = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.app_icon_rounded_without_background)
                    .size(with(LocalDensity.current) {
                        appIconSize.toPx().toInt()
                    }) //현재 버튼의 크기만큼 리사이징한다.
                    .placeholder(R.drawable.app_icon_rounded_without_background)
                    .build()
            )
            val style = LocalTypography.current
            val color = LocalColors.current
            val localDensity = LocalDensity.current
            val positionOfTitle = remember {
                mutableStateOf(Offset.Zero)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1F)
                    .padding(top = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(150.dp, Alignment.CenterVertically)
            ) {
                Text(text = CATCH_PRAISE, style = style.heading01)

                Image(
                    modifier = Modifier
                        .size(appIconSize),
                    painter = resource,
                    contentDescription = "앱 아이콘",
                )

                Column(
                    modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                        positionOfTitle.value = layoutCoordinates.positionInRoot()
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        20.dp,
                        Alignment.CenterVertically
                    )
                ) {
                    UIComponents.CircularWaitingBar(
                        barColor = LocalColors.current.secondaryWhite100,
                        backgroundColor = LocalColors.current.subSecondaryGray100
                    )
                    Text(text = "$APP_NAME 로딩중...", style = style.heading02)

                }
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = color.primaryGreen100,
                    radius = localDensity.run { 320.dp.toPx() },
                    center = localDensity.run {
                        positionOfTitle.value.let {
                            Offset(it.x + 60.dp.toPx(), it.y + 50.dp.toPx())
                        }
                    }

                )
            }

        }


    }


    //약관 동의 화면
    @Composable
    fun AppUseAgreementScreen(
        modifier: Modifier = Modifier,
        agreementText: String, //약관 글이 전달됨.
        onSuccess: () -> Unit
    ) {
        val localTypography = LocalTypography.current
        val scrollState = rememberScrollState()


        //전달된 약관 글을 보여주고, 만약 크기를 넘어가게된다면 , 스크롤을 지원한다.
        Surface(
            modifier = modifier
                .fillMaxSize(),
            color = LocalColors.current.primaryGreen100
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {

                Text(
                    text = APP_AGREEMENT_TITLE,
                    style = localTypography.heading01
                )

                Card(
                    modifier = Modifier.wrapContentSize(),
                    contentColor = LocalColors.current.secondaryWhite100
                ) {
                    Text(
                        modifier = Modifier
                            .heightIn(512.dp)
                            .widthIn(312.dp)
                            .verticalScroll(scrollState),
                        text = agreementText,
                        style = localTypography.sub02
                    )
                }


                Button(
                    onClick = onSuccess,
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFFFFFFFF)
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "동의합니다",
                        Modifier
                            .padding(vertical = 10.dp, horizontal = 30.dp),
                        style = localTypography.heading02
                    )
                }

            }
        }
    }

    //약관에 대한 정보를 기재한다.
    @Composable
    fun FullTextAgreementScreen(
        agreementText: String
    ) {
        Box() {

        }
    }


    private const val APP_AGREEMENT_TITLE = "Pro_Pose 서비스 이용 약관"

}


@Preview
@Composable
fun TestPrepareServiceScreen() {

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
@Composable
fun PreviewInnerAppLoadingScreen() {
    PrepareServiceScreens.InnerAppLoadingScreen()
}

