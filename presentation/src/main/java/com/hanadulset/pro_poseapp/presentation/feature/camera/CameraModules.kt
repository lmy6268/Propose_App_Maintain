package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.OrientationEventListener
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModules.ExpandableButton
import com.hanadulset.pro_poseapp.presentation.ui_components.PretendardFamily
import com.mutualmobile.composesensors.rememberGravitySensorState
import com.mutualmobile.composesensors.rememberMagneticFieldSensorState
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.sqrt


object CameraModules {
    @Composable
    fun ModeMenu(selectedIdx: MutableIntState, modifier: Modifier = Modifier) {
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
                    .widthIn(210.dp)
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
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = PretendardFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (i == selectedIdx.intValue) Color(0xFF999999) else Color(
                                0xFF000000
                            ),
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
        modifier: Modifier = Modifier,
        viewRateIdx: Int,
        onClick: (Int) -> Unit,
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

        ) {

            if (isExpandedState.value) Row(
                Modifier
                    .fillMaxWidth()
                    .offset(x = 20.dp)
            ) {
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
                        for (idx in text.indices) {
                            Text(modifier = Modifier.clickable(indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                onClick(idx)
                            },
                                text = text[idx],
                                fontSize = 12.sp,
                                fontWeight = if (viewRateIdx == idx) FontWeight.Bold else FontWeight.Light,
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
                Text(
                    text = text[viewRateIdx], //화면 비 글씨 표기
                    fontWeight = FontWeight(FontWeight.Bold.weight), fontSize = 12.sp
                )
            }

        }
    }

    //상단 부분
    @Composable
    fun UpperButtons(
        modifier: Modifier = Modifier,
        viewRateIdx: Int,
        mainColor: Color = MaterialTheme.colors.primary,
        mainTextColor: Color = MaterialTheme.colors.onPrimary,
        subColor: Color = MaterialTheme.colors.onSecondary,
        subTextColor: Color = MaterialTheme.colors.onSecondary,
        selectedModeIdxState: MutableIntState,
        onSettingEvent: () -> Unit,
        viewRateClickEvent: (Int) -> Unit
    ) {
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val viewRateList = stringArrayResource(id = R.array.view_rates)
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
                text = viewRateList.toList(),
                type = "비율",
                viewRateIdx = viewRateIdx,
                isExpandedState = isExpandedState,
                onClick = viewRateClickEvent

            )
            //확장 가능한 버튼이 확장 되지 않은 경우
            if (!isExpandedState.value) {
                ModeMenu(
                    selectedModeIdxState,
                )
                NormalButton(
                    buttonName = "설정", iconDrawableId = R.drawable.settings
                ) {
                    //설정화면으로 이동
                    onSettingEvent()
                }

            }
        }


    }

    @Composable
    fun CompositionScreen(
        modifier: Modifier = Modifier,
        screenSize: Size,
        centroid: Offset,
        pointerState: Pair<String, Int>?,
        onSetNewPoint: () -> Unit,
    ) {
        val context = LocalContext.current
        val radius = 30F // 구도추천 포인트 감지 영역
        val triggerState = remember {
            mutableStateOf(false)
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val SHAKE_THRESHOLD_GRAVITY = 1.1F //ThresHold
        val sensorListener = rememberUpdatedState {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val (gravityX, gravityY, gravityZ) = event.values.map {
                            it / SensorManager.GRAVITY_EARTH
                        }
                        val gForce = sqrt(
                            (gravityX.pow(2) + gravityY.pow(2) + gravityZ.pow(2)).toDouble()
                        ).toFloat()
//                        Log.d(
//                            "트리거 객체 상태 ",
//                            "gForce: $gForce / pointerState : ${pointerState.toString()}"
//                        )
                        //만약 흔들림이 감지되지 않은 경우에 구도 추천을 트리거 한다.
                        if (gForce < SHAKE_THRESHOLD_GRAVITY && pointerState == null && triggerState.value.not()) {
                            onSetNewPoint()
                            triggerState.value = true
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accurancy: Int) {}
            }
        }
        val startToShowState = remember {
            mutableStateOf(false)
        }
        LaunchedEffect(Unit) {
            delay(2000)
            startToShowState.value = true
        }


        //리스너 등록
        DisposableEffect(startToShowState.value) {
            if (startToShowState.value)
                sensorManager.registerListener(
                    sensorListener.value(), accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL
                )
            onDispose {
                sensorManager.unregisterListener(
                    sensorListener.value(), accelerometerSensor
                )
            }
        }


        HorizontalCheckModule(
            centerRadius = radius, centroid = centroid, modifier = modifier
        )
//        HorizonAndVerticalCheckScreen()
        RecommendPointModule(
            pointerState = pointerState, centroid = centroid, radius = radius, modifier = modifier
        )

    }

    // 구도 추천 포인트
    @Composable
    fun RecommendPointModule(
        pointerState: Pair<String, Int>?,
        modifier: Modifier = Modifier,
        centroid: Offset,
        radius: Float
    ) {
        //맞춰야 하는 대상
        val targetOffset = remember {
            mutableStateOf<Offset?>(null)
        }
        val gravitySensorState = rememberGravitySensorState()
        val magneticFieldSensorState = rememberMagneticFieldSensorState()
        val pitch = remember {
            mutableFloatStateOf(0f)
        }
        val roll = remember {
            mutableFloatStateOf(0f)
        }
        val azimuth = remember {
            mutableFloatStateOf(0f)
        }



        LaunchedEffect(key1 = gravitySensorState, key2 = magneticFieldSensorState) {
            val gravity = arrayOf(
                gravitySensorState.xForce, gravitySensorState.yForce, gravitySensorState.zForce
            ).toFloatArray()
            val geomagnetic = arrayOf(
                magneticFieldSensorState.xStrength,
                magneticFieldSensorState.yStrength,
                magneticFieldSensorState.zStrength
            ).toFloatArray()
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                azimuth.floatValue = orientation[0]  //z축회전
                pitch.floatValue = orientation[1] // y축 회전
                roll.floatValue = orientation[2] //x축 회전
                if (targetOffset.value != null) {
                    targetOffset.value = Offset(
                        targetOffset.value!!.x * roll.floatValue * 10,
                        targetOffset.value!!.y * pitch.floatValue * 10
                    )
                    Log.d("UI 객체 이동:", targetOffset.value.toString())
                }
            }
        }


        //만약 포인터가 활성화 되었다면,
        if (pointerState != null) {
            if (targetOffset.value == null) targetOffset.value = Offset(0F, 0F)
            val animatedState = animateOffsetAsState(
                targetValue = targetOffset.value!!,
                animationSpec = tween(16),
                label = "targetAnimation"
            )

            //위치를 설정한다.
            Canvas(modifier = modifier.fillMaxSize()) {
                val pointPosition = Offset(
                    if (pointerState.first == "horizon") centroid.x * (pointerState.second / 10) else centroid.x,
                    if (pointerState.first == "vertical") centroid.y * (pointerState.second / 10) else centroid.y,
                )
                //기기가 움직이면, 타겟 또한 움직여야 하므로, 값을 저장한다.
                targetOffset.value = pointPosition
                drawCircle(
                    color = Color.White, radius = radius, center = animatedState.value
                )
            }
        }
    }


    // 수평계 모듈
    @Composable
    fun HorizontalCheckModule(
        modifier: Modifier = Modifier, centerRadius: Float, centroid: Offset
    ) {
        val context = LocalContext.current

        val shortLineLength = 30F

        val rotationState = remember {
            mutableFloatStateOf(-1F)
        }

        val animation = remember {
            Animatable(0F)
        }
        val angleThreshold = 5F
        //아직 이슈가 있음.
        val rotationEventListener = rememberUpdatedState {
            object : OrientationEventListener(context.applicationContext) {
                override fun onOrientationChanged(orientation: Int) {
                    // -1이 나오면 측정을 중지한다.
                    if (orientation != -1) {
                        rotationState.floatValue = when (orientation.toFloat()) {
                            in 180F - angleThreshold..180F -> -180F
                            in 0F..angleThreshold -> -0F
                            in 360F - angleThreshold..360F -> -360F
                            else -> -orientation.toFloat()
                        }

                        Log.d("rotation State:", orientation.toString())
                    }
                }
            }
        }


        LaunchedEffect(Unit) {
            animation.animateTo(
                rotationState.floatValue, animationSpec = tween(16, easing = LinearEasing)
            )
        }

        DisposableEffect(Unit) {
            rotationEventListener.value().enable() //시작
            onDispose {
                rotationEventListener.value().disable()//종료
            }
        }

        Canvas(
            modifier = modifier,
        ) {
            val leftEndOffset = Offset((centroid.x - centerRadius), centroid.y)
            val leftStartOffset = Offset(leftEndOffset.x - shortLineLength, centroid.y)
            val rightStartOffset = Offset((centroid.x + centerRadius), centroid.y)
            val rightEndOffset = Offset(rightStartOffset.x + shortLineLength, centroid.y)

            //회전을 감지 한다.
            rotate(
                rotationState.floatValue, pivot = centroid //회전 기준점
            ) {
                if (rotationState.floatValue == 0.0F || rotationState.floatValue in listOf(
                        -180F, 180F
                    )
                ) {
                    drawLine(
                        start = leftStartOffset,
                        end = rightEndOffset,
                        strokeWidth = 8F,
                        color = Color.Yellow
                    )
                    drawCircle(
                        color = Color.Yellow,
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 3.dp.toPx()
                        )
                    )
                } else {
                    drawLine(
                        Color.White, leftStartOffset, leftEndOffset, strokeWidth = 5F
                    )
                    drawLine(
                        Color.White, rightStartOffset, rightEndOffset, strokeWidth = 5F
                    )
                    drawCircle(
                        color = Color.White,
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 3.dp.toPx()
                        )
                    )
                }


            }
        }
    }


    @Composable
    fun FocusRing(
        duration: Long, modifier: Modifier = Modifier, color: Color, pointer: Offset?
    ) {

        if (pointer != null) {
            val animationSize = remember {
                AnimationState(150F)
            }
            val rectSize = Size(animationSize.value, animationSize.value)
            LaunchedEffect(animationSize) {
                animationSize.animateTo(
                    targetValue = 100F, animationSpec = tween(durationMillis = 200)
                )
            }
            Canvas(
                modifier = modifier.size(animationSize.value.dp)

            ) {
                drawRect(
                    color = color, topLeft = pointer.copy(
                        pointer.x - rectSize.width / 2, pointer.y - rectSize.height / 2
                    ), size = rectSize, style = Stroke(
                        width = 3.dp.toPx()
                    )
                )
                drawCircle(color, radius = 5F, center = pointer)
            }
        }


    }


    @Composable
    fun NormalButton(
        buttonName: String, modifier: Modifier = Modifier, iconDrawableId: Int, onClick: () -> Unit
    ) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.based_circle),
                tint = Color.Unspecified,
                contentDescription = buttonName
            )
            Icon(
                painter = painterResource(
                    id = iconDrawableId
                ), contentDescription = "$buttonName 아이콘"
            )
        }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    @Composable
    fun LowerButtons(
        selectedModeIdxState: MutableIntState,
        modifier: Modifier = Modifier,
        capturedImageBitmap: Uri?, //캡쳐된 이미지의 썸네일을 받아옴.
        ddaogiFeatureEvent: () -> Unit = {},
        fixedButtonPressedEvent: () -> Unit = {}, //고정 버튼 누름 이벤트 인식
        poseBtnClickEvent: () -> Unit = {},
        captureImageEvent: () -> Unit = { },
        zoomInOutEvent: (Float) -> Unit = {}
    ) {
        val isFixedBtnPressed = remember {
            mutableStateOf(false)
        }
        val fixedBtnImage = if (isFixedBtnPressed.value) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {

        }
        val isDdaogiActivated = remember {
            mutableStateOf(false)
        }
        val isPoseActivated = remember {
            mutableStateOf(false)
        }
        val textMeasurer = rememberTextMeasurer()
        //현재 줌 상태
        val zoomState = remember {
            mutableStateOf("1")
        }
        val textLayoutResult = remember {
            textMeasurer.measure(
                "포즈\n추천", TextStyle(
                    color = Color.White, fontSize = 10.sp
                )
            )
        }

        //버튼 이미지 배치
        val buttonImg = if (isPressed) R.drawable.ic_shutter_pressed
        else if (isFocused) R.drawable.ic_shutter_focused
        else R.drawable.ic_shutter_normal

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically)
        ) {
            //첫번째 단
            Row(
                horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()
            ) {

                if (selectedModeIdxState.intValue == 0 || selectedModeIdxState.intValue == 1)

//                    Canvas(
//                        modifier = Modifier
//                            .wrapContentSize()
//                            .clickable {
//                                isDdaogiActivated.value = !isDdaogiActivated.value
//                                poseBtnClickEvent()
//                            }
//                    ) {
//                        drawCircle(
//                            center = Offset(center.x, center.y),
//                            color = Color(0xFF000000), radius = 60F
//                        )
//                        drawText(
//                            textLayoutResult, topLeft = Offset(
//                                x = center.x - textLayoutResult.size.width / 2,
//                                y = center.y - textLayoutResult.size.height / 2,
//                            )
//                        )
//
//                    }
                    IconButton(modifier = Modifier.heightIn(15.dp), onClick = {
                        isPoseActivated.value = !isPoseActivated.value
                        poseBtnClickEvent()
                    }) {
                        Icon(
                            painterResource(id = R.drawable.based_circle),
                            tint = Color(
                                0x80000000
                            ),
                            contentDescription = "background",
                        )
                        Text(
                            style = TextStyle(
                                color = Color.White, fontSize = 10.sp
                            ), text = "포즈\n추천"
                        )
                    }
                else IconButton(
                    modifier = Modifier.heightIn(15.dp),
                    enabled = false,
                    onClick = {}) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(
                            0x00000000
                        ),
                        modifier = Modifier.size(15.dp),
                        contentDescription = "background",
                    )
                    Text(
                        style = TextStyle(
                            color = Color(
                                0x00000000
                            ), fontSize = 10.sp
                        ), text = "포즈\n추천"
                    )
                }


                Row {
                    listOf("1", "2").forEach { str ->
                        IconButton(
                            modifier = Modifier.apply {
                                if (str == zoomState.value) size(50.dp)
                                else size(10.dp)
                            },
                            onClick = {
                                zoomState.value = str
                                zoomInOutEvent(str.toFloat())
                            },
                        ) {
                            Icon(

                                painter = painterResource(id = R.drawable.based_circle),
                                tint = if (str == zoomState.value) Color(0xFF000000) else Color.Unspecified,
                                contentDescription = "줌버튼"
                            )
                            Text(
                                text = if (str == zoomState.value) "${str}X" else str,
                                style = MaterialTheme.typography.h2,
                                fontWeight = if (str == zoomState.value) FontWeight.Bold else FontWeight.Light,
                                color = if (str == zoomState.value) Color(0xFFFFFFFF) else Color(
                                    0xFF000000
                                ),
                            )
                        }

                    }
                }
                IconButton(modifier = Modifier.heightIn(15.dp), onClick = {
                    ddaogiFeatureEvent()
                }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(0x80000000),
                        contentDescription = "background",
                    )
                    Text(
                        style = TextStyle(
                            color = Color.White, fontSize = 10.sp
                        ), text = "따오기"
                    )
                }


            }
            //두번쨰 단
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    30.dp, Alignment.CenterHorizontally
                ), verticalAlignment = Alignment.CenterVertically
            ) {
                //캡쳐 썸네일 이미지 뷰 -> 원형으로 표사

                GlideImage(
                    imageModel = capturedImageBitmap,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAFAFA))
                        .paint(
                            painter = painterResource(R.drawable.based_circle),
                            contentScale = ContentScale.FillBounds
                        )
                        .clickable(
                            interactionSource = MutableInteractionSource(), indication = null
                        ) {
                            openGallery(launcher)
                        },
                    contentScale = ContentScale.Crop,
                    circularReveal = CircularReveal(duration = 250),
                    contentDescription = "캡쳐된 이미지"
                )
                //캡쳐 버튼
                Box(modifier = Modifier.clickable(
                    indication = null, //Ripple 효과 제거
                    interactionSource = interactionSource
                ) {
                    captureImageEvent()
                }) {
                    Icon(
                        modifier = Modifier.size(80.dp),
                        painter = painterResource(id = buttonImg),
                        tint = if (!isFocused && !isPressed) MaterialTheme.colors.secondary
                        else Color.Unspecified,
                        contentDescription = "촬영버튼"
                    )
                }

                Box(Modifier.clickable(
                    indication = null, // Remove ripple effect
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
        }

    }


