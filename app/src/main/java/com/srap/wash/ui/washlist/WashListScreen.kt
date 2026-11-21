package com.srap.wash.ui.washlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.srap.wash.logic.model.LatestUsedResponse
import com.srap.wash.ui.component.SettingsTopBar
import com.srap.wash.utils.ToastUtil
import kotlinx.coroutines.delay

@Composable
private fun WashListItem(
    navController: NavController,
    data: LatestUsedResponse.Data,
    countdown: WashCountdown?
) {
    var secondsLeft by remember(countdown) {
        mutableLongStateOf(countdown?.remainingSeconds(System.currentTimeMillis()) ?: 0)
    }
    LaunchedEffect(countdown) {
        if (countdown == null) return@LaunchedEffect
        while (true) {
            secondsLeft = countdown.remainingSeconds(System.currentTimeMillis())
            if (secondsLeft == 0L) break
            delay(1000)
        }
    }
    val isIdle = data.workStatus == 10 && secondsLeft == 0L

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        onClick = {
            if (isIdle) {
                navController.navigate("detail/${data.goodsId}")
            } else {
                ToastUtil.show("该洗衣机正在使用中...")
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // 左右分隔
            ) {
                // 左边的两行文字
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.goodsName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = data.orgName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 右边的文字，居中对齐

                Column(
                    modifier = Modifier.align(Alignment.CenterVertically), // 垂直居中
                    horizontalAlignment = Alignment.CenterHorizontally // 水平居中
                ) {
                    Badge(
                        containerColor = if (isIdle) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                    Text(
                        text = when {
                            secondsLeft > 0 -> formatWashDuration(secondsLeft)
                            isIdle -> "空闲"
                            else -> "忙碌"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (countdown != null && countdown.programMinutes > 0 && secondsLeft > 0) {
                LinearProgressIndicator(
                    progress = { countdown.remainingFraction(secondsLeft) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
        }

    }
}

@Composable
fun WashList(navController: NavController, viewModel: WashListViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "最近使用",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
        if (viewModel.data.isEmpty()) {
            item {
                Text(
                    text = "暂无最近使用记录，点击右下角扫码添加洗衣机",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
        items(viewModel.data, key = { it.goodsId }) { item ->
            WashListItem(navController, item, viewModel.countdowns[item.goodsId])
        }
    }
}

@Composable
fun ElectricityCard() {
    val viewModel: ElectricityViewModel = hiltViewModel()

    val data = viewModel.data
    if (data != null) {
        Card(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally // 内容居中
            ) {
                Text("剩余电费")

                Text("￥ ${data.remainPower}")
            }
        }
    }
}

@Composable
fun WashListScreen(navController: NavController) {
    val viewModel: WashListViewModel = hiltViewModel()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(lifecycle) {
        val isLogin = viewModel.isLogin()
        if (!isLogin) {
            navController.navigate("login") {
                // 清空导航栈
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true // 清除栈中的所有屏幕，包括起始屏幕
                }
            }
        } else {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.refreshWhileVisible()
            }
        }
    }

    Scaffold(
        topBar = {
            SettingsTopBar("洗衣机")
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !viewModel.loading,
                enter = slideInVertically { it * 2 },
                exit = slideOutVertically { it * 2 },
            ) {
                FloatingActionButton(

                    onClick = {
                        navController.navigate("scan/code")
                    },
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "扫描二维码")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 显示加载动画
            if (viewModel.loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ElectricityCard()
                        WashList(navController, viewModel)
                    }
                }
            }
        }
    }
}
