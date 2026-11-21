package com.srap.wash.ui.washlist

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.srap.wash.logic.model.LatestUsedResponse
import com.srap.wash.logic.repository.NetworkRepo
import com.srap.wash.logic.state.Code
import com.srap.wash.logic.state.LoadingState
import com.srap.wash.ui.base.BaseViewModel
import com.srap.wash.utils.StorageUtil
import com.srap.wash.utils.ToastUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WashListViewModel @Inject constructor(
    networkRepo: NetworkRepo,
    @ApplicationContext context: Context
) : BaseViewModel(networkRepo) {
    private val notifications = WashCountdownNotifications(context)

    var data by mutableStateOf<List<LatestUsedResponse.Data>>(emptyList())
        private set

    internal var countdowns by mutableStateOf<Map<Int, WashCountdown>>(emptyMap())
        private set

    var loading by mutableStateOf(true)
        private set

    suspend fun refreshWhileVisible() {
        refreshMachines(showError = true)
        while (refreshCountdowns()) {
            delay(30_000)
            refreshMachines(showError = false)
        }
    }

    private suspend fun refreshMachines(showError: Boolean) {
        when (val state = networkRepo.getLatestUsed("001").first()) {
            is LoadingState.Error -> if (showError) ToastUtil.show(state.errMsg)
            is LoadingState.Success -> data = state.response
        }
        loading = false
    }

    private suspend fun refreshCountdowns(): Boolean {
        val orders = networkRepo.getOrderList(page = 1, orderStatus = PAID_ORDER_STATUS.toString()).first()
        val list = (orders as? LoadingState.Success)?.response
        if (list == null) {
            val now = System.currentTimeMillis()
            countdowns = countdowns.filterValues { it.deadlineMillis > now }
            return countdowns.isNotEmpty()
        }

        val refreshed = mutableMapOf<Int, WashCountdown>()
        var allDetailsRead = true
        val paidOrders = list.items.orEmpty()
            .filter { it.orderStatus == PAID_ORDER_STATUS }
            .sortedByDescending { it.createTime }
        for (order in paidOrders) {
            val detailResult = networkRepo.getOrderDetail(order.id).first()
            val detail = (detailResult as? LoadingState.Success)?.response
            if (detail == null) {
                allDetailsRead = false
                continue
            }
            detail.washCountdowns(System.currentTimeMillis()).forEach { countdown ->
                refreshed.putIfAbsent(countdown.goodsId, countdown)
            }
        }

        val now = System.currentTimeMillis()
        val previous = if (allDetailsRead) emptyMap() else countdowns
        countdowns = (previous + refreshed).filterValues { it.deadlineMillis > now }
        notifications.update(countdowns, removeMissing = allDetailsRead)
        return paidOrders.isNotEmpty()
    }

    suspend fun isLogin(): Boolean {
        if (StorageUtil.Token.isEmpty()) return false
        return try {
            networkRepo.isLoginStatus().code != Code.NOT_LOGIN
        } catch (_: IOException) {
            true
        }
    }
}
