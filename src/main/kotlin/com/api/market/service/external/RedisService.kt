package com.api.market.service.external

import com.api.market.service.dto.NftMetadataResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class RedisService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val nftApiService: NftApiService,
) {

    fun getNft(nftId: Long): Mono<NftMetadataResponse> {
        return reactiveRedisTemplate.opsForValue().get("NFT:$nftId")
            .flatMap { data ->
                Mono.justOrEmpty(objectMapper.convertValue(data, NftMetadataResponse::class.java))
            }
            .switchIfEmpty(
                nftApiService.getNftById(nftId)
            )
    }

    fun getNfts(nftIds: List<Long>): Flux<NftMetadataResponse> {
        if (nftIds.isEmpty()) {
            return Flux.empty()
        }
        val keys = nftIds.map { "NFT:$it" }
        return reactiveRedisTemplate.opsForValue().multiGet(keys)
            .flatMapMany { list ->
                Flux.fromIterable(list.filterNotNull().map { data ->
                    objectMapper.convertValue(data, NftMetadataResponse::class.java)
                }).switchIfEmpty (
                    nftApiService.getNftsByIds(nftIds)
                )
            }
    }
}