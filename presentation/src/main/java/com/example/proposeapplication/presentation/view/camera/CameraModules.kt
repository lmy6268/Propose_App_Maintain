package com.example.proposeapplication.presentation.view.camera

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.presentation.view.camera.CameraModules.CompositionArrow
import com.example.proposeapplication.presentation.view.camera.CameraModules.ExpandableButton
import com.example.proposeapplication.presentation.view.camera.CameraModules.LowerButtons
import com.example.proposeapplication.presentation.view.camera.CameraModules.MenuModule
import kotlinx.coroutines.delay


object CameraModules {
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

    //확장가능한 버튼
    @Composable
    fun ExpandableButton(
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
        modifier: Modifier = Modifier,
        navController: NavHostController,
        selectedViewRateIdxState: MutableIntState
    ) {
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val viewRateList = stringArrayResource(id = R.array.view_rates)

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
//                        navController.navigate()

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

        //현재 줌 상태
        val zoomState = remember {
            mutableIntStateOf(0)
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
                    onClick = {
                        mainViewModel.reqCompRecommend()
                    }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(0x80000000),
                        contentDescription = "background",

                        )
                    Text(
                        text = "구도 추천"
                    )
                }
                listOf("1", "2").forEachIndexed { index, str ->
                    IconButton(
                        onClick = {
                            zoomState.intValue = index
                            mainViewModel.setZoomLevel(str.toFloat())
                        },
                    ) {
                        Icon(
                            modifier = Modifier.apply {
                                if (index == zoomState.intValue) sizeIn(50.dp)
                                else sizeIn(10.dp)
                            },
                            painter = painterResource(id = R.drawable.based_circle),
                            tint = if (index == zoomState.intValue) Color(0xFF000000) else Color.Unspecified,
                            contentDescription = "줌버튼"
                        )
                        Text(
                            text = if (index == zoomState.intValue) "${str}X" else str,
                            style = MaterialTheme.typography.h1,
                            fontWeight = if (index == zoomState.intValue) FontWeight.Bold else FontWeight.Light,
                            color = if (index == zoomState.intValue) Color(0xFFFFFFFF) else Color(
                                0xFF000000
                            ),
                        )
                    }

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
//                Box(
//                    Modifier.clickable(
//                        indication = null, //Ripple 효과 제거
//                        interactionSource = MutableInteractionSource()
//                    ) {
//                        if (!isPressedFixedBtn.value) {
//                            mainViewModel.reqFixedScreen()
//                            isPressedFixedBtn.value = true
//                        } else isPressedFixedBtn.value = false
//                    }
//                ) {
//                    Icon(
//                        modifier = Modifier
//                            .size(80.dp),
//                        painter = painterResource(id = fixedButtonImg),
//                        tint = Color.Unspecified,
//                        contentDescription = "고정버튼"
//                    )
//                }
                Box(
                    Modifier.clickable(
                        indication = null, // Remove ripple effect
                        interactionSource = MutableInteractionSource()
                    ) {
                        isPressedFixedBtn.value = !isPressedFixedBtn.value
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(80.dp),
                        painter = painterResource(id = fixedButtonImg),
                        tint = Color.Unspecified,
                        contentDescription = "고정버튼"
                    )
                }




            }
        }

    }


    //구도 추천 관련 UI
    @Composable
    fun CompositionArrow(arrowDirection: String,modifier: Modifier = Modifier) {
        val tint = Color.Unspecified
        val size = 50.dp
        Box(modifier = modifier) {
            when (arrowDirection) {
                "L" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 10.dp),
                        painter = painterResource(id = R.drawable.left_arrow),
                        contentDescription = "왼쪽 화살표 ", tint = tint
                    )
                }

                "R" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.CenterEnd)
                            .padding(horizontal = 10.dp),
                        painter = painterResource(id = R.drawable.right_arrow),
                        contentDescription = "오른쪽 화살표 ",
                        tint = tint
                    )
                }

                "U" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.TopCenter)
                            .padding(vertical = 10.dp),
                        painter = painterResource(id = R.drawable.up_arrow),
                        contentDescription = "위쪽 화살표 ",
                        tint = tint
                    )
                }

                "D" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.BottomCenter)
                            .padding(vertical = 10.dp),
                        painter = painterResource(id = R.drawable.down_arrow),
                        contentDescription = "아래쪽 화살표 ",
                        tint = tint
                    )
                }

                "S" -> {
                    var show by remember { mutableStateOf(true) }

                    LaunchedEffect(key1 = Unit) {
                        delay(2000) //2초만 보여줌
                        show = false
                    }
                    if (show) Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.Center),
                        painter = painterResource(id = R.drawable.good_comp),
                        contentDescription = "좋은 구도",
                        tint = tint
                    )
                }

                else -> {}
            }

        }

    }

    //메뉴별 아이콘
    @Composable
    fun MenuModule(text: String, isSelected: Boolean) {
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

    //생명주기 추적
    @Composable
    fun rememberLifecycleEvent(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current): Lifecycle.Event {
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


    private fun openGallery(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        //실 기기에서는 안됨..


        launcher.launch(
//            Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
//            }
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}


//Test Preview
@Preview
@Composable
fun PreviewExpandableBtn() {
    ExpandableButton(text = listOf("Test", "TT"),
        type = "테스트",
        isExpandedState = remember { mutableStateOf(false) },
        selectedState = remember {
            mutableIntStateOf(0)
        })
}


@Preview
@Composable
fun PreviewLower() {
//    LowerButtons(isPressedFixedBtn =, capturedThumbnailBitmap =, mainViewModel =)
}

@Preview
@Composable
fun PreviewMenuModule() {
    MenuModule(text = "hi", isSelected = true)
}

@Preview(widthDp = 300, heightDp = 400)
@Composable
fun PreviewArrow() {
    CompositionArrow("L")
}





