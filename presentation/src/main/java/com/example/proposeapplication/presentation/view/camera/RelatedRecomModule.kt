package com.example.proposeapplication.presentation.view.camera

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proposeapplication.presentation.view.camera.RelatedRecomModule.MenuModule
import com.example.proposeapplication.presentation.view.camera.RelatedRecomModule.RecommendPoses

object RelatedRecomModule {
    // extension method for current page offset
//    @OptIn(ExperimentalFoundationApi::class)
//    fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
//        return (currentPage - page) + currentPageOffsetFraction
//    }

    //추천된 포즈를 슬라이드로 볼 수 있게 해줌 
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RecommendPoses(poseList: List<String>, modifier: Modifier = Modifier) {

        val pageCount = 10
        val pagerState = rememberPagerState(pageCount = {
            4
        })

        Surface(
            modifier = modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->
                // Our page content
                Text(
                    text = "Page: $page"
                )
            }
            Row(
                Modifier.fillMaxSize(0.3f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(20.dp)
                            .clickable {

                            }

                    )
                }
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


}

@Preview
@Composable
fun PreviewMenuModule() {
    MenuModule(text = "hi", isSelected = true)
}

@Preview
@Composable
fun PreviewRecommendPoses() {
    RecommendPoses(listOf("hello", "1"))
}