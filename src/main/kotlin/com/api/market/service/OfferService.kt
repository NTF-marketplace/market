package com.api.market.service

import com.api.market.controller.dto.request.OfferCreateRequest
import com.api.market.controller.dto.response.OfferResponse
import com.api.market.controller.dto.response.OfferResponse.Companion.toResponse
import com.api.market.domain.auction.AuctionRepository
import com.api.market.domain.offer.Offer
import com.api.market.domain.offer.OfferRepository
import com.api.market.enums.StatusType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

@Service
class OfferService(
    private val offerRepository: OfferRepository,
    private val auctionRepository: AuctionRepository,
) {

    private val offerCreatedPublisher = Sinks.many().multicast().onBackpressureBuffer<Offer>()

    fun offerHistory(nftId: Long): Flux<OfferResponse> {
        return auctionRepository.findByNftIdAndStatusType(nftId, StatusType.ACTIVED)
            .flatMap { auction ->
                offerRepository.findAllByAuctionIdOrderByCreatedAtDesc(auction.id!!)
                    .switchIfEmpty(Flux.empty())
                    .map { offer -> offer.toResponse() }
            }
    }


    fun create(address: String, request: OfferCreateRequest): Mono<Void> {
        return saveOffer(address, request)
            .doOnSuccess { offer ->
                offerCreatedPublisher.tryEmitNext(offer)
            }
            .then()
    }

    fun getOfferCreatedPublisher(): Flux<Offer> {
        return offerCreatedPublisher.asFlux()
    }
    fun saveOffer(address: String,request: OfferCreateRequest): Mono<Offer> {
        return auctionRepository.findById(request.auctionId)
            .switchIfEmpty( Mono.error { IllegalArgumentException("") })
            .flatMap {
            val newOffer = Offer(
                address = address,
                price = request.price,
                auctionId = it.id!!,
            )
            offerRepository.save(newOffer)
        }
    }

    fun offerPriceDesc(auctionId: Long): Mono<Offer> {
        return offerRepository.findFirstByAuctionIdOrderByPriceDesc(auctionId)
    }
}