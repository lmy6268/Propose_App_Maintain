package com.hanadulset.pro_poseapp.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.sceneview.Scene
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.node.Node

object ArScreen {
    @Composable
    fun ArScreen(
        modifier: Modifier = Modifier,
        ) {
        val nodes = remember { mutableStateListOf<ArNode>() }
        Scene(
            modifier = modifier,
            nodes = nodes,
            onCreate = {

            },
            onFrame = {

            },
            onTap = { motionEvent, node, i ->

            }
        )
    }
}