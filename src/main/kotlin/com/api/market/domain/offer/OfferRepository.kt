package com.api.market.domain.offer

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OfferRepository : ReactiveCrudRepository<Offer,Long> {
}