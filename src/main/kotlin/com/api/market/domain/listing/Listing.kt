package com.api.market.domain.listing

import com.api.market.controller.dto.request.ListingUpdateRequest
import com.api.market.enums.TokenType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

@Table("listing")
data class Listing(
    @Id val id: Long? = null,
    val nftId: Long,
    val address: String,
    val createdAt: Long? = Instant.now().toEpochMilli(),
    val endDate: Long,
    val active: Boolean,
    val price: BigDecimal,
    val tokenType : TokenType
){
    fun update(updateRequest: ListingUpdateRequest): Listing {
        return this.copy(
            endDate = updateRequest.endDate.toInstant().toEpochMilli(),
            price = updateRequest.price.toBigDecimal(),
            tokenType = updateRequest.tokenType
        )
    }

    fun cancel(): Listing {
        return this.copy(active = false)
    }
}
