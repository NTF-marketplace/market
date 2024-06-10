package com.api.market.service

import com.api.market.properties.WalletApiProperties
import com.api.market.service.dto.AccountResponse
import com.api.market.service.dto.NftResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class WalletApiService(
    walletApiProperties: WalletApiProperties,
) {

    private val webClient = WebClient.builder()
        .baseUrl(walletApiProperties.uri)
        .build()

    fun getAccountNftByAddress(wallet:String): Flux<NftResponse> {
        return webClient.get()
            .uri{
                it.path("/account/nft")
                it.queryParam("walletAddress",wallet)
                it.build()
            }
            .retrieve()
            .bodyToFlux(NftResponse::class.java)
    }
    fun getAccountByAddress(wallet:String): Flux<AccountResponse> {
        return webClient.get()
            .uri{
                it.path("/account")
                it.queryParam("walletAddress",wallet)
                it.build()
            }
            .retrieve()
            .bodyToFlux(AccountResponse::class.java)
    }
}