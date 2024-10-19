package com.api.market.domain.auction

import com.api.market.enums.StatusType
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AuctionRepository: R2dbcRepository<Auction,Long> {

    fun existsByNftIdAndAddressAndStatusType(nftId: Long, address: String, statusType: StatusType): Mono<Boolean>

    fun findByIdAndStatusType(auctionId: Long, statusType: StatusType): Mono<Auction>

    fun findByNftIdAndStatusType(nftId: Long, statusType: StatusType): Flux<Auction>
}