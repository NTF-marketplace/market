package com.api.market.service.dto

import com.api.market.enums.OrderStatusType
import java.math.BigDecimal

data class LedgerStatusRequest(
    val orderId: Long,
    val status: OrderStatusType,
    val ledgerPrice: BigDecimal?,
)
