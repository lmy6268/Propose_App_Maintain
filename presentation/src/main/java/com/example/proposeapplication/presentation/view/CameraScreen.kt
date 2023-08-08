package com.example.proposeapplication.presentation.view

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.example.proposeapplication.presentation.CustomFont
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.presentation.uistate.CameraUiState
import com.example.proposeapplication.utils.camera.AutoFitSurfaceView
import com.example.proposeapplication.utils.camera.OrientationLiveData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

object CameraScreen {
    private val defaultFont = CustomFont.font_pretendard

    @Composable
    fun Menu(selectedState: MutableIntState) {
        val menuList = listOf<String>("포즈", "포즈&구도", "구도")
        val selectedIdx = remember {
            mutableIntStateOf(0)
        }
        Box(
            Modifier
                .widthIn(150.dp)
                .heightIn(30.dp)
        ) {

            Box(
                Modifier
                    .background(
                        color = Color(0x80FAFAFA), shape = RoundedCornerShape(18.dp)
                    )
                    .heightIn(30.dp)
                    .widthIn(220.dp)
                    .align(alignment = Alignment.CenterStart)
            ) {}
            Row(
                modifier = Modifier.align(alignment = Alignment.CenterStart),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in menuList.indices) {
                    Box(modifier = Modifier
                        .heightIn(50.dp)
                        .widthIn(20.dp)
                        .background(
                            color = if (i == selectedIdx.intValue) Color.Black else Color(
                                0x00FFFFFF
                            ), shape = RoundedCornerShape(30.dp)
                        )
                        .clickable(indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            selectedIdx.intValue = i

                        }

                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            text = menuList[i],
                            fontSize = 14.sp,
                            fontFamily = defaultFont,
                            fontWeight = FontWeight.Bold,
                            color = if (i == selectedIdx.intValue) Color.White else Color(0xFF000000),
                        )
                    }
                }
            }
        }


    }


    @Composable
    private fun rememberLifecycleEvent(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current): Lifecycle.Event {
        var state by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                state = event
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        return state
    }


    @Composable
//@Preview
    fun Screen(
        navController: NavHostController, mainViewModel: MainViewModel
    ) {
        val context = LocalContext.current
        val lifecycleEvent = rememberLifecycleEvent()
        val systemUiController = rememberSystemUiController()
        val surfaceHolder = remember { mutableStateOf<SurfaceView?>(null) }
        val rotateState = OrientationLiveData(context).observeAsState() //회전 상태를 파악하는 상태 변수

        //State Values
        //고정 여부
        val isFixed = remember {
            mutableStateOf(false)
        }
        val viewRateList = listOf(Size(1, 1), Size(5, 4), Size(4, 3), Size(16, 9))
        val viewSelectedState = remember { mutableIntStateOf(3) }
        val selectedIdx = remember {
            mutableIntStateOf(1)
        }

        //현재 줌 레벨을 가지고 있는 상태 변수
        val zoomState = remember {
            mutableIntStateOf(1)
        }

        //화면에 사용자의 입력이 들어간 경우 기존에 확장된 창들을 닫음
        val interruptState = remember {
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

        val fixedButtonImg = if (isFixed.value) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed
        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                mainViewModel.getLatestImage()
            }
//        val galleryLauncher =
//            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
//                launcher.launch(Intent(Intent.ACTION_VIEW).apply {
//                    setDataAndType(it, "image/*")
////                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                })
//            }


        //생명주기를 인식 -> 만약 다시 활동상태가 재개 된다면, 카메라 객체를 다시 초기화함.
        LaunchedEffect(lifecycleEvent) {
            if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
                if (surfaceHolder.value is AutoFitSurfaceView) mainViewModel.showPreview(
                    surfaceHolder.value!!.holder.surface
                )
                mainViewModel.getLatestImage()
            }


        }

        //화면 진입점
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
                    }, modifier = Modifier.fillMaxSize()
//                        .aspectRatio(3F / 4F),
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
//                                        calculateSize(width, height, viewRate.value).apply {
//                                            setAspectRatio(width, height)
//                                        }
                                    setAspectRatio(width, height)
                                }

                            }
                        }
                    })
                }


                //고정된 화면 보여주는 곳
                AndroidView(
                    factory = {
                        ImageView(context)
                    }, modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
//                        .aspectRatio(3F / 4F),

                ) { image ->
                    if (isFixed.value) (context as AppCompatActivity).lifecycleScope.launch {
                        mainViewModel.fixedScreenUiState.collect {
                            if (it is CameraUiState.Success) {
                                image.scaleType = ImageView.ScaleType.CENTER_CROP
                                image.setImageBitmap(it.data as Bitmap)
                                image.alpha = 0.4F
                            }
                        }
                    }
                    else image.setImageBitmap(null)
                }
