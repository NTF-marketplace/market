package com.api.market.controller.dto.request

import com.api.market.enums.TokenType
import java.time.ZonedDateTime

data class ListingUpdateRequest(
    val endDate: ZonedDateTime,
    val price: Double,
    val tokenType : TokenType
)
