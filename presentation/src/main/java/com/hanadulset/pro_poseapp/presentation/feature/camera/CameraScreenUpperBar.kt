package com.hanadulset.pro_poseapp.presentation.feature.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModuleExtension.ExpandableButton
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
        onSelectedViewRate: (Int) -> Unit
    ) {
        //변수 선언
        //구도 추천 여부
        val recommendCompState = remember { mutableStateOf(compStateInit) }
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val changeCompState by rememberUpdatedState(newValue = {
            recommendCompState.value = recommendCompState.value.not()
            onChangeCompSetEvent(recommendCompState.value)
        })


        //컴포저블 세팅
        Row(
            modifier = modifier
                .padding(10.dp)
                .fillMaxWidth()
                .height(50.dp),
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
                }
            )
            //확장 가능한 버튼이 확장 되지 않은 경우
            if (!isExpandedState.value) {
                //구도추천 On/OFF
                CameraModuleExtension.SwitchableButton(
                    innerText = "구도 추천",
                    init = recommendCompState.value,
                    positiveColor = Color(0xFF95FA99),
                    negativeColor = Color(0xFF999999),
                    onChangeState = changeCompState
                )


                //정보화면 이동
                CameraModules.NormalButton(
                    buttonName = "정보", iconDrawableId = R.drawable.settings, onClick = moveToInfo
                )

            }
        }


    }


}