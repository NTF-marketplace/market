package com.api.market.domain.offer

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OfferRepository : ReactiveCrudRepository<Offer,Long> {

    fun findFirstByAuctionIdOrderByPriceDesc(auctionId: Long): Mono<Offer>

    fun findAllByAuctionId(auctionId: Long): Flux<Offer>
}