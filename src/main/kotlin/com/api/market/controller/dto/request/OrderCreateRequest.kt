package com.api.market.controller.dto.request

import com.api.market.enums.OrderType

data class OrderCreateRequest(
    val orderableId: Long,
    val orderType: OrderType,
    val listingId: Long
)
