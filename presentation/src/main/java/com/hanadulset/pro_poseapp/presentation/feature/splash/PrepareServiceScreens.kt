package com.hanadulset.pro_poseapp.presentation.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceScreens.SplashScreen
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import kotlinx.coroutines.flow.asStateFlow

object PrepareServiceScreens {
    private const val APP_NAME = "Pro_Pose"

    @Composable
    fun SplashScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {

            Image(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center),
                painter = painterResource(id = R.drawable.app_icon_rounded),
                contentDescription = "앱 아이콘",
            )

            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 100.dp),
                text = APP_NAME,
                style =
//                MaterialTheme.typography.h1
                TextStyle(
                    fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
            )
        }
    }

    @Composable
    fun AppLoadingScreen(
        previewState: CameraState,
        prepareServiceViewModel: PrepareServiceViewModel,
        onAfterLoadedEvent: () -> Unit
    ) {

        val totalLoadedState by prepareServiceViewModel.totalLoadedState.collectAsState()

        LaunchedEffect(Unit) {
            prepareServiceViewModel.preLoadMethods()
        }
        if (totalLoadedState && previewState.cameraStateId == CameraState.CAMERA_INIT_COMPLETE) onAfterLoadedEvent()
        else InnerAppLoadingScreen()


    }

    //로딩 중일 때 보여주는 화면
    @Composable
    fun InnerAppLoadingScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {

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
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 20.dp)
                )

                Text(
                    text = "앱 로딩 중...", style =
//                MaterialTheme.typography.h1
                    TextStyle(
                        fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                )
            }

        }


    }


}


@Preview(widthDp = 360, heightDp = 800)
@Composable
fun PreViewSplash() {
    SplashScreen()
}

@Preview
@Composable
fun PreviewInnerAppLoadingScreen() {
    PrepareServiceScreens.InnerAppLoadingScreen()
}

