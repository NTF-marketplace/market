package com.api.market.controller.dto.response

import com.api.market.domain.orders.Orders
import com.api.market.enums.OrderStatusType
import com.api.market.enums.OrderType
import java.math.BigDecimal

data class OrdersResponse(
    val id : Long,
    val orderType: OrderType,
    val address: String,
    val ledgerPrice: BigDecimal,
    val createdAt: Long?,
    val orderStatusType: OrderStatusType,
) {
    companion object {
        fun Orders.toResponse() = OrdersResponse(
            id = id!!,
            orderType = orderType,
            address = address,
            ledgerPrice = ledgerPrice ?: BigDecimal.ZERO,
            createdAt = createdAt,
            orderStatusType = orderStatusType,
        )
    }
}
