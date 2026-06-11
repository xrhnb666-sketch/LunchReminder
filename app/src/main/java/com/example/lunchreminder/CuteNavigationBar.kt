package com.example.lunchreminder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppTab(
    val label: String,
    val iconRes: Int,
) {
    HOME("首页", UiAssets.home),
    HISTORY("历史", UiAssets.history),
    STATS("统计", UiAssets.stats),
    SETTINGS("设置", UiAssets.settings),
}

@Composable
fun CuteNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = CuteColors.Background,
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth()
                .height(76.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = androidx.compose.ui.graphics.Color(0x1AD5A06C),
                    spotColor = androidx.compose.ui.graphics.Color(0x1AD5A06C),
                )
                .clip(RoundedCornerShape(24.dp))
                .background(CuteColors.NavBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTab.entries.forEach { tab ->
                    CuteNavigationItem(
                        tab = tab,
                        selected = currentTab == tab,
                        onClick = { onTabSelected(tab) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.CuteNavigationItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val textColor = if (selected) CuteColors.Orange else CuteColors.TextSecondary
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = tab.iconRes),
            contentDescription = tab.label,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(if (selected) 5.dp else 0.dp)
                .clip(RoundedCornerShape(50))
                .background(CuteColors.Orange),
        )
    }
}