//    //수직 수평 확인
//    @Composable
//    fun HorizonAndVerticalCheckScreen(
//        modifier: Modifier = Modifier
//    ) {
//        val context = LocalContext.current
//        val gravitySensorState = rememberGravitySensorState()
//        val magneticFieldSensorState = rememberMagneticFieldSensorState()
//        val pitch = remember {
//            mutableFloatStateOf(0f)
//        }
//        val roll = remember {
//            mutableFloatStateOf(0f)
//        }
//        val yaw = remember {
//            mutableFloatStateOf(0f)
//        }
//        LaunchedEffect(key1 = gravitySensorState, key2 = magneticFieldSensorState) {
//            val gravity = arrayOf(
//                gravitySensorState.xForce, gravitySensorState.yForce, gravitySensorState.zForce
//            ).toFloatArray()
//            val geomagnetic = arrayOf(
//                magneticFieldSensorState.xStrength,
//                magneticFieldSensorState.yStrength,
//                magneticFieldSensorState.zStrength
//            ).toFloatArray()
//            val r = FloatArray(9)
//            val i = FloatArray(9)
//            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
//                val orientation = FloatArray(3)
//                SensorManager.getOrientation(r, orientation)
//                pitch.floatValue = orientation[1] * 10
//                roll.floatValue = orientation[2] * 10
//                Log.d("pitch and roll", "pitch: ${pitch.floatValue} / roll: ${roll.floatValue}")
//            }
//        }
//
//        Box(modifier = modifier) {
//            Canvas(
//                modifier = Modifier
//                    .offset(
//                        x = roll.floatValue.dp, y = -pitch.floatValue.dp
//                    )
//                    .align(
//                        BiasAlignment(
//                            horizontalBias = (roll.floatValue),
//                            verticalBias = (pitch.floatValue),
//                        )
//                    ),
//
//                ) {
//                drawRect(
//                    color = Color.White, size = Size(300F, 400F)
//                )
//            }
//        }
//    }


    private fun openGallery(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW

        intent.setDataAndType(
            Uri.Builder()
                .path(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.path + "/Pictures/Pro_Pose/")
                .build(),
            "image/*",

            )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        launcher.launch(
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent
        )
    }
}


