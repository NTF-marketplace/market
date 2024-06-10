package com.api.market.domain.order

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("order")
data class Order(
    @Id val id: Long? = null,
    val address: String,
    val listingId: Long,
    val createdAt: Long? = Instant.now().toEpochMilli()
    )
