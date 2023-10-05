package com.hanadulset.pro_poseapp.presentation.feature.gallery

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.utils.camera.ImageResult

object GalleryScreen {
    //최근 찍힌 이미지들을 목록으로 보여준다.
    @Composable
    fun GalleryScreen(
        imageList: List<ImageResult>,
        onLoadImages: () -> Unit,
        onDeleteImage: (Int) -> Unit
    ) {
        val lazyListState = rememberLazyListState()
        val currentIndex =
            remember { derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo.map { it.index } } }
        //이미지 슬라이더 및 현재 사진 삭제 기능 갖추기
        Box(modifier = Modifier) {
            Row {

            }
            if (imageList.isNotEmpty()) {
                LazyRow(
                    state = lazyListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(imageList) { index, item ->
//                        GlideImage(
//                            imageModel = { null },
//
//                            ) {
//
//                        }
                    }
                }
                IconButton(
                    onClick = {
                        //삭제하시겠습니까? 다이얼로그 보여주기
                    }, modifier = Modifier.wrapContentSize()
                ) {
                    Icon(
                        tint = Color.White,
                        painter = painterResource(id = R.drawable.icon_delete),
                        contentDescription = "지우기"
                    )
                }
            }
            //이미지가 없음을 알려줌
            else {

            }
        }
    }
}

@Preview
@Composable
fun Test() {
    GalleryScreen.GalleryScreen(
        imageList = listOf(ImageResult()),
        onLoadImages = { },
        onDeleteImage = {}
    )
}