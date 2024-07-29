package com.api.market.domain.orders

import com.api.market.enums.OrderStatusType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name ="orders")
data class Orders (
    @Id val id : Long? = null,
    val listingId: Long,
    val address: String,
    val createdAt: Long? = System.currentTimeMillis(),
    val statusType: OrderStatusType
)