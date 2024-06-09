package com.api.market.domain.nft

import reactor.core.publisher.Mono

interface NftRepositorySupport {
    fun insert(nft: Nft): Mono<Nft>
}