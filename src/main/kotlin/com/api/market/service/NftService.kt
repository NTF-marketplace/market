package com.api.market.service

import com.api.market.domain.nft.NftRepository
import com.api.market.service.dto.NftResponse
import com.api.market.service.dto.NftResponse.Companion.toEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class NftService(
    private val nftRepository: NftRepository,
) {
    fun save(response: NftResponse): Mono<Void> {
        return nftRepository.findById(response.id)
            .flatMap {
                Mono.empty<Void>()
            }
            .switchIfEmpty(
                nftRepository.insert(response.toEntity()).then()
            )
    }


}