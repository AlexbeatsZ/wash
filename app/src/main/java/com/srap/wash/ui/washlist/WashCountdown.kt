package com.srap.wash.ui.washlist

import com.srap.wash.logic.model.OrderDetailResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

private val orderTimeFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss", Locale.ROOT)
    .withResolverStyle(ResolverStyle.STRICT)
private val orderTimeZone = ZoneId.of("Asia/Shanghai")
internal const val PAID_ORDER_STATUS = 2

internal data class WashCountdown(
    val goodsId: Int,
    val machineName: String,
    val deadlineMillis: Long,
    val programMinutes: Int
) {
    fun remainingSeconds(nowMillis: Long): Long =
        ((deadlineMillis - nowMillis).coerceAtLeast(0) + 999) / 1000

    fun remainingFraction(remainingSeconds: Long): Float =
        (remainingSeconds / (programMinutes * 60f)).coerceIn(0f, 1f)
}

internal fun OrderDetailResponse.Data.washCountdowns(nowMillis: Long): List<WashCountdown> {
    if (orderStatus != PAID_ORDER_STATUS || !finishTime.isNullOrBlank()) return emptyList()
    val estimate = completeTime ?: return emptyList()
    val deadline = try {
        LocalDateTime.parse(estimate, orderTimeFormat).atZone(orderTimeZone).toInstant().toEpochMilli()
    } catch (_: DateTimeParseException) {
        return emptyList()
    }
    if (deadline <= nowMillis) return emptyList()

    return lineItems.orEmpty().map { item ->
        WashCountdown(item.goodsId, machineName, deadline, markMinutes)
    }
}

internal fun formatWashDuration(seconds: Long): String {
    val remaining = seconds.coerceAtLeast(0)
    val hours = remaining / 3600
    val minutes = remaining / 60 % 60
    val rest = remaining % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, rest)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, rest)
    }
}
