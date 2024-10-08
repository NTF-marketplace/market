package com.api.market.controller.dto.request

import com.api.market.enums.ChainType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class ListingCreateRequest(
    val nftId: Long,
    val createdDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val price: BigDecimal,
    val chainType: ChainType
)
