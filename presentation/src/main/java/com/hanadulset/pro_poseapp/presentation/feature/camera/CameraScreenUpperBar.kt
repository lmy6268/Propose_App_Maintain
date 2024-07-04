package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.os.SystemClock
import androidx.camera.core.AspectRatio
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.feature.camera.component.CameraScreenButtons.ExpandableButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUpperBar.UpperBar
import com.hanadulset.pro_poseapp.presentation.feature.camera.component.CameraScreenButtons
import com.hanadulset.pro_poseapp.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.utils.model.common.ProPoseSize

object CameraScreenUpperBar {

    //화면비 버튼, 구도 추천 On/off, 정보 화면이 들어간다.
    @Composable
    fun UpperBar(
        modifier: Modifier = Modifier, //상단바의 전반적인 크기를 조절
        viewRateList: List<ViewRate>,
        moveToInfo: () -> Unit,
        onSelectedViewRate: (Int) -> Unit,
        needToCloseViewRateList: () -> Boolean = { false },
    ) {
        //변수 선언
        //구도 추천 여부
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val buttonSize = 36.dp
        val beforeTime = rememberSaveable(moveToInfo) {
            mutableLongStateOf(0L)
        }
        //컴포저블 세팅
        Row(
            modifier = modifier.padding(horizontal = buttonSize),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //오버레이 되는 확장 가능한 버튼
            ExpandableButton(
                itemList = viewRateList.map { it.name },
                type = "화면 비",
                modifier = Modifier,
                onSelectedItemEvent = onSelectedViewRate,
                isExpanded = {
                    isExpandedState.value = it
                },
                defaultButtonSize = buttonSize,
                triggerClose = needToCloseViewRateList
            )

            //확장 가능한 버튼이 확장 되지 않은 경우
            if (!isExpandedState.value) {

                val INTERVAL = 400L
                //정보화면 이동
                CameraScreenButtons.NormalButton(
                    modifier = Modifier.border(
                        BorderStroke(
                            2.dp,
                            LocalColors.current.subPrimaryBlack100
                        ),
                        shape = CircleShape
                    ),
                    buttonName = "정보",
                    onClick = {
                        val clickedTime = SystemClock.elapsedRealtime()
                        if ((clickedTime - beforeTime.longValue) >= INTERVAL) {
                            moveToInfo()
                            beforeTime.longValue = clickedTime
                        }
                    },
                    colorTint = LocalColors.current.secondaryWhite100,
                    buttonSize = buttonSize,
                    buttonText = "i",
                    buttonTextColor = LocalColors.current.subPrimaryBlack100,
                    buttonTextSize = (buttonSize.value.toInt() / 2)
                )

            }
        }


    }


}

@Composable
@Preview
fun PreviewUpperBar() {
    UpperBar(
        viewRateList = listOf(
            ViewRate(
                name = "3:4",
                aspectRatioType = AspectRatio.RATIO_4_3,
                aspectRatioSize = ProPoseSize(3, 4)
            ), ViewRate(
                "9:16",
                aspectRatioType = AspectRatio.RATIO_16_9,
                aspectRatioSize = ProPoseSize(9, 16)
            )
        ),
        onSelectedViewRate = {

        },
        moveToInfo = {

        }

    )

}