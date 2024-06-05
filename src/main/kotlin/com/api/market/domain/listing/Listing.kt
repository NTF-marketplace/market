package com.api.market.domain.listing

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("listing")
data class Listing(
    @Id val id: Long? = null,
    val nftId: Long,
    val address: String,
    val createAt: Long? = System.currentTimeMillis(),
    val endDate: Long,
    val isActive: Boolean,
    val price: BigDecimal
)
