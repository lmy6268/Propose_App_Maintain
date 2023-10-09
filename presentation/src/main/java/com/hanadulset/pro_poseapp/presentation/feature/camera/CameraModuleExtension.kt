package com.hanadulset.pro_poseapp.presentation.feature.camera

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.presentation.R

object CameraModuleExtension {

    @Composable
    fun SwitchableButton(
        onOffState: Boolean,
        innerText: String,
        modifier: Modifier = Modifier
    ) {

    }




    @Composable
    fun FixedButton(
        fixedButtonPressedEvent: () -> Unit
    ) {
        val isFixedBtnPressed = remember {
            mutableStateOf(false)
        }
        val fixedBtnImage = if (isFixedBtnPressed.value) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed

        Box(Modifier.clickable(
            indication = CustomIndication, // Remove ripple effect
            interactionSource = MutableInteractionSource()
        ) {
            isFixedBtnPressed.value = !isFixedBtnPressed.value
            fixedButtonPressedEvent()
        }) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = fixedBtnImage),
                tint = Color.Unspecified,
                contentDescription = "고정버튼"
            )
        }
    }

    @Composable
    fun ShutterButton(
        onClickEvent: () -> Unit,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        //버튼 이미지 배치
        val buttonImg = if (isPressed) R.drawable.ic_shutter_pressed
        else R.drawable.ic_shutter_normal

        Box(modifier = Modifier.clickable(
            indication = null, //Ripple 효과 제거
            interactionSource = interactionSource
        ) {
            onClickEvent()
        }) {
            Icon(
                modifier = Modifier.size(80.dp),
                tint = Color.Unspecified,
                painter = painterResource(id = buttonImg),
                contentDescription = "촬영버튼"
            )
        }

    }

    object CustomIndication : Indication {
        private class DefaultDebugIndicationInstance(
            private val isPressed: State<Boolean>,
        ) : IndicationInstance {
            override fun ContentDrawScope.drawIndication() {
                drawContent()
                if (isPressed.value) {
                    drawCircle(color = Color.Gray.copy(alpha = 0.1f))
                }
            }
        }

        @Composable
        override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
            val isPressed = interactionSource.collectIsPressedAsState()
            return remember(interactionSource) {
                DefaultDebugIndicationInstance(isPressed)
            }
        }
    }


}