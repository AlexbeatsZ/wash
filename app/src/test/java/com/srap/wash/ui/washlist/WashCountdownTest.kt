package com.srap.wash.ui.washlist

import com.srap.wash.logic.model.OrderDetailResponse
import java.time.Instant
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WashCountdownTest {
    private val now = Instant.parse("2026-11-21T04:15:00Z").toEpochMilli()

    @Test
    fun serverEstimatesUseChinaTime() {
        val originalZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            val countdown = order.washCountdowns(now).single()
            assertEquals(1800L, countdown.remainingSeconds(now))
            assertEquals(1500L, countdown.remainingSeconds(now + 300_000))
            assertEquals(0.5f, countdown.remainingFraction(1800), 0f)
        } finally {
            TimeZone.setDefault(originalZone)
        }
    }

    @Test
    fun expiredEstimatesReachZeroAfterLongPause() {
        val countdown = order.washCountdowns(now).single()
        assertEquals(1L, countdown.remainingSeconds(countdown.deadlineMillis - 1))
        assertEquals(0L, countdown.remainingSeconds(countdown.deadlineMillis + 87_000))
        assertTrue(order.washCountdowns(countdown.deadlineMillis).isEmpty())
    }

    @Test
    fun finishedAndUnpaidOrdersDoNotProduceCountdowns() {
        assertTrue(order.copy(orderStatus = 3).washCountdowns(now).isEmpty())
        assertTrue(order.copy(finishTime = "2026-11-21 12:10:00").washCountdowns(now).isEmpty())
        assertTrue(order.copy(orderStatus = 0).washCountdowns(now).isEmpty())
    }

    @Test
    fun missingInvalidAndEmptyOrderDataDoNotStartTimers() {
        assertTrue(order.copy(completeTime = null).washCountdowns(now).isEmpty())
        assertTrue(order.copy(completeTime = "2026-02-31 12:45:00").washCountdowns(now).isEmpty())
        assertTrue(order.copy(lineItems = emptyList()).washCountdowns(now).isEmpty())
    }

    @Test
    fun multipleMachinesKeepIndependentCountdownsAndDurationKeepsHours() {
        val otherMachine = order.lineItems.single().copy(goodsId = 20)
        assertEquals(
            listOf(10, 20),
            order.copy(lineItems = order.lineItems + otherMachine)
                .washCountdowns(now).map { it.goodsId }
        )
        assertEquals("1:05:01", formatWashDuration(3901))
        assertEquals("01:01", formatWashDuration(61))
        assertEquals("00:00", formatWashDuration(-2))
        assertEquals(1f, order.washCountdowns(now).single().remainingFraction(4000), 0f)
    }

    private val order = OrderDetailResponse.Data(
        id = "id",
        orderNo = "order-no",
        orderType = 0,
        orderStatus = 2,
        createTime = "2026-11-21 11:00:00",
        completeTime = "2026-11-21 12:45:00",
        finishTime = "",
        markPrice = "",
        payPrice = "",
        machineType = 0,
        machineName = "洗衣机",
        shopAutoId = 0,
        shopId = "",
        shopName = "",
        isBluetooth = 0,
        markMinutes = 60,
        machineFunctionName = "",
        parentTypeId = "",
        lineItems = listOf(OrderDetailResponse.LineItem(10, 1, null, null, null, null))
    )
}
