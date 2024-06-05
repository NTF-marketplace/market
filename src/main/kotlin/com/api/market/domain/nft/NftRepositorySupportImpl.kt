package com.api.market.domain.nft

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Mono

class NftRepositorySupportImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
): NftRepositorySupport {
    override fun insert(nft: Nft): Mono<Nft> {
        return r2dbcEntityTemplate.insert(nft)
    }
}