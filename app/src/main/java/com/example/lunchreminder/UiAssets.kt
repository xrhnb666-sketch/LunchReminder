package com.example.lunchreminder

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

object UiAssets {
    @DrawableRes val breakfast = R.drawable.ic_breakfast
    @DrawableRes val lunch = R.drawable.ic_lunch
    @DrawableRes val dinner = R.drawable.ic_dinner
    @DrawableRes val skipCloud = R.drawable.ic_skip_cloud

    @DrawableRes val bear = R.drawable.img_bear
    @DrawableRes val appIcon = R.drawable.app_icon
    @DrawableRes val plant = R.drawable.img_plant
    @DrawableRes val cloudBackground = R.drawable.img_cloud_bg
    @DrawableRes val stars = R.drawable.img_stars

    @DrawableRes val home = R.drawable.nav_home
    @DrawableRes val history = R.drawable.nav_history
    @DrawableRes val stats = R.drawable.nav_stats
    @DrawableRes val settings = R.drawable.nav_settings

    @DrawableRes val flower = R.drawable.decor_flower
    @DrawableRes val smallStar = R.drawable.img_star_small

    @Composable
    fun painter(@DrawableRes resId: Int): Painter {
        return painterResource(id = resId)
    }
}

@Composable
fun BoxScope.UiAssetBackgroundSlot(
    visible: Boolean = false,
    @DrawableRes resId: Int = UiAssets.cloudBackground,
    modifier: Modifier = Modifier.fillMaxSize(),
    alpha: Float = 0.08f,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (visible) {
        Image(
            painter = UiAssets.painter(resId),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier.alpha(alpha),
        )
    }
}
