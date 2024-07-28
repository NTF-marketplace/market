package com.api.market.domain

import com.api.market.domain.auction.Auction
import com.api.market.domain.listing.Listing
import com.api.market.enums.StatusType
import com.api.market.enums.TokenType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Listing::class, name = "listing"),
    JsonSubTypes.Type(value = Auction::class, name = "auction")
)
interface ScheduleEntity {
    val id: Long?
    val nftId: Long
    val statusType: StatusType
    val createdDate: Long
    val endDate: Long
    val tokenType: TokenType
    val address: String
    fun updateStatus(statusType: StatusType): ScheduleEntity
}