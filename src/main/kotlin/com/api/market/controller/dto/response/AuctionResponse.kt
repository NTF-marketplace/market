package com.api.market.controller.dto.response

import com.api.market.enums.ChainType
import com.api.market.enums.StatusType
import java.math.BigDecimal

data class AuctionResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: StatusType,
    val startingPrice: BigDecimal,
    val chainType: ChainType
)
