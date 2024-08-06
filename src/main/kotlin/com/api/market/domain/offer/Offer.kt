package com.api.market.domain.offer

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(JsonSubTypes.Type(value = Offer::class, name = "offer"))
@Table("offer")
data class Offer (
    @Id val id: Long? = null,
    val auctionId: Long,
    val address: String,
    val createdAt: Long? = System.currentTimeMillis(),
    val price: BigDecimal
)
