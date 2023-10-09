package com.hanadulset.pro_poseapp.presentation.feature

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.filament.Engine
import com.google.ar.core.Config
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.hanadulset.pro_poseapp.presentation.R
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.Node
import io.github.sceneview.node.RenderableNode
import io.github.sceneview.utils.Color

object ArScreen {
    @Composable
    fun ArScreen(
        modifier: Modifier = Modifier,
        onCreatedEvent: () -> Unit = {},
        onDisposeEvent: () -> Unit = {}
//        arNode: ArNode? = null
    ) {
//        val nodes = remember { mutableStateListOf<ArNode>(
//            ArNode(
//                Engine
//            )
//        ) }
//        DisposableEffect(Unit) {
//            onDispose { onDisposeEvent() }
//        }


        ARScene(
            modifier = modifier,
            planeRenderer = false,
            onCreate = {
                Log.d("ViewCreated:", it.toString())
                onCreatedEvent()

            },
            onFrame = {

            },
            onSessionCreate = { session: ArSession ->
//                val node = ArModelNode(
//                    placementMode = PlacementMode.INSTANT
//                )




                Log.d("SessionCreated:", session.toString())
                configureSession { arSession, config ->
                    config.depthMode = Config.DepthMode.AUTOMATIC
                    config.focusMode = Config.FocusMode.AUTO
                }
            }
        )
    }


}

@Composable
@Preview
fun TestAr() {
    ArScreen.ArScreen(
        modifier = Modifier.fillMaxSize(),
    )

}