//                }

                //상단 부분
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(100.dp)
                        .padding(top = 30.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ExpandableButton(
                        text = viewRateList.map { it -> "${it.height}:${it.width}" },
                        type = "비율",
                        viewSelectedState
                    )
                    Menu(selectedIdx)
                    IconButton(onClick = { }) {
                        Icon(
                            modifier = Modifier
                                .widthIn(44.dp)
                                .heightIn(44.dp),
                            painter = painterResource(id = R.drawable.based_circle),
                            tint = Color.Unspecified,
                            contentDescription = "설정"
                        )
                        Icon(
                            painter = painterResource(
                                id = R.drawable.settings
                            ), contentDescription = "설정 아이콘"
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                ) {


//                    MenuModule(text = "hello", isSelected = true)
                    ExpandableButton(
                        text = listOf("0.5X", "1X", "2X"),
                        type = "줌",
                        selectedState = zoomState,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    //하단 부분
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                if (systemUiController.isNavigationBarVisible) 200.dp else 50.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //촬영된 이미지 보기
                        AndroidView(modifier = Modifier
                            .widthIn(70.dp)
                            .heightIn(70.dp)
                            .clickable {
                                openGallery(launcher)
                            }, factory = {
                            ImageView(context).apply {
                                this.setImageDrawable(
                                    AppCompatResources.getDrawable(
                                        context, R.drawable.based_circle
                                    )
                                )
                            }
                        }) { image ->
                            //이미지가 촬영되면, 촬영된 이미지를 뷰에 띄워준다
                            (context as AppCompatActivity).lifecycleScope.launch {
                                var beforeData: Bitmap? = null
                                mainViewModel.latestImgUiState.collect {
                                    if (it is CameraUiState.Success) {
                                        val data = it.data as Bitmap
                                        if (data != beforeData) {
                                            Glide.with(context).load(it.data).circleCrop()
                                                .into(image).apply { isCaptured.value = false }
                                            beforeData = it.data
                                        }
                                    } else if (it is CameraUiState.Error) {
                                        Log.d("CameraScreen: ", it.exception.message!!)
                                        image.setImageResource(R.drawable.based_circle)
                                    }
                                }
                            }
                        }
                        Box(Modifier.clickable(
                            indication = null,
                            interactionSource = interactionSource,
                        ) {
                            if (isCaptured.value.not()) {
                                mainViewModel.takePhoto(rotateState.value!!)
                                isCaptured.value = true
                            }
                        }) {
                            Icon(
                                modifier = Modifier
                                    .widthIn(100.dp)
                                    .heightIn(100.dp),
                                painter = painterResource(id = buttonImg),
                                tint = Color.Unspecified,
                                contentDescription = "촬영버튼"
                            )
                        }
                        IconButton(onClick = {
                            if (isFixed.value.not()) (context as AppCompatActivity).lifecycleScope.launch {
                                surfaceHolder.value?.let { mainViewModel.getFixedScreen(it) }
                                isFixed.value = true
                            } else isFixed.value = false
                        }) {
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
    }


    //메뉴별 아이콘
    @Composable
    private fun MenuModule(text: String, isSelected: Boolean) {
        Box(
            modifier = Modifier
                .widthIn(96.dp)
                .heightIn(36.dp)
                .background(
                    color = Color(
                        if (isSelected) 0xFF212121
                        else 0x00000000 //투명하게
                    ), shape = RoundedCornerShape(size = 18.dp)
                )
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center),
                color = Color(
                    if (isSelected) 0xFFFFFFFF
                    else 0xFF000000 //검은색
                )
            )
        }

    }

    //확장가능한 버튼
    @Composable
    private fun ExpandableButton(
        text: List<String>,
        type: String,
        selectedState: MutableIntState?,
        modifier: Modifier = Modifier
    ) {
        val isClicked = remember {
            mutableStateOf(false)
        }
        Box(modifier = modifier
            .apply {
                if (isClicked.value) fillMaxWidth()
                else widthIn(44.dp)

            }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200, easing = LinearEasing
                )
            ) //크기 변경이 감지되면 애니메이션을 추가해준다.
            .heightIn(44.dp)

        ) {

            if (isClicked.value) Row(Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1F))
                Box(
                    Modifier.background(
                        color = Color(0x80FAFAFA), shape = RoundedCornerShape(30.dp)
                    )

                ) {
                    //닫는 버튼
                    IconButton(
                        modifier = Modifier.heightIn(30.dp),
                        onClick = { isClicked.value = false }) {
                        Icon(
                            painterResource(id = R.drawable.based_circle),
                            tint = Color.Unspecified,
                            contentDescription = "background",

                            )
                        Icon(
                            painterResource(id = R.drawable.close),
                            contentDescription = "close",
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth(0.9F)
                            .align(Alignment.Center)
                            .padding(start = 40.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        for (i in text.indices) {
                            Text(
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() })
                                {
                                    selectedState!!.intValue = i
                                },
                                text = text[i],
                                fontSize = 12.sp,
                                fontWeight =
                                if (i == selectedState!!.intValue) FontWeight.Bold else FontWeight.Light,
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1F))
            }
            else IconButton(onClick = {
                isClicked.value = true
            }) {
                Icon(
                    modifier = Modifier
                        .widthIn(44.dp)
                        .heightIn(44.dp),
                    painter = painterResource(id = R.drawable.based_circle),
                    tint = Color.Unspecified,
                    contentDescription = type
                )
                if (selectedState != null) {
                    Text(
                        text = text[selectedState.intValue], //화면 비 글씨 표기
                        fontWeight = FontWeight(FontWeight.Bold.weight), fontSize = 12.sp
                    )
                }
            }

        }
    }
}


@Preview
@Composable
fun testMenu() {
    CameraScreen.Menu(selectedState = remember {
        mutableIntStateOf(0)
    })
}


private fun calculateSize(width: Int, height: Int, aspectRatio: Size): Size {
    fun gcd(a: Int, b: Int): Int = if (b != 0) gcd(b, a % b) else a
    val std = if (width > height) gcd(width, height) else gcd(height, width)
    //만약에 3:4라고 한다면 ,
    return Size(std * aspectRatio.width, std * aspectRatio.height)

}

private fun openGallery(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    launcher.launch(Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
    })
}


