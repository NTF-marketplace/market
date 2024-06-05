package com.api.market.domain.listing

import org.springframework.data.r2dbc.repository.R2dbcRepository

interface ListingRepository: R2dbcRepository<Listing,Long> {
}