package com.api.market.service.dto

import com.api.market.controller.dto.response.AuctionResponse
import com.api.market.domain.auction.Auction
import com.api.market.domain.listing.Listing
import com.api.market.enums.ChainType
import com.api.market.enums.OrderType
import com.api.market.enums.StatusType
import java.math.BigDecimal

data class SaleResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: StatusType,
    val price: BigDecimal,
    val chainType: ChainType,
    val orderType: OrderType,
) {
    companion object {
        fun Listing.toResponse() = SaleResponse(
            id = this.id!!,
            nftId = this.nftId,
            address = this.address,
            createdDateTime = this.createdDate,
            endDateTime =  this.endDate,
            statusType = this.statusType,
            price = this.price,
            chainType = this.chainType,
            orderType =  OrderType.LISTING
        )

        fun Auction.toResponse() = SaleResponse (
            id = this.id!!,
            nftId = this.nftId,
            address = this.address,
            createdDateTime = this.createdDate,
            endDateTime =  this.endDate,
            statusType = this.statusType,
            price = this.startingPrice,
            chainType = this.chainType,
            orderType = OrderType.AUCTION
        )
    }
}

