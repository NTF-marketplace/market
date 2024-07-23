package com.api.market.domain.listing

import com.api.market.enums.ListingStatusType
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ListingRepository: R2dbcRepository<Listing,Long> {

    fun existsByNftIdAndAddressAndStatusType(nftId: Long, address: String, statusType: ListingStatusType): Mono<Boolean>

}