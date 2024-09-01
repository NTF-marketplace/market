package com.api.market.service.external

import com.api.market.properties.NftApiProperties
import com.api.market.service.dto.NftMetadataResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class NftApiService(
    nftApiProperties: NftApiProperties
) {
    private val webClient = WebClient.builder()
        .baseUrl(nftApiProperties.uri )
        .build()


    fun getNftsByIds(nftIds: List<Long>): Flux<NftMetadataResponse> {
        return webClient.get()
            .retrieve()
            .bodyToFlux(NftMetadataResponse::class.java)
    }

    fun getNftById(nftId: Long): Mono<NftMetadataResponse> {
        return webClient.get()
            .uri{
                it.path("/${nftId}")
                it.build()
            }
            .retrieve()
            .bodyToMono(NftMetadataResponse::class.java)
    }

}