package com.api.market.controller.dto.response

import com.api.market.enums.ChainType
import com.api.market.enums.OrderType
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
    val chainType: ChainType,
)
