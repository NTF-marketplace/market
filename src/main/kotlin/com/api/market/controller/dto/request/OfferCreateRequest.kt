package com.api.market.controller.dto.request

import java.math.BigDecimal

data class OfferCreateRequest(
    val auctionId: Long,
    val price: BigDecimal,
)
