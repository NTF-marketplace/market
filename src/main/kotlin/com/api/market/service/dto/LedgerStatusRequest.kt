package com.api.market.service.dto

import com.api.market.enums.OrderStatusType

data class LedgerStatusRequest(
    val orderId: Long,
    val status: OrderStatusType,
)
