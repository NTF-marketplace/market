package com.api.market.domain.listing

import com.api.market.enums.StatusType
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface ListingRepository: R2dbcRepository<Listing,Long> {

    fun existsByNftIdAndAddressAndStatusType(nftId: Long, address: String, statusType: StatusType): Mono<Boolean>

}