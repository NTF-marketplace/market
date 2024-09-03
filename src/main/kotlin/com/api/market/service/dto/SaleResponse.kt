package com.api.market.service.dto

import com.api.market.enums.ChainType
import com.api.market.enums.OrderType
import com.api.market.enums.StatusType
import java.math.BigDecimal

data class SaleResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: StatusType,
    val startingPrice: BigDecimal,
    val chainType: ChainType,
    val orderType: OrderType,
)

