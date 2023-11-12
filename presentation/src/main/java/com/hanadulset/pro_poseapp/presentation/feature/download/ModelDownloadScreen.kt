package com.hanadulset.pro_poseapp.presentation.feature.download

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material.Text
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.component.LocalTypography
import com.hanadulset.pro_poseapp.presentation.core.CustomDialog
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.DownloadState
import kotlin.math.roundToLong

object ModelDownloadScreen {

    /**
     *  _화면 흐름 방식_
     *
     * 1. _[ModelDownloadProgressScreen]_ : 다운로드 할 모델이 있는 경우, 모델을 다운로드 해야한다고 알려줌.
     *
     *      -> **이 때 용량이 어느 정도 인지 사용자에게 알려줘야 할 거 같음.**
     * 2. _[DownloadStateModal]_ : 사용자가 모델을 다운로드 하겠다는 경우, 다운로드를 진행하는 모달창을 만들고,
     *
     *      사용자가 다운로드를 취소한다면, 앱을 종료한다.
     *
     * */

    val pretendardFamily = FontFamily(
        Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
    )


    //모델 다운로드가 진행될 화면
    @Composable
    fun ModelDownloadRequestScreen(
        isCheck: CheckResponse?,
        moveToLoading: () -> Unit,
        moveToDownloadProgress: (Int) -> Unit,
        requestCheckDownload: () -> Unit = {}
    ) {
        val context = LocalContext.current
        val checkState by rememberUpdatedState(newValue = isCheck)
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalColors.current.primaryGreen100)
                .navigationBarsPadding(),
        ) {
            if (checkState!!.downloadType == CheckResponse.TYPE_MUST_DOWNLOAD
                || checkState!!.downloadType == CheckResponse.TYPE_ADDITIONAL_DOWNLOAD
            ) {
                CustomDialog.DownloadAlertDialog(
                    isDownload = checkState!!.downloadType == CheckResponse.TYPE_MUST_DOWNLOAD,
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.BottomCenter),
                    totalSize = checkState!!.totalSize,
                    onDismissRequest = {
                        if (checkState!!.downloadType == CheckResponse.TYPE_MUST_DOWNLOAD
                        ) (context as Activity).finish()
                        else moveToLoading()
                    },
                    onConfirmRequest = {
                        moveToDownloadProgress(checkState!!.downloadType)
                    }
                )
            } else if (checkState!!.needToDownload.not()) moveToLoading()
            //서버와 연결이 되지 않을 때
            else {
                CustomDialog.InternetConnectionDialog(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.BottomCenter),
                    //설정화면으로 연결
                    onConfirmRequest = requestCheckDownload,
                    onDismissRequest = {
                        Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                            launcher.launch(this)
                        }
                    }
                )
            }
        }


    }

//상태에 따라 화면을 변경하도록 하자.

    @Composable
    fun ModelDownloadProgressScreen(
        isDownload: Boolean?,
        downloadedInfo: DownloadState,
        onDismissEvent: (Context) -> Unit,
    ) {
        val context = LocalContext.current
        val onDownloading = if (isDownload == null || isDownload) "다운로드" else "업데이트"
        val stopDownload = if (isDownload == null || isDownload) "앱 종료" else "다음에 받기"
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val fontStyle = LocalTypography.current

        val currentBytesMB = downloadedInfo.currentBytes / 1e+6
        val totalBytesMB = downloadedInfo.totalBytes / 1e+6
        val progressRate = (currentBytesMB / totalBytesMB).toFloat()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalColors.current.primaryGreen100)
        ) {
            Surface(
                modifier = Modifier
                    .sizeIn(screenHeight / 3)
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "리소스 $onDownloading", style = fontStyle.heading01
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "리소스 $onDownloading 중... ( ${downloadedInfo.currentFileIndex + 1} / ${downloadedInfo.totalFileCnt})",
                        style = fontStyle.sub02
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    CustomLinearProgressBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        progress = progressRate,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End,
                        text = downloadedInfo.run {
                            val byteC = (currentBytes / 1e+6).roundToLong()
                            val byteT = (totalBytes / 1e+6).roundToLong()
                            "$byteC MB / $byteT MB"
                        }, style = fontStyle.sub02
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier.wrapContentSize(),
                        shape = ShapeDefaults.Medium,
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFFAFAF),
                                    shape = RoundedCornerShape(size = 12.dp)
                                )
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable(
                                    indication = null, //Ripple 효과 제거
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    }
                                ) {
                                    onDismissEvent(context)
                                }

                        ) {
                            Text(
                                text = stopDownload,
                                modifier = Modifier.align(Alignment.Center), style = TextStyle(
                                    lineHeight = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = pretendardFamily
                                )
                            )
                        }
                    }


                }

            }
        }

    }

    @Composable
    private fun CustomLinearProgressBar(
        modifier: Modifier = Modifier,
        progress: Float,
        backgroundColor: Color = Color(0xFFF0F0F0),
        color: Color = LocalColors.current.primaryGreen100,
    ) {
        val strokeWidth = LocalDensity.current.run { 15.dp.toPx() }

        Canvas(
            modifier
                .height(15.dp)
                .padding(horizontal = 5.dp)
                .progressSemantics(progress)
        ) {
            drawLinearIndicatorBackground(backgroundColor, strokeWidth)
            drawLinearIndicator(0f, progress, color, strokeWidth)
        }


    }

    private fun DrawScope.drawLinearIndicatorBackground(
        color: Color,
        strokeWidth: Float
    ) = drawLinearIndicator(0f, 1f, color, strokeWidth)

    private fun DrawScope.drawLinearIndicator(
        startFraction: Float,
        endFraction: Float,
        color: Color,
        strokeWidth: Float
    ) {
        val width = size.width
        val height = size.height
        // Start drawing from the vertical center of the stroke
        val yOffset = height / 2

        val isLtr = layoutDirection == LayoutDirection.Ltr
        val barStart = (if (isLtr) startFraction else 1f - endFraction) * width
        val barEnd = (if (isLtr) endFraction else 1f - startFraction) * width

        // Progress line
        drawLine(
            color, Offset(barStart, yOffset),
            Offset(barEnd, yOffset), strokeWidth,
            cap = StrokeCap.Round

        )
    }


}

@Preview(showSystemUi = true)
@Composable
private fun TestRequestModal() {

    ModelDownloadScreen.ModelDownloadProgressScreen(
        isDownload = true,
        DownloadState(
            currentBytes = 1000000,
            totalBytes = 100000000,
            currentFileIndex = 0,
            totalFileCnt = 2
        ),
        onDismissEvent = {

        }
    )


}

@Preview(showSystemUi = true)
@Composable
private fun TestDialog() {
    ModelDownloadScreen.ModelDownloadRequestScreen(
        isCheck = CheckResponse(),
        moveToLoading = { /*TODO*/ },
        moveToDownloadProgress = {}
    )
}
