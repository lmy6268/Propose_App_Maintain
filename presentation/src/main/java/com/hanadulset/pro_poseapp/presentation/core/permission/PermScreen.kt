package com.hanadulset.pro_poseapp.presentation.core.permission

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

object PermScreen {
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    //권한 설정화면
    fun PermScreen(
        multiplePermissionsState: MultiplePermissionsState,
        moveToCamera: () -> Unit
    ) {
        val needToCheck = remember { mutableStateOf(false) }
        //https://hanyeop.tistory.com/452 참고함.
        LaunchedEffect(needToCheck.value) {
            if (needToCheck.value) {
                if (multiplePermissionsState.allPermissionsGranted) moveToCamera()
                else needToCheck.value = false
            }
        }
        Screen(multiplePermissionsState = multiplePermissionsState, needToCheck = needToCheck)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Screen(
        multiplePermissionsState: MultiplePermissionsState,
        needToCheck: MutableState<Boolean>
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "애플리케이션을 이용하기 위해서는  권한을 허용하셔야 합니다.",
                    modifier = Modifier.fillMaxWidth(0.5F)
                )
                Button(onClick = {
                    if (multiplePermissionsState.allPermissionsGranted.not()) {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                        needToCheck.value = true
                    }
                }) {
                    Text(text = "권한 설정하기")
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
private fun PreviewScreen() {
    PermScreen.Screen(
        multiplePermissionsState =
        rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.CAMERA)),
        remember { mutableStateOf(false) }
    )
}