package com.api.market.controller.dto.response

import com.api.market.enums.ListingStatusType
import com.api.market.enums.TokenType
import java.math.BigDecimal

data class ListingResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: ListingStatusType,
    val price: BigDecimal,
    val tokenType: TokenType
)
