package com.api.market.domain.offer

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("offer")
data class Offer (
    @Id val id: Long? = null,
    val auctionId: Long,
    val address: String,
    val createdAt: Long? = System.currentTimeMillis(),
    val price: BigDecimal
)
