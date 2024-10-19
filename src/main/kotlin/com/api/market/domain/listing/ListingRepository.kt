package com.api.market.domain.listing

import com.api.market.enums.StatusType
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ListingRepository: ReactiveCrudRepository<Listing,Long> {

    fun existsByNftIdAndAddressAndStatusTypeIn(nftId: Long, address: String, statusTypes: Collection<StatusType>): Mono<Boolean>

    fun findByIdAndStatusType(id: Long, statusType: StatusType): Mono<Listing>

    fun findAllByNftIdAndStatusTypeIn(nftId: Long, statusType: Collection<StatusType>): Flux<Listing>
}