package com.srap.wash.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.srap.wash.logic.model.OrderListResponse
import com.srap.wash.ui.component.RefreshLoadList
import com.srap.wash.ui.component.SettingsTopBar

/**
 * 获取订单状态说明
 *
 * @param orderStatus 订单状态ID
 */
fun getOrderStatus(orderStatus: Int): String {
    return when (orderStatus) {
        0 -> "待支付"
        1 -> "订单超时"
        2 -> "已支付"
        3 -> "已完成"
        else -> orderStatus.toString()
    }
}

@Composable
fun OrderCard(item: OrderListResponse.Item) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(item.createTime)
                Text(getOrderStatus(item.orderStatus))
            }

            Text(item.machineName)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.machineFunctionName,
                    style = MaterialTheme.typography.bodySmall
                )
                Text("￥ ${item.payPrice}/${item.markPrice}")
            }
        }
    }
}

@Composable
fun OrderScreen(navController: NavController) {
    val viewModel: OrderViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            SettingsTopBar("订单")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RefreshLoadList(
                viewModel = viewModel,
            ) {
                OrderCard(it)
            }
        }
    }
}