//Test Preview
@Preview
@Composable
fun PreviewExpandableBtn() {
    ExpandableButton(
        text = listOf("Test", "TT"),
        type = "테스트",
        isExpandedState = remember { mutableStateOf(false) },
        viewRateIdx = 0,
    ) {}
}

@Preview
@Composable
fun PreviewMode() {
    CameraModules.ModeMenu(selectedIdx = remember { mutableIntStateOf(1) })
}


@Preview
@Composable
fun PreviewLower() {
//    LowerButtons(isPressedFixedBtn =,capturedImageBitmap =, mainViewModel =)
}

@Preview(widthDp = 250, heightDp = 360)
@Composable
fun TestFocusRing() {
    Surface(Modifier.fillMaxSize()) {
        CameraModules.FocusRing(
            color = Color(0xFF000000), pointer = Offset(20F, 20F), duration = 1000L
        )
    }
}
//
//@Preview(
//    backgroundColor = 0xFF000000, showSystemUi = true
//)
//@Composable
//fun TestCompositionScreen() {
//    Box(
//        Modifier
//            .fillMaxSize()
//            .background(color = Color.Black)
//    ) {
//        val density = LocalDensity.current
//        val context = LocalContext.current
//        CameraModules.CompositionScreen(modifier = Modifier.align(Alignment.Center),
//            screenSize = with(density) {
//                Size(411.dp.toPx(), 800.dp.toPx())
//            },
//            context,
//            onSetNewPoint = {
//                Pair("horizon", 10)
//            })
//    }
//}


