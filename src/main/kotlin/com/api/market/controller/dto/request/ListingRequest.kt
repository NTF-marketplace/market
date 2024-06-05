package com.api.market.controller.dto.request

import java.math.BigDecimal
import java.time.LocalDateTime

data class ListingRequest(
    val nftId: Long,
    val address: String,
    val endDate: LocalDateTime,
    val price: BigDecimal,
)
