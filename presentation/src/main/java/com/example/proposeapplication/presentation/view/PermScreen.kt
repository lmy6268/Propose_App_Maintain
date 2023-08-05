package com.example.proposeapplication.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.proposeapplication.presentation.MainActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

object PermScreen {
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    //권한 설정화면
    fun Screen(
        navController: NavHostController,
        multiplePermissionsState: MultiplePermissionsState
    ) {
        val needToCheck = remember { mutableStateOf(false) }
        //https://hanyeop.tistory.com/452 참고함.
        LaunchedEffect(Unit) {
            if (needToCheck.value) {
                if (multiplePermissionsState.allPermissionsGranted) navController.navigate(
                    MainActivity.page.Cam.name) {
                    popUpTo(MainActivity.page.Perm.name) { inclusive = true }
                }
                else needToCheck.value = false
            }
        }

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