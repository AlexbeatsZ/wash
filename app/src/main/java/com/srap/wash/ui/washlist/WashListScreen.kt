package com.srap.wash.ui.washlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.srap.wash.logic.model.LatestUsedResponse
import com.srap.wash.ui.component.SettingsTopBar
import com.srap.wash.utils.TimeUtil
import com.srap.wash.utils.ToastUtil

@Composable
fun WashListItem(navController: NavController, data: LatestUsedResponse.Data, messageData: TimeData?) {
    val context = LocalContext.current
    var residualTime by remember { mutableLongStateOf(0) }

    val viewModel: CountdownViewModel = hiltViewModel()

    val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val timeLeft = intent.getLongExtra("residualTime", 0)
            val id = intent.getIntExtra("id", 0)
            if (id != data.goodsId) return
            residualTime = timeLeft
        }
    }

    // 注册广播接收器
    DisposableEffect(messageData) {
        val filter = IntentFilter("com.srap.wash.UPDATE_TIME")
        ContextCompat.registerReceiver(
            context,
            timeReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // 取消注册广播接收器
        onDispose {
            context.unregisterReceiver(timeReceiver)
        }
    }

    // 开始倒计时
    LaunchedEffect(messageData) {
        messageData?.let {
            viewModel.startCountdown(data.goodsId, it.timeStamp, it.allTimeStamp)
        }
    }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        onClick = {
            if (data.workStatus == 10) {
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
                        containerColor = if (data.workStatus == 10) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                    Text(
                        text = if (messageData?.timeStamp != null) TimeUtil.getDateTime(residualTime) else if (data.workStatus == 10) "空闲" else "忙碌",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (messageData != null) {
                LinearProgressIndicator(
                    progress = { residualTime.toFloat() / messageData.allTimeStamp.toFloat() },
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
        items(viewModel.data) { item ->
            // 获取 TimeData，传入它以便在 LaunchedEffect 中使用
            val messageData = viewModel.messageData.get<Int, TimeData>(item.goodsId)
            WashListItem(navController, item, messageData)
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

    LaunchedEffect(Unit) {
        val isLogin = viewModel.isLogin()
        if (!isLogin) {
            navController.navigate("login") {
                // 清空导航栈
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true // 清除栈中的所有屏幕，包括起始屏幕
                }
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
