package com.api.market.controller.dto.request

import com.api.market.enums.TokenType
import java.time.ZonedDateTime

data class ListingCreateRequest(
    val nftId: Long,
    val address: String,
    val endDate: ZonedDateTime,
    val price: Double,
    val tokenType: TokenType
)
