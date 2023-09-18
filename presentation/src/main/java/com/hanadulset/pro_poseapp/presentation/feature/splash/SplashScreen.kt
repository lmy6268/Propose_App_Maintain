package com.hanadulset.pro_poseapp.presentation.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.splash.SplashScreen.Splash

object SplashScreen {

    @Composable
    fun Screen(
        splashViewModel: SplashViewModel = hiltViewModel(),
        onAfterLoadAllData: () -> Unit
    ) {

        splashViewModel.preLoadModel()


        val modelLoadedState by splashViewModel.modelLoadedState.collectAsState()
        LaunchedEffect(modelLoadedState){
            if(modelLoadedState) onAfterLoadAllData()
        }
        if (modelLoadedState.not()) Splash()
    }

    @Composable
    fun Splash() {
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
                text = APP_NAME, style =
//                MaterialTheme.typography.h1
                TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )
        }


    }

    private const val APP_NAME = "Pro_Pose"

}

@Preview(widthDp = 360, heightDp = 800)
@Composable
fun PreViewSplash() {
    Splash()
}

