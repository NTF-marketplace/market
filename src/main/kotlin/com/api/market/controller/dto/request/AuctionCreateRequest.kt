package com.api.market.controller.dto.request

import com.api.market.enums.TokenType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class AuctionCreateRequest(
    val nftId: Long,
    val createdDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val startingPrice: BigDecimal,
    val tokenType: TokenType
)
