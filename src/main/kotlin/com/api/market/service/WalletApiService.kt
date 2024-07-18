package com.api.market.service

import com.api.market.properties.WalletApiProperties
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class WalletApiService(
    walletApiProperties: WalletApiProperties,
) {

    private val webClient = WebClient.builder()
        .baseUrl(walletApiProperties.uri)
        .build()

    fun getAccountNftByAddress(wallet:String,nftId: Long): Mono<Boolean> {
        return webClient.get()
            .uri{
                it.path("v1/account/has/nft")
                it.queryParam("address",wallet)
                it.queryParam("nftId",nftId)
                it.build()
            }
            .retrieve()
            .bodyToMono(Boolean::class.java)
    }
}