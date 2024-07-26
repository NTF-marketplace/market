package com.api.market.domain.listing

import com.api.market.enums.StatusType
import com.api.market.enums.TokenType
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal


@JsonSerialize
@JsonDeserialize
@Table("listing")
data class Listing @JsonCreator constructor(
    @JsonProperty("id") @Id val id: Long? = null,
    @JsonProperty("nftId") val nftId: Long,
    @JsonProperty("address") val address: String,
    @JsonProperty("createdDate") val createdDate: Long,
    @JsonProperty("endDate") val endDate: Long,
    @JsonProperty("statusType") val statusType: StatusType,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("tokenType") val tokenType: TokenType
) {
    fun update(updateRequest: Listing): Listing {
        return this.copy(
            statusType = updateRequest.statusType
        )
    }

    fun cancel(status: StatusType): Listing {
        return this.copy(statusType = status)
    }
}