//
////구도 추천 관련 UI
//@Composable
//fun CompositionArrow(arrowDirection: String, modifier: Modifier = Modifier) {
//    val tint = Color.Unspecified
//    val size = 50.dp
//    Box(modifier = modifier) {
//        when (arrowDirection) {
//            "L" -> {
//                Icon(
//                    modifier = Modifier
//                        .size(size)
//                        .align(Alignment.CenterStart)
//                        .padding(horizontal = 10.dp),
//                    painter = painterResource(id = R.drawable.left_arrow),
//                    contentDescription = "왼쪽 화살표 ",
//                    tint = tint
//                )
//            }
//
//            "R" -> {
//                Icon(
//                    modifier = Modifier
//                        .size(size)
//                        .align(Alignment.CenterEnd)
//                        .padding(horizontal = 10.dp),
//                    painter = painterResource(id = R.drawable.right_arrow),
//                    contentDescription = "오른쪽 화살표 ",
//                    tint = tint
//                )
//            }
//
//            "U" -> {
//                Icon(
//                    modifier = Modifier
//                        .size(size)
//                        .align(Alignment.TopCenter)
//                        .offset(y = 80.dp)
//                        .padding(vertical = 10.dp),
//                    painter = painterResource(id = R.drawable.up_arrow),
//                    contentDescription = "위쪽 화살표 ",
//                    tint = tint
//                )
//            }
//
//            "D" -> {
//                Icon(
//                    modifier = Modifier
//                        .size(size)
//                        .align(Alignment.BottomCenter)
//                        .offset(y = (-70).dp)
//                        .padding(vertical = 10.dp),
//                    painter = painterResource(id = R.drawable.down_arrow),
//                    contentDescription = "아래쪽 화살표 ",
//                    tint = tint
//                )
//            }
//
//            "S" -> {
//                var show by remember { mutableStateOf(true) }
//
//                LaunchedEffect(key1 = Unit) {
//                    delay(2000) //2초만 보여줌
//                    show = false
//                }
//                if (show) Icon(
//                    modifier = Modifier
//                        .size(size)
//                        .align(Alignment.Center),
//                    painter = painterResource(id = R.drawable.good_comp),
//                    contentDescription = "좋은 구도",
//                    tint = tint
//                )
//            }
//
//            else -> {}
//        }
//
//    }
//
//}



