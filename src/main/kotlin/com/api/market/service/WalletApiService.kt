package com.api.market.service

import com.api.market.enums.ChainType
import com.api.market.properties.WalletApiProperties
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class WalletApiService(
    walletApiProperties: WalletApiProperties,
) {

    private val webClient = WebClient.builder()
        .baseUrl(walletApiProperties.uri)
        .build()

    fun validNftByAddress(address:String,nftId: Long): Mono<Boolean> {
        return webClient.get()
            .uri{
                it.path("v1/account/has/nft")
                it.queryParam("address",address)
                it.queryParam("nftId",nftId)
                it.build()
            }
            .retrieve()
            .bodyToMono(Boolean::class.java)
    }


    fun validAccountBalanceByAddress(address: String, chainType: ChainType, balance:BigDecimal) : Mono<Boolean> {
        return webClient.get()
            .uri{
                it.path("v1/account/has/balance")
                it.queryParam("address",address)
                it.queryParam("chainType",chainType)
                it.queryParam("requiredBalance",balance)
                it.build()
            }
            .retrieve()
            .bodyToMono(Boolean::class.java)
    }
}