package com.hanadulset.pro_poseapp.presentation.core

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import kotlin.math.roundToInt

object CustomDialog {
    @Composable
    fun CustomAlertDialog(
        modifier: Modifier = Modifier,
        dialogTitle: String,
        subTitle: String,
        subTitleAdd: String = "",
        dismissText: String = "취소",
        confirmText: String = "확인",
        onDismissRequest: () -> Unit,
        onConfirmRequest: () -> Unit
    ) {
        val pretendardFamily = FontFamily(
            Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
            Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
        )
        val mainStyle = TextStyle(
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = pretendardFamily
        )
        val subStyle = TextStyle(
            lineHeight = 10.sp,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp,
            fontFamily = pretendardFamily
        )
        val buttonSize = DpSize(150.dp, 55.dp)


        Surface(
            modifier = modifier.wrapContentSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                Modifier.padding(20.dp, 30.dp, 20.dp, 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = dialogTitle, style = mainStyle
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = subTitle, style = subStyle
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (subTitleAdd != "") {
                    Text(
                        text = subTitleAdd, style = subStyle
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DialogButton(
                        buttonText = dismissText,
                        buttonSize = buttonSize,
                        backgroundColor = LocalColors.current.secondaryWhite100,
                        fontFamily = pretendardFamily,
                        fontWeight = FontWeight.Light,
                        onClick = onDismissRequest
                    )
                    DialogButton(
                        buttonText = confirmText,
                        buttonSize = buttonSize,
                        backgroundColor = LocalColors.current.primaryGreen100,
                        fontFamily = pretendardFamily,
                        fontWeight = FontWeight.Bold,
                        onClick = onConfirmRequest
                    )

                }
            }
        }
    }


    @Composable
    private fun DialogButton(
        modifier: Modifier = Modifier,
        buttonText: String,
        buttonSize: DpSize,
        backgroundColor: Color,
        fontWeight: FontWeight = FontWeight.Light,
        fontFamily: FontFamily,
        onClick: () -> Unit
    ) {
        Surface(
            Modifier.wrapContentSize(),
            shadowElevation = 2.dp,
            shape = ShapeDefaults.Medium
        ) {
            Box(
                modifier = modifier
                    .background(
                        shape = RoundedCornerShape(12.dp), color = backgroundColor
                    )
                    .size(buttonSize)
                    .clickable(onClick = onClick), contentAlignment = Alignment.Center
            ) {
                Text(
                    buttonText, style = TextStyle(
                        fontWeight = fontWeight, fontSize = 13.sp, fontFamily = fontFamily
                    ), textAlign = TextAlign.Center
                )
            }
        }

    }

    @Composable
    fun DownloadAlertDialog(
        isDownload: Boolean,
        totalSize: Long,
        modifier: Modifier = Modifier,
        onConfirmRequest: () -> Unit,
        onDismissRequest: () -> Unit
    ) {
        CustomAlertDialog(
            modifier = modifier,
            dialogTitle = "추가 리소스를 ${if (isDownload) "다운로드" else "업데이트"} 해야합니다.\n" + "진행 하시겠습니까? ( ${(totalSize / 1e+6).roundToInt()} MB )",
            subTitle = "모바일 네트워크 이용시, 데이터 요금이 부과될 수 있습니다.",
            dismissText = if (isDownload) "앱 종료" else "다음에 받기",
            confirmText = "확인",
            onDismissRequest = onDismissRequest,
            onConfirmRequest = onConfirmRequest
        )
    }

    @Composable
    fun InternetConnectionDialog(
        modifier: Modifier,
        onConfirmRequest: () -> Unit,
        onDismissRequest: () -> Unit
    ) {
        CustomAlertDialog(
            modifier = modifier,
            dialogTitle = "리소스 서버에 연결할 수 없습니다.",
            subTitle = "인터넷 설정을 확인해주세요.",
            subTitleAdd = "인터넷에 연결되어있는 경우, 다시 한번 시도해주세요.",
            dismissText = "설정하러 가기",
            confirmText = "다시 시도하기",
            onDismissRequest = onDismissRequest,
            onConfirmRequest = onConfirmRequest
        )
    }


    @Composable
    fun AppUpdateDialog(
        modifier: Modifier,
        noticeText: String,
        mustUpdate: Boolean,
        versionName:String,
        onConfirmRequest: () -> Unit,
        onDismissRequest: () -> Unit
    ) {
        CustomAlertDialog(
            modifier = modifier,
            dialogTitle = "앱 업데이트 안내",
            subTitle = "새로운 버전 ($versionName) 이 출시되었습니다.",
            subTitleAdd = noticeText,
            dismissText = if (mustUpdate) "종료" else "나중에",
            confirmText = "앱 업데이트",
            onDismissRequest = onDismissRequest,
            onConfirmRequest = onConfirmRequest
        )
    }


}

@Preview(showSystemUi = true)
@Composable
private fun TestDownloadAlert() {
    CustomDialog.DownloadAlertDialog(isDownload = false, totalSize = 100900200, onConfirmRequest = {
        Log.d("Hello", "clickConfirm")
    }, onDismissRequest = {
        Log.d("Hello", "clickStop")
    })
}
