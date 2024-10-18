package com.api.market.controller.dto.response

import com.api.market.domain.offer.Offer
import java.math.BigDecimal

data class OfferResponse(
    val id: Long,
    val address: String,
    val createdAt: Long?,
    val price: BigDecimal,
) {
    companion object {
        fun Offer.toResponse() = OfferResponse(
            id = id!!,
            address = address,
            createdAt = createdAt,
            price = price,
        )
    }
}
