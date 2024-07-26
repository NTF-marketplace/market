package com.api.market.controller.dto.response

import com.api.market.enums.StatusType
import com.api.market.enums.TokenType
import java.math.BigDecimal

data class ListingResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: StatusType,
    val price: BigDecimal,
    val tokenType: TokenType
)
