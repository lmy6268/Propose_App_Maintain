package com.example.proposeapplication.presentation.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.R

object CameraScreen {

    @Composable
    fun Menu(selectedIdx: MutableIntState, modifier: Modifier = Modifier) {
        val cameraModeList = stringArrayResource(id = R.array.camera_modes)

        Box(
            modifier = modifier
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
                for (i in cameraModeList.indices) {
                    Box(modifier = Modifier
                        .heightIn(50.dp)
                        .widthIn(20.dp)
                        .background(
                            color = if (i == selectedIdx.intValue) MaterialTheme.colors.primary else Color(
                                0x00FFFFFF
                            ), shape = RoundedCornerShape(30.dp)
                        )
                        .clickable(indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            selectedIdx.intValue = i
                            Log.d("현재 모드 : ", cameraModeList[selectedIdx.intValue])
                        }

                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            text = cameraModeList[i],
                            style = MaterialTheme.typography.h1,
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
    fun Screen(
        navController: NavHostController, mainViewModel: MainViewModel
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val previewView = PreviewView(context)
        val isUpdated = remember {
            mutableStateOf(false)
        }
        val isPressedFixedBtn = remember {
            mutableStateOf(false)
        }
        val capturedEdgesBitmap: Bitmap by mainViewModel.edgeDetectBitmapState.collectAsState() //업데이트된 고정화면을 가지고 있는 변수
        val capturedThumbnailBitmap: Bitmap by mainViewModel.capturedBitmapState.collectAsState() //업데이트된 캡쳐화면을 가지고 있는 변수
        val viewRateList = stringArrayResource(id = R.array.view_rates)
        val poseRecString: String by mainViewModel.poseResultState.collectAsState()
        if (poseRecString != "") Log.d(this.javaClass::class.simpleName, "Hog 결과: $poseRecString")
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .wrapContentSize()
                    .aspectRatio(3 / 4F)
                    .align(Alignment.Center)
                    .offset(y = (-80).dp)

            ) {
                //뷰를 계속 업데이트 하면서 생겼던 오류
                if (isUpdated.value.not()) {
                    mainViewModel.showPreview(
                        lifecycleOwner,
                        previewView.surfaceProvider,
                        AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                    )
                    it.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    isUpdated.value = true
                }

            }
            Image(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = (-80).dp)
                    .align(Alignment.Center)
                    .alpha(
                        if (isPressedFixedBtn.value) 0.5F
                        else 0F
                    ),
                bitmap = capturedEdgesBitmap.asImageBitmap(), contentDescription = "엣지화면"
            )
            UpperButtons(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .heightIn(100.dp)
                    .fillMaxWidth(),
                navController = navController
            )
            LowerButtons(
                capturedThumbnailBitmap = capturedThumbnailBitmap,
                mainViewModel = mainViewModel,
                isPressedFixedBtn = isPressedFixedBtn,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )

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
        isExpandedState: MutableState<Boolean>,
        selectedState: MutableIntState?,
        modifier: Modifier = Modifier
    ) {
        Box(modifier = modifier
            .apply {
                if (isExpandedState.value) fillMaxWidth()
                else widthIn(44.dp)
            }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200, easing = LinearEasing
                )
            ) //크기 변경이 감지되면 애니메이션을 추가해준다.
//            .heightIn(44.dp)

        ) {

            if (isExpandedState.value) Row(Modifier.fillMaxWidth()) {
//                Spacer(modifier = Modifier.weight(1F))
                Box(
                    Modifier.background(
                        color = Color(0x80FAFAFA), shape = RoundedCornerShape(30.dp)
                    )

                ) {
                    //닫는 버튼
                    IconButton(modifier = Modifier.heightIn(30.dp),
                        onClick = { isExpandedState.value = false }) {
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
                            .padding(horizontal = 40.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        for (i in text.indices) {
                            Text(modifier = Modifier.clickable(indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                selectedState!!.intValue = i
                            },
                                text = text[i],
                                fontSize = 12.sp,
                                fontWeight = if (i == selectedState!!.intValue) FontWeight.Bold else FontWeight.Light,
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }
            }
            else IconButton(onClick = {
                isExpandedState.value = true
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


    //상단 부분
    @Composable
    fun UpperButtons(
        modifier: Modifier = Modifier, navController: NavHostController
    ) {
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val viewRateList = stringArrayResource(id = R.array.view_rates)
        val selectedViewRateIdxState = remember {
            mutableIntStateOf(0)
        }

        val selectedModeIdxState = remember {
            mutableIntStateOf(0)
        }

        Row(
            modifier = modifier
                .padding(20.dp)
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            //오버레이 되는 확장 가능한 버튼
            ExpandableButton(
                text = viewRateList.toList(),
                type = "비율",
                isExpandedState,
                selectedViewRateIdxState,
            )
            if (isExpandedState.value.not()) {
                Menu(
                    selectedModeIdxState,
                )
                IconButton(
                    onClick = {


                    }) {
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
        }


    }

    @Composable
    fun LowerButtons(
        modifier: Modifier = Modifier,
        isPressedFixedBtn: MutableState<Boolean>,
        capturedThumbnailBitmap: Bitmap, //캡쳐된 이미지의 썸네일을 받아옴.
        mainViewModel: MainViewModel
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        val isCaptured = remember {
            mutableStateOf(false)
        }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {

        }

        //버튼 이미지 배치
        val buttonImg = if (isPressed) R.drawable.ic_shutter_pressed
        else if (isFocused) R.drawable.ic_shutter_focused
        else R.drawable.ic_shutter_normal
        val fixedButtonImg = if (isPressedFixedBtn.value) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed

        val testImage = LocalContext.current.assets.open("test.png").use {
            BitmapFactory.decodeStream(it)
        }

        Column(
            modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally

        ) {
            //첫번째 단
            Row(horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally)) {
                IconButton(modifier = Modifier.heightIn(30.dp),
                    onClick = {
//                        mainViewModel.reqPoseRecommend()
                        mainViewModel.testPose(testImage)
                    }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(0x80000000),
                        contentDescription = "background",

                        )
                    Text(
                        text = "포즈 추천"
                    )
                }
                IconButton(modifier = Modifier.heightIn(30.dp),
                    onClick = { }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(0x80000000),
                        contentDescription = "background",

                        )
                    Text(
                        text = "구도 추천"
                    )
                }
            }
            //두번쨰 단
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //캡쳐 썸네일 이미지 뷰 -> 원형으로 표사
                Image(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .paint(
                            painter = painterResource(R.drawable.based_circle),
                            contentScale = ContentScale.FillBounds
                        )
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null
                        ) {
                            openGallery(launcher)
                        },
                    contentScale = ContentScale.Crop,
                    bitmap = capturedThumbnailBitmap.asImageBitmap(), contentDescription = "캡쳐된 이미지"
                )
                //캡쳐 버튼
                Box(
                    modifier = Modifier.clickable(
                        indication = null, //Ripple 효과 제거
                        interactionSource = interactionSource
                    ) {
                        mainViewModel.getPhoto()
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .size(100.dp),
                        painter = painterResource(id = buttonImg),
                        tint = Color.Unspecified,
                        contentDescription = "촬영버튼"
                    )
                }
                //고정 버튼
                Box(
                    Modifier.clickable(
                        indication = null, //Ripple 효과 제거
                        interactionSource = MutableInteractionSource()
                    ) {
                        if (!isPressedFixedBtn.value) {
                            mainViewModel.reqFixedScreen()
                            isPressedFixedBtn.value = true
                        } else isPressedFixedBtn.value = false
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .size(80.dp),
                        painter = painterResource(id = fixedButtonImg),
                        tint = Color.Unspecified,
                        contentDescription = "고정버튼"
                    )
                }

            }
        }

    }
}

@Preview
@Composable
fun testMenu() {
    CameraScreen.Menu(selectedIdx = remember {
        mutableIntStateOf(0)
    })
}

private fun openGallery(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    //실 기기에서는 안됨..
    launcher.launch(Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
    })
}


