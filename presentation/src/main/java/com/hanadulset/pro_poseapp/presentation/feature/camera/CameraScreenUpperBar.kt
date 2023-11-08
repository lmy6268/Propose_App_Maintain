package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.ExpandableButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUpperBar.UpperBar
import com.hanadulset.pro_poseapp.utils.camera.ViewRate

object CameraScreenUpperBar {

    //화면비 버튼, 구도 추천 On/off, 정보 화면이 들어간다.
    @Composable
    fun UpperBar(
        modifier: Modifier = Modifier, //상단바의 전반적인 크기를 조절
        compStateInit: Boolean,
        viewRateList: List<ViewRate>,
        onChangeCompSetEvent: (Boolean) -> Unit,
        moveToInfo: () -> Unit,
        onSelectedViewRate: (Int) -> Unit,
        needToCloseViewRateList: Boolean = false,
    ) {
        //변수 선언
        //구도 추천 여부
        val recommendCompState = rememberSaveable { mutableStateOf(compStateInit) }
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val changeCompState by rememberUpdatedState(newValue = {
            recommendCompState.value = recommendCompState.value.not()
            onChangeCompSetEvent(recommendCompState.value)
        })
        val triggerCloseValue by rememberUpdatedState(newValue = needToCloseViewRateList)


        //컴포저블 세팅
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
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
                defaultButtonSize = 44.dp,
                triggerClose = triggerCloseValue
            )

            //확장 가능한 버튼이 확장 되지 않은 경우
            if (!isExpandedState.value) {
                //구도추천 On/OFF
                CameraScreenButtons.SwitchableButton(
                    innerText = "구도 추천",
                    init = recommendCompState.value,
                    positiveColor = LocalColors.current.primaryGreen100,
                    negativeColor = Color.Black,
                    onChangeState = {
                        changeCompState()
                    }
                )


                //정보화면 이동
                CameraScreenButtons.NormalButton(
                    modifier = Modifier.border(
                        BorderStroke(
                            2.dp,
                            LocalColors.current.subPrimaryBlack100
                        ),
                        shape = CircleShape
                    ),
                    innerIconDrawableId = R.drawable.settings,
                    buttonName = "정보",
                    onClick = moveToInfo,
                    colorTint = LocalColors.current.secondaryWhite100,
                    buttonSize = 44.dp,
                    innerIconColorTint = LocalColors.current.subPrimaryBlack100,
                    innerIconDrawableSize = 24.dp
                )

            }
        }


    }


}

@Composable
@Preview
fun PreviewUpperBar() {
    UpperBar(
        compStateInit = false,
        viewRateList = listOf(
            ViewRate(
                name = "3:4",
                aspectRatioType = AspectRatio.RATIO_4_3,
                aspectRatioSize = Size(3, 4)
            ), ViewRate(
                "9:16",
                aspectRatioType = AspectRatio.RATIO_16_9,
                aspectRatioSize = Size(9, 16)
            )
        ),
        onChangeCompSetEvent = {

        },
        onSelectedViewRate = {

        },
        moveToInfo = {

        }

    )

}