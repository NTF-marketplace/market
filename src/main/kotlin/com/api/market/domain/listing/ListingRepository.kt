package com.api.market.domain.listing

import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ListingRepository: R2dbcRepository<Listing,Long> {

    fun existsByNftIdAndAddressAndActiveTrue(nftId: Long, address: String): Mono<Boolean>

    fun findByNftIdAndActiveTrue(nftId: Long): Mono<Listing>

}