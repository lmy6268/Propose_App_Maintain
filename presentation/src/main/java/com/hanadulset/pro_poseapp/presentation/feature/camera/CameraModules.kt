package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.util.Log
import android.view.OrientationEventListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.ui_components.PretendardFamily
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt


object CameraModules {
//    @Composable
//    fun ModeMenu(selectedIdx: MutableIntState, modifier: Modifier = Modifier) {
//        val cameraModeList = stringArrayResource(id = R.array.camera_modes)
//        Box(
//            modifier = modifier
//        ) {
//            Box(
//                Modifier
//                    .background(
//                        color = Color(0x80FAFAFA), shape = RoundedCornerShape(18.dp)
//                    )
//                    .heightIn(30.dp)
//                    .widthIn(210.dp)
//                    .align(alignment = Alignment.CenterStart)
//            ) {}
//            Row(
//                modifier = Modifier.align(alignment = Alignment.CenterStart),
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                for (i in cameraModeList.indices) {
//                    Box(modifier = Modifier
//                        .heightIn(50.dp)
//                        .widthIn(20.dp)
//                        .background(
//                            color = if (i == selectedIdx.intValue) MaterialTheme.colors.primary else Color(
//                                0x00FFFFFF
//                            ), shape = RoundedCornerShape(30.dp)
//                        )
//                        .clickable(indication = null,
//                            interactionSource = remember { MutableInteractionSource() }) {
//                            selectedIdx.intValue = i
//                            Log.d("현재 모드 : ", cameraModeList[selectedIdx.intValue])
//                        }
//
//                    ) {
//                        Text(
//                            modifier = Modifier
//                                .align(Alignment.Center)
//                                .padding(horizontal = 20.dp, vertical = 10.dp),
//                            text = cameraModeList[i],
//                            style = TextStyle(
//                                fontSize = 12.sp,
//                                fontFamily = PretendardFamily,
//                                fontWeight = FontWeight.Bold
//                            ),
//                            color = if (i == selectedIdx.intValue) Color(0xFF999999) else Color(
//                                0xFF000000
//                            ),
//                        )
//                    }
//                }
//            }
//        }
//    }


    @Composable
    fun CompositionScreen(
        modifier: Modifier = Modifier,
        screenSize: Size,
        centroid: Offset,
        trackerPoint: State<Offset?>,
        pointerState: Pair<String, Int>?, //포인터의 위치
        onSetNewPoint: () -> Unit,
        onStartToTracking: (Offset) -> Unit,
        onCancelPoint: () -> Unit
    ) {
        val context = LocalContext.current

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


        val startToShowState = remember { mutableStateOf(false) }
        val startToTracking = remember { mutableStateOf(false) }
        val radius = 30F // 구도추천 포인트 감지 영역
        val SHAKE_THRESHOLD_GRAVITY = 8F //Threshold
        val comboNonShakingCntThreshold = 15

        val timestamp = remember {
            mutableFloatStateOf(0.0F)
        }
        val shakeState = remember {
            mutableStateOf(false)
        }
        var comboNonShakingCnt = 0
        var stick = remember {
            Offset(0F, 0F)
        }
        val sensorListener = rememberUpdatedState {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_GYROSCOPE -> {
                            val (x, y, z) = event.values
                            val dt =
                                (event.timestamp - timestamp.floatValue) * (1.0F / 1000000000.0F)
                            timestamp.floatValue = event.timestamp.toFloat()
                            if (dt - timestamp.floatValue * (1.0F / 1000000000.0F) != 0F) {
                                val dx = abs(x * dt * 1000)
                                val dy = abs(y * dt * 1000)
                                val dz = abs(z * dt * 1000)
                                //만약 흔들림 방지턱을 넘은 경우
                                if (dx < SHAKE_THRESHOLD_GRAVITY && dy < SHAKE_THRESHOLD_GRAVITY && dz < SHAKE_THRESHOLD_GRAVITY) {
                                    //흔들린 이후, 연속적으로 흔들리지 않았다는 데이터가 30개 이상 전달된 경우
                                    if (shakeState.value && comboNonShakingCnt >= comboNonShakingCntThreshold) {
                                        shakeState.value = false //흔들리지 않음 상태로 변경한다.
                                        comboNonShakingCnt = 0 //cnt 개수를 초기화 한다.
                                    } else comboNonShakingCnt += 1 //흔들리지 않았다는 데이터의 개수를 1증가

                                } else {
                                    if (shakeState.value.not()) shakeState.value = true
                                    //연속적으로 흔들리지 않음 상태를 만족시키지 못했으므로
                                    else comboNonShakingCnt = 0
                                }
                            }
                        }

                        else -> {
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accurancy: Int) {}
            }
        }

        LaunchedEffect(shakeState.value) {
            if (shakeState.value) Toast.makeText(context, "흔들림이 감지되었습니다.", Toast.LENGTH_SHORT)
                .show()
            else {
                Toast.makeText(context, "흔들림이 멈췄습니다 . 트리거를 실행합니다.", Toast.LENGTH_SHORT).show()
                if (startToTracking.value.not() && startToShowState.value) {
                    startToTracking.value = true
                }
            }
        }
        LaunchedEffect(startToTracking.value) {
            if (startToTracking.value) onSetNewPoint()
        }

        LaunchedEffect(pointerState) {
            if (pointerState == null || pointerState.first == "") startToTracking.value = false
        }


        LaunchedEffect(Unit) {
            delay(3000)
            startToShowState.value = true
            sensorManager.registerListener(
                sensorListener.value(), gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        //리스너 등록
        DisposableEffect(startToShowState.value) {
            onDispose {
                sensorManager.unregisterListener(sensorListener.value(), gyroscopeSensor)
            }
        }


        HorizontalCheckModule(
            centerRadius = radius, centroid = centroid, modifier = modifier
        )
        if (pointerState != null && pointerState.first != "") RecommendPointModule(
            sensorManager = sensorManager,
            pointerState = pointerState,
            centroid = centroid,
            radius = radius,
            modifier = modifier,
            screenSize = screenSize,
            onCancelPoint = onCancelPoint
        )


    }

    // 구도 추천 포인트
    @Composable
    fun RecommendPointModule(
        sensorManager: SensorManager,
        pointerState: Pair<String, Int>,
        modifier: Modifier = Modifier,
        centroid: Offset,
        radius: Float,
        screenSize: Size,
        onCancelPoint: () -> Unit
    ) {
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        //맞춰야 하는 대상
        var targetOffset = remember {
            Offset(
                if (pointerState.first == "horizon") centroid.x * (100 + pointerState.second * 2) / 100 else centroid.x,
                if (pointerState.first == "vertical") centroid.y * (100 + pointerState.second * 2) / 100 else centroid.y,
            )
        }
        //기준 방향 각
        var stdOffset: Offset? = remember {
            null
        }

        //만약 포인터가 활성화 되었다면,
        val animatedState = animateOffsetAsState(
            targetValue = targetOffset,
            animationSpec = tween(16), label = "targetAnimation"
        )
        //이전에 들어온 X좌표 방향각
        var lastX = remember {
            0F
        }

        //이전에 들어온 Y좌표 방향각
        var lastY = remember {
            0F
        }

        val timeInterval = 400
        var timeChecker = remember {
            0
        }
        //10.01 기준으로, targetOffset에 값이 계속해서 누적되는 현상이 있었다.
        //데이터가 변화할 때만, 해당 값을 Offset에 반영해야 한다.
        val sensorListener = rememberUpdatedState {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            //현재 들어온 값에 대한 처리
                            val X = (event.values[1] * 500).roundToInt().toFloat()
                            val Y = (event.values[0] * 500).roundToInt().toFloat()
                            //기준 방향각 설정
                            if (stdOffset == null) stdOffset = Offset(X, Y)
                            timeChecker += 1
                            if (timeInterval >= timeChecker) {
                                //기준 방향각으로부터의 변화량을 구함
                                val dx = X - stdOffset!!.x
                                val dy = Y - stdOffset!!.y

                                //변화량을 target에 반영
                                targetOffset = Offset(targetOffset.x + dx, targetOffset.y + dy)
                                timeChecker = 0
                                Log.d("XY : ", "($X,$Y), $stdOffset, $targetOffset")
                            }

                        }

                        else -> {}
                    }

                }

                override fun onAccuracyChanged(sensor: Sensor, accurancy: Int) {}
            }
        }


        LaunchedEffect(Unit) {
            sensorManager.registerListener(
                sensorListener.value(), rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        //리스너 해제
        DisposableEffect(Unit) {
            onDispose {
                sensorManager.unregisterListener(sensorListener.value(), rotationVectorSensor)
            }
        }

        //위치를 설정한다.
        Canvas(modifier = modifier.fillMaxSize())
        {
            drawCircle(
                color = Color.White, radius = radius, center = animatedState.value
            )

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
        buttonName: String,
        modifier: Modifier = Modifier,
        iconDrawableId: Int,
        onClick: () -> Unit
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
        poseScreenVisibleState: Boolean,
        modifier: Modifier = Modifier,
        capturedImageBitmap: Uri?, //캡쳐된 이미지의 썸네일을 받아옴.
        ddaogiFeatureEvent: () -> Unit = {},
        fixedButtonPressedEvent: () -> Unit = {}, //고정 버튼 누름 이벤트 인식
        poseBtnClickEvent: () -> Unit = {},
        captureImageEvent: () -> Unit = { },
        zoomInOutEvent: (Float) -> Unit = {},
        onClickGalleyBtnEvent: () -> Unit
    ) {


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




        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterVertically)
        ) {
            //첫번째 단
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedModeIdxState.intValue == 0 || selectedModeIdxState.intValue == 1)

                    IconButton(modifier = Modifier.heightIn(15.dp),
                        onClick = {
                            poseBtnClickEvent()
                        }) {
                        Icon(
                            painterResource(id = R.drawable.based_circle),
                            tint = Color(
                                if (poseScreenVisibleState.not()) 0x80000000 else 0x8095FA99
                            ),
                            contentDescription = "background",
                        )
                        Text(
                            style = TextStyle(
                                color = Color.White, fontSize = 10.sp
                            ), text = "포즈\n추천"
                        )
                    }
                else IconButton(modifier = Modifier.heightIn(15.dp),
                    enabled = false,
                    onClick = {

                    }) {
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
                    imageModel = { capturedImageBitmap },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAFAFA))
                        .paint(
                            painter = painterResource(R.drawable.based_circle),
                            contentScale = ContentScale.FillBounds
                        )
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = CameraModuleExtension.CustomIndication
                        ) {
                            onClickGalleyBtnEvent() //갤러리 이미지 보여주는 화면으로 넘어가기
                        },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                    ),
                    component = rememberImageComponent {
                        +CircularRevealPlugin(
                            duration = 150 //화면에 보여주기까지 딜레이 -> 이것 때문에 느리게 보였을 듯
                        )
                    }
                )
                //캡쳐 버튼
                CameraModuleExtension.ShutterButton { captureImageEvent() }
                //고정 버튼
                CameraModuleExtension.FixedButton { fixedButtonPressedEvent() }

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


//    private fun openGallery(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
//
//        val intent = Intent()
//        intent.action = Intent.ACTION_VIEW
//
//        intent.setDataAndType(
//            Uri.Builder()
//                .path(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.path + "/Pictures/Pro_Pose/")
//                .build(),
//            "image/*",
//
//            )
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        launcher.launch(
//            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////            intent
//        )
//    }
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



