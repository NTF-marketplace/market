package com.api.market.service

import com.api.market.properties.WalletApiProperties
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Service
class WalletApiService(
    walletApiProperties: WalletApiProperties,
) {

    private val webClient = WebClient.builder()
        .baseUrl(walletApiProperties.uri)
        .build()

    fun getAccountNftByAddress(wallet:String): Flux<Long> {
        return webClient.get()
            .uri{
                it.path("/account/nft")
                it.queryParam("wallet",wallet)
                it.build()
            }
            .retrieve()
            .bodyToFlux(Long::class.java)
    }
}