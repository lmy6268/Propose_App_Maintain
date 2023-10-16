package com.hanadulset.pro_poseapp.presentation.feature.gallery

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons
import com.hanadulset.pro_poseapp.utils.camera.ImageResult

object GalleryScreen {
    //최근 찍힌 이미지들을 목록으로 보여준다.
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun GalleryScreen(
        modifier: Modifier = Modifier,
        imageList: List<ImageResult>,
        onLoadImages: () -> Unit,
        onDeleteImage: (Int, () -> Unit) -> Unit,
        onBackPressed: () -> Unit
    ) {
        val updatedImageList by rememberUpdatedState(newValue = imageList) //새로이 가져온 이미지 데이터
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {

        }
        // Get screen dimensions and density
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val pagerHeight = screenHeight / 2

        Box(modifier = modifier) {
            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        onBackPressed()
                    }, modifier = Modifier
                        .size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.left_arrow),
                        contentDescription = "뒤로가기"
                    )
                }

                Text(modifier = Modifier.clickable {
                    openGallery(launcher)
                }, text = "갤러리")
            }
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically)
            ) {
                if (updatedImageList.isEmpty()) {
                    //이미지 없음을 알려줌

                } else {
                    val horizontalPagerState =
                        rememberPagerState(initialPage = 0, 0F) { updatedImageList.size }

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "${horizontalPagerState.currentPage + 1}/${updatedImageList.size}",
                        fontSize = 20.sp,
                        fontFamily = CameraScreenButtons.pretendardFamily,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                    HorizontalPager(
                        modifier = Modifier
                            .animateContentSize { initialValue, targetValue -> }
                            .height(pagerHeight),
                        state = horizontalPagerState,
                        contentPadding = PaddingValues(horizontal = pagerHeight / 6)
                    ) { idx ->
                        val currentImgUri = imageList[idx].dataUri!!
                        ImageContent(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            imgUri = currentImgUri,
                            imgSize = pagerHeight
                        )
                    }
                    IconButton(
                        onClick = {
                            //삭제하시겠습니까? 다이얼로그 보여주기
                            onDeleteImage(horizontalPagerState.currentPage) {
                                onLoadImages()
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                    ) {
                        Icon(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            tint = Color.Black,
                            painter = painterResource(id = R.drawable.icon_delete),
                            contentDescription = "지우기"
                        )
                    }


                }


            }
        }

    }

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

    @Composable
    private fun ImageContent(
        modifier: Modifier,
        imgUri: Uri,
        imgSize: Dp,
    ) {
        val imagePainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imgUri)
                .size(with(LocalDensity.current) {
                    imgSize.toPx().toInt()
                }) //뷰 사이즈의 크기 만큼 이미지 리사이징
                .build()
        )
        Box(
            modifier = modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card {
                Image(
                    painter = imagePainter,
                    contentScale = ContentScale.Fit,
                    contentDescription = "저장된 이미지",
                    alignment = Alignment.Center
                )
            }


        }


    }
}


@Preview
@Composable
fun Test() {
    GalleryScreen.GalleryScreen(
        modifier = Modifier.fillMaxSize(),
        imageList = listOf(ImageResult(), ImageResult(), ImageResult(), ImageResult()),
        onLoadImages = { },
        onDeleteImage = { index, func ->

        },
        onBackPressed = {}
    )
}