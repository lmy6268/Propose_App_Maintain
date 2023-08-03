package com.example.proposeapplication.presentation.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.presentation.uistate.CameraUiState
import com.example.proposeapplication.utils.camera.AutoFitSurfaceView
import com.example.proposeapplication.utils.camera.OrientationLiveData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

data class CameraScreen(private val mainViewModel: MainViewModel, private val context: Context) {

}

object CameraScr {

}


@Composable
//@Preview
fun Screen(
    mainViewModel: MainViewModel, context: Context
) {

    val systemUiController = rememberSystemUiController()
    val surfaceHolder = remember { mutableStateOf(SurfaceView(context)) }
    val rotateState = OrientationLiveData(context).observeAsState()
    val isFixed = remember {
        mutableStateOf(false)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isCaptured = remember {
        mutableStateOf(false)
    }
    val buttonImg = if (isPressed) R.drawable.ic_shutter_pressed
    else if (isFocused) R.drawable.ic_shutter_focused
    else R.drawable.ic_shutter_normal
    val fixedButtonImg =
        if (isFixed.value) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //미리보기 화면
            AndroidView(
                factory = {
                    AutoFitSurfaceView(it)
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                surfaceHolder.value = it
                it.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

                    override fun surfaceChanged(
                        holder: SurfaceHolder, format: Int, width: Int, height: Int
                    ) = Unit

                    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                        // Selects appropriate preview size and configures view finder
                        it.apply {
                            //적절한 미리보기 사이즈를 정해주는 곳 -> 한번 유심히 들여다 봐야할듯
                            mainViewModel.getPreviewSize(context, display).apply {
                                setAspectRatio(width, height)
                                (context as AppCompatActivity).lifecycleScope.launch {
                                    mainViewModel.showPreview(holder.surface)
                                }

                            }

                        }
                    }
                })
            }

            AndroidView(
                factory = {
                    ImageView(context)
                }, modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()

            ) { image ->
                if (isFixed.value) (context as AppCompatActivity).lifecycleScope.launch {
                    mainViewModel.fixedScreenUiState.collect() {
                        if (it is CameraUiState.Success) {
                            image.scaleType = ImageView.ScaleType.CENTER_CROP
                            image.setImageBitmap(it.data as Bitmap)
                            image.alpha = 0.4F
                        }
                    }
                }
                else image.setImageBitmap(null)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .heightIn(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(onClick = {}) {
                    Text(text = "화면비율")
                }
                Button(onClick = {}) {
                    Text(text = "줌")
                }
                Button(onClick = {}) {
                    Text(text = "설정")
                }
            }


            //하단 부분

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .heightIn(
                        if (systemUiController.isNavigationBarVisible) 200.dp else 50.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AndroidView(
                    modifier = Modifier
                        .widthIn(70.dp)
                        .heightIn(70.dp),
                    factory = {
                        ImageView(context).apply {
                            this.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    context, R.drawable.based_circle
                                )
                            )
                        }
                    }
                ) { image ->

                    (context as AppCompatActivity).lifecycleScope.launch {
                        mainViewModel.captureUiState.collect {
                            if (it is CameraUiState.Success) Glide.with(context)
                                .load(it.data as Bitmap).circleCrop().into(image)
                                .apply { isCaptured.value = false }
                        }
                    }
                }
                IconButton(
                    onClick = {
                        if (isCaptured.value.not()) {
                            mainViewModel.takePhoto(rotateState.value!!)
                            isCaptured.value = true
                        }
                    },
                    interactionSource = interactionSource
                ) {
                    Icon(
                        modifier = Modifier
                            .widthIn(100.dp)
                            .heightIn(100.dp),
                        painter = painterResource(id = buttonImg),
                        tint = Color.Unspecified,
                        contentDescription = "촬영버튼"
                    )
                }
                IconButton(
                    onClick = {
                        if (isFixed.value.not()) (context as AppCompatActivity).lifecycleScope.launch {
                            mainViewModel.getFixedScreen(surfaceHolder.value)
                            isFixed.value = true
                        } else isFixed.value = false
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .widthIn(70.dp)
                            .heightIn(70.dp),
                        painter = painterResource(id = fixedButtonImg),
                        tint = Color.Unspecified,
                        contentDescription = "고정버튼"
                    )
                }
            }


        }
    }
}