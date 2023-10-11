package com.hanadulset.pro_poseapp.presentation.feature

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import com.google.ar.core.Anchor
import com.google.ar.core.CameraConfig
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.arcore.instantPlacementEnabled
import io.github.sceneview.ar.arcore.planeFindingEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ArScreen {
    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    fun ArScreen(
        modifier: Modifier = Modifier,
        onCreatedEvent: () -> Unit = {},
        onDisposeEvent: () -> Unit = {},
        onShowAgainEvent: Boolean
    ) {


        val lifecycle = LocalLifecycleOwner.current
        val arSessionState = remember {
            mutableStateOf<ArSession?>(null)
        }
        val arSceneView = remember {
            mutableStateOf<ArSceneView?>(null)
        }
        val anchorState = remember {
            mutableStateOf<Anchor?>(null)
        }

        LaunchedEffect(Unit) {
            onCreatedEvent()
        }

        ARScene(
            modifier = modifier,
            planeRenderer = false,
            onCreate = {

            },
            onFrame = { frame ->

                arSessionState.value = arSessionState.value ?: arSession

                arSceneView.value = arSceneView.value ?: this@ARScene
                CoroutineScope(Dispatchers.Main).launch {
                    if (arSession!!.isResumed) {
                        if (anchorState.value == null) {
                            Log.d(
                                "Now session config: ",
                                "${frame.camera.trackingState} and ${arSession!!.config.instantPlacementEnabled} "
                            )
                            //트래킹이 가능할 때 앵커를 생성한다.
//                            if (frame.camera.trackingState == TrackingState.TRACKING)
                            anchorState.value = frame.createAnchor(
                                0F,
                                0F,
                            )

                        }

                        onPause(lifecycle)
                        arSessionState.value!!.pause()
                        onDisposeEvent()
                    }
                }

            },
            onSessionCreate =
            { session: ArSession ->
                session.let { arSession ->
                    arSession.configure { config ->
                        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
                        config.focusMode = Config.FocusMode.AUTO
                        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                        config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                        config.instantPlacementEnabled = true
                    }
                }
            }
        )

        LaunchedEffect(onShowAgainEvent)
        {
            if (onShowAgainEvent) {
                onCreatedEvent()
                arSceneView.value?.onResume(lifecycle)
                arSessionState.value = arSceneView.value!!.arSession

                Log.d(
                    "Anchor:",
                    anchorState.value?.toString() ?: ""
                )

                Log.d(
                    "updated Anchor:",
                    arSessionState.value?.update()?.updatedAnchors?.toList()?.toString() ?: "null"
                )
            }

        }


    }
}

@Composable
@Preview
fun TestAr() {
    ArScreen.ArScreen(
        modifier = Modifier.fillMaxSize(), onShowAgainEvent = true
    )

}