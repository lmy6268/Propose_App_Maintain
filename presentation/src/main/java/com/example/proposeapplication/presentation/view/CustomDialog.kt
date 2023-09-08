package com.example.proposeapplication.presentation.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.proposeapplication.presentation.R
import kotlin.math.roundToInt


object CustomDialog {
    @Composable
    fun CustomAlertDialog(
        modifier: Modifier = Modifier,
        onDismissRequest: () -> Unit,
        properties: DialogProperties = DialogProperties(),
        content: @Composable () -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                content()
            }

        }
    }

    @Composable
    fun DownloadAlertDialog(
        totalSize: Long,
        onConfirmRequest: () -> Unit,
        onDismissRequest: () -> Unit
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

        CustomAlertDialog(
            modifier = Modifier
                .width(320.dp)
                .height(200.dp),
            onDismissRequest = onDismissRequest,
        ) {
            Column(Modifier.padding(20.dp, 30.dp, 20.dp, 20.dp)) {
                Text(
                    text = "추가 리소스를 다운로드 해야합니다.\n" +
                            "진행 하시겠습니까? ( ${(totalSize / 1e+6).roundToInt()} MB )",
                    style = mainStyle
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "모바일 네트워크 이용시, 데이터 요금이 부과될 수 있습니다.",
                    style = subStyle
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFAFAFA)
                            )
                            .width(130.dp)
                            .height(50.dp)
                            .clickable(
                                indication = null, //Ripple 효과 제거
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }
                            ) {
                                onDismissRequest()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "앱 종료",
                            style = TextStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = 13.sp,
                                fontFamily = pretendardFamily
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF95FFA7)
                            )
                            .width(130.dp)
                            .height(50.dp)
                            .clickable {
                                onConfirmRequest()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "확인",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = pretendardFamily
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }


            }


        }
    }
}

@Preview(widthDp = 320, heightDp = 300)
@Composable
private fun testDownloadAlert() {
    CustomDialog.DownloadAlertDialog(
        totalSize = 100900200,
        onConfirmRequest = {
            Log.d("Hello", "clickConfirm")
        },
        onDismissRequest = {
            Log.d("Hello", "clickStop")
        }
    )
}