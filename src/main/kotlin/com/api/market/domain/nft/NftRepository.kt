package com.api.market.domain.nft

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface NftRepository : ReactiveCrudRepository<Nft, Long>, NftRepositorySupport {
}