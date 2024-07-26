package com.api.market.domain.auction

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
@Table("auction")
data class Auction @JsonCreator constructor(
    @JsonProperty("id") @Id val id : Long? = null,
    @JsonProperty("nftId") val nftId: Long,
    @JsonProperty("address") val address: String,
    @JsonProperty("createdDate") val createdDate: Long,
    @JsonProperty("endDate") val endDate: Long,
    @JsonProperty("statusType") val statusType: StatusType,
    @JsonProperty("startingPrice") val startingPrice: BigDecimal,
    @JsonProperty("tokenType") val tokenType: TokenType
)
