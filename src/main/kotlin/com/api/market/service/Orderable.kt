package com.api.market.service

import com.api.market.enums.ChainType
import java.math.BigDecimal

interface Orderable {
    val id: Long
    val nftId: Long
    val address: String
    val price: BigDecimal
    val chainType: ChainType
}