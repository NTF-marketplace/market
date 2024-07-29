package com.api.market.service.dto

import com.api.market.enums.ChainType
import com.api.market.enums.TokenType
import java.math.BigDecimal

data class LedgerRequest(
    val nftId: Long,
    val address: String,
    val price: BigDecimal,
    val chainType: ChainType,
    val orderAddress: String
